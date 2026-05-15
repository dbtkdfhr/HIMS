package ui;

import common.type.OrderStatus;
import employee.EmployeeDTO;
import exception.InputException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import order.request.OrderRequestDTO;
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.common.UiTableFactory;
import ui.data.UiServiceStore;

public class VendorManagerPanel {

  public static final String[] MENUS = {
      "발주 요청 목록 조회",
      "발주 승인",
      "발주 반려"
  };

  private final UiServiceStore store;
  private final EmployeeDTO user;
  private final Consumer<String> logger;

  public VendorManagerPanel(UiServiceStore store, EmployeeDTO user, Consumer<String> logger) {
    this.store = store;
    this.user = user;
    this.logger = logger;
  }

  public Map<String, Supplier<JPanel>> views() {
    Map<String, Supplier<JPanel>> views = new LinkedHashMap<>();
    views.put(MENUS[0], () -> orderListPanel(null));
    views.put(MENUS[1], this::approvePanel);
    views.put(MENUS[2], this::rejectPanel);
    return views;
  }

  private JPanel orderListPanel(String status) {
    JPanel panel = page("발주 요청 목록 조회");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JLabel detail = new JLabel("발주 요청을 선택하면 상세 정보가 표시됩니다.");
    JComboBox<String> statusBox = new JComboBox<>(orderStatusOptions());
    statusBox.setSelectedItem(status == null ? "전체" : status);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      detail.setText("발주 요청을 선택하면 상세 정보가 표시됩니다.");
      fillOrders(model, selectedStatus(statusBox));
    }));
    statusBox.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      detail.setText("발주 요청을 선택하면 상세 정보가 표시됩니다.");
      fillOrders(model, selectedStatus(statusBox));
    }));

    table.getSelectionModel().addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
        long orderId = ((Number) table.getValueAt(table.getSelectedRow(), 0)).longValue();
        UiExceptionHandler.run(logger, () -> {
          OrderRequestDTO order = store.findOrder(orderId);
          if (order != null) {
            detail.setText("발주ID " + orderId + " | 매장 " + store.findStoreName(order.getStoreId())
                + " | 상품 " + store.findProductName(order.getProductId()) + " | 상태 "
                + order.getOrderStatus());
          }
        });
      }
    });

    JPanel top = new JPanel(new BorderLayout());
    top.setOpaque(false);
    top.add(orderListToolbar(statusBox, refresh), BorderLayout.NORTH);
    top.add(detail, BorderLayout.SOUTH);

    panel.add(top, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillOrders(model, status));
    return panel;
  }

  private JPanel approvePanel() {
    JPanel panel = page("발주 승인");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JTextField quantityField = new JTextField(8);
    JButton approve = new JButton("승인");
    JButton refresh = new JButton("대기 목록");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("승인수량"));
    controls.add(quantityField);
    controls.add(approve);
    controls.add(refresh);

    approve.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        long orderId = selectedOrderId(table);
        int quantity = parsePositive(quantityField.getText(), "승인수량");
        store.approveOrder(orderId, user.getEmployeeId(), quantity);
        fillOrders(model, OrderStatus.REQUESTED.name());
        logger.accept("발주 승인 완료: " + orderId);
    }));
    refresh.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> fillOrders(model, OrderStatus.REQUESTED.name())));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillOrders(model, OrderStatus.REQUESTED.name()));
    return panel;
  }

  private JPanel rejectPanel() {
    JPanel panel = page("발주 반려");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JTextField reasonField = new JTextField(24);
    JButton reject = new JButton("반려");
    JButton refresh = new JButton("대기 목록");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("반려사유"));
    controls.add(reasonField);
    controls.add(reject);
    controls.add(refresh);

    reject.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        long orderId = selectedOrderId(table);
        store.rejectOrder(orderId, user.getEmployeeId(), required(reasonField.getText(), "반려사유"));
        fillOrders(model, OrderStatus.REQUESTED.name());
        logger.accept("발주 반려 완료: " + orderId);
    }));
    refresh.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> fillOrders(model, OrderStatus.REQUESTED.name())));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillOrders(model, OrderStatus.REQUESTED.name()));
    return panel;
  }

  private DefaultTableModel orderModel() {
    return UiTableFactory.model("발주ID", "매장", "상품", "요청수량", "승인수량", "상태", "요청사유", "반려사유");
  }

  private void fillOrders(DefaultTableModel model, String status) throws Exception {
    model.setRowCount(0);
    for (OrderRequestDTO order : store.findOrdersByStatus(status)) {
      model.addRow(row(order));
    }
  }

  private Object[] row(OrderRequestDTO order) throws Exception {
    return new Object[]{
        order.getOrderRequestId(),
        store.findStoreName(order.getStoreId()),
        store.findProductName(order.getProductId()),
        order.getOrderQuantity(),
        order.getApprovedQuantity() == null ? "-" : order.getApprovedQuantity(),
        order.getOrderStatus(),
        nullToBlank(order.getRequestReason()),
        nullToBlank(order.getRejectReason())
    };
  }

  private JPanel page(String title) {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBackground(UiConstants.PANEL_BACKGROUND);
    panel.setBorder(javax.swing.BorderFactory.createTitledBorder(title));
    return panel;
  }

  private JPanel toolbar(JButton button) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setOpaque(false);
    panel.add(button);
    return panel;
  }

  private JPanel orderListToolbar(JComboBox<String> statusBox, JButton refresh) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setOpaque(false);
    panel.add(new JLabel("상태"));
    panel.add(statusBox);
    panel.add(refresh);
    return panel;
  }

  private String[] orderStatusOptions() {
    return new String[]{
        "전체", OrderStatus.REQUESTED.name(), OrderStatus.APPROVED.name(),
        OrderStatus.REJECTED.name(), OrderStatus.RECEIVED.name(), OrderStatus.CANCELED.name(),
        OrderStatus.DONE.name()
    };
  }

  private String selectedStatus(JComboBox<String> statusBox) {
    Object selected = statusBox.getSelectedItem();
    return "전체".equals(selected) ? null : String.valueOf(selected);
  }

  private long selectedOrderId(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      throw new InputException("발주 요청을 선택해 주세요.");
    }
    return ((Number) table.getValueAt(row, 0)).longValue();
  }

  private int parsePositive(String value, String label) {
    try {
      int number = Integer.parseInt(value.trim());
      if (number <= 0) {
        throw new NumberFormatException();
      }
      return number;
    } catch (NumberFormatException e) {
      throw new InputException(label + "은 1 이상의 숫자여야 합니다.");
    }
  }

  private String required(String value, String label) {
    String text = value == null ? "" : value.trim();
    if (text.isEmpty()) {
      throw new InputException(label + "을 입력해 주세요.");
    }
    return text;
  }

  private String nullToBlank(String value) {
    return value == null ? "" : value;
  }

}
