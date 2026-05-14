package ui;

import common.type.OrderStatus;
import employee.EmployeeDTO;
import exception.InputException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import order.request.OrderRequestDTO;
import ui.common.UiConstants;
import ui.common.UiTableFactory;
import ui.data.MockDataStore;

public class VendorManagerPanel {

  public static final String[] MENUS = {
      "발주 요청 목록 조회",
      "발주 요청 상세 확인",
      "발주 승인",
      "발주 반려",
      "승인 발주 외부 전송",
      "외부 출고 처리",
      "발주 상태별 필터링",
      "발주 승인/반려 이력 조회"
  };

  private final MockDataStore store;
  private final EmployeeDTO user;
  private final Consumer<String> logger;

  public VendorManagerPanel(MockDataStore store, EmployeeDTO user, Consumer<String> logger) {
    this.store = store;
    this.user = user;
    this.logger = logger;
  }

  public Map<String, JPanel> views() {
    Map<String, JPanel> views = new LinkedHashMap<>();
    views.put(MENUS[0], orderListPanel(null));
    views.put(MENUS[1], detailPanel());
    views.put(MENUS[2], approvePanel());
    views.put(MENUS[3], rejectPanel());
    views.put(MENUS[4], transitionPanel("승인 발주 외부 전송", OrderStatus.APPROVED.name(), "-"));
    views.put(MENUS[5], transitionPanel("외부 출고 처리", OrderStatus.APPROVED.name(), OrderStatus.APPROVED.name()));
    views.put(MENUS[6], filterPanel());
    views.put(MENUS[7], historyPanel());
    return views;
  }

  private JPanel orderListPanel(String status) {
    JPanel panel = page("발주 요청 목록 조회");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> fillOrders(model, status));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, status);
    return panel;
  }

  private JPanel detailPanel() {
    JPanel panel = page("발주 요청 상세 확인");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JLabel detail = new JLabel("발주 요청을 선택하면 상세 정보가 표시됩니다.");
    table.getSelectionModel().addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
        long orderId = ((Number) table.getValueAt(table.getSelectedRow(), 0)).longValue();
        OrderRequestDTO order = store.findOrder(orderId);
        if (order != null) {
          detail.setText("발주ID " + orderId + " | 매장 " + store.findStoreName(order.getStoreId())
              + " | 상품 " + store.findProductName(order.getProductId()) + " | 상태 "
              + order.getOrderStatus());
        }
      }
    });
    panel.add(detail, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, null);
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

    approve.addActionListener(event -> {
      try {
        long orderId = selectedOrderId(table);
        int quantity = parsePositive(quantityField.getText(), "승인수량");
        store.approveOrder(orderId, user.getEmployeeId(), quantity);
        fillOrders(model, OrderStatus.REQUESTED.name());
        logger.accept("발주 승인 완료: " + orderId);
      } catch (RuntimeException e) {
        showError(panel, e);
      }
    });
    refresh.addActionListener(event -> fillOrders(model, OrderStatus.REQUESTED.name()));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, OrderStatus.REQUESTED.name());
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

    reject.addActionListener(event -> {
      try {
        long orderId = selectedOrderId(table);
        store.rejectOrder(orderId, user.getEmployeeId(), required(reasonField.getText(), "반려사유"));
        fillOrders(model, OrderStatus.REQUESTED.name());
        logger.accept("발주 반려 완료: " + orderId);
      } catch (RuntimeException e) {
        showError(panel, e);
      }
    });
    refresh.addActionListener(event -> fillOrders(model, OrderStatus.REQUESTED.name()));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, OrderStatus.REQUESTED.name());
    return panel;
  }

  private JPanel transitionPanel(String title, String status) {
    return transitionPanel(title, status, null);
  }

  private JPanel transitionPanel(String title, String status, String externalStatus) {
    JPanel panel = page(title);
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JButton process = new JButton(title);
    JButton refresh = new JButton("대상 조회");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(process);
    controls.add(refresh);

    process.addActionListener(event -> {
      try {
        long orderId = selectedOrderId(table);
        if ("-".equals(externalStatus)) {
          store.sendOrderToVendor(orderId);
        } else {
          store.shipOrder(orderId);
        }
        fillOrders(model, status, externalStatus);
        logger.accept(title + " 완료: " + orderId);
      } catch (RuntimeException e) {
        showError(panel, e);
      }
    });
    refresh.addActionListener(event -> fillOrders(model, status, externalStatus));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, status, externalStatus);
    return panel;
  }

  private JPanel filterPanel() {
    JPanel panel = page("발주 상태별 필터링");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JComboBox<String> statusBox = new JComboBox<>(new String[]{
        "전체", OrderStatus.REQUESTED.name(), OrderStatus.APPROVED.name(),
        OrderStatus.REJECTED.name(), OrderStatus.SENT.name(), OrderStatus.RECEIVED.name(),
        OrderStatus.CANCELED.name()
    });
    JButton search = new JButton("조회");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("상태"));
    controls.add(statusBox);
    controls.add(search);
    search.addActionListener(event -> {
      String status = "전체".equals(statusBox.getSelectedItem()) ? null
          : String.valueOf(statusBox.getSelectedItem());
      fillOrders(model, status);
    });
    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, null);
    return panel;
  }

  private JPanel historyPanel() {
    JPanel panel = page("발주 승인/반려 이력 조회");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> fillHistory(model));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillHistory(model);
    return panel;
  }

  private DefaultTableModel orderModel() {
    return UiTableFactory.model("발주ID", "매장", "상품", "요청수량", "승인수량", "상태", "외부상태",
        "요청사유", "반려사유");
  }

  private void fillOrders(DefaultTableModel model, String status) {
    fillOrders(model, status, null);
  }

  private void fillOrders(DefaultTableModel model, String status, String externalStatus) {
    model.setRowCount(0);
    for (OrderRequestDTO order : store.findOrdersByStatus(status)) {
      if (externalStatus == null || externalStatus.equals(store.findExternalOrderStatus(order.getOrderRequestId()))) {
        model.addRow(row(order));
      }
    }
  }

  private void fillHistory(DefaultTableModel model) {
    model.setRowCount(0);
    for (OrderRequestDTO order : store.orders()) {
      if (OrderStatus.APPROVED.name().equals(order.getOrderStatus())
          || OrderStatus.REJECTED.name().equals(order.getOrderStatus())
          || order.getApprovalEmployeeId() != null) {
        model.addRow(row(order));
      }
    }
  }

  private Object[] row(OrderRequestDTO order) {
    return new Object[]{
        order.getOrderRequestId(),
        store.findStoreName(order.getStoreId()),
        store.findProductName(order.getProductId()),
        order.getOrderQuantity(),
        order.getApprovedQuantity() == null ? "-" : order.getApprovedQuantity(),
        order.getOrderStatus(),
        store.findExternalOrderStatus(order.getOrderRequestId()),
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

  private void showError(JPanel panel, RuntimeException e) {
    logger.accept("오류: " + e.getMessage());
  }
}
