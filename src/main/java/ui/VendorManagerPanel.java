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
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);
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
                + OrderStatus.valueOf(order.getOrderStatus()).getDisplayName());
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
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);
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
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);
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
        OrderStatus.valueOf(order.getOrderStatus()).getDisplayName(),
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
        "전체", OrderStatus.REQUESTED.getDisplayName(), OrderStatus.APPROVED.getDisplayName(),
        OrderStatus.REJECTED.getDisplayName(), OrderStatus.RECEIVED.getDisplayName(), OrderStatus.CANCELED.getDisplayName(),
        OrderStatus.DONE.getDisplayName()
    };
  }

  private String selectedStatus(JComboBox<String> statusBox) {
    Object selected = statusBox.getSelectedItem();
    if ("전체".equals(selected) || selected == null) return null;
    String display = String.valueOf(selected);
    for (OrderStatus os : OrderStatus.values()) {
        if (os.getDisplayName().equals(display)) return os.name();
    }
    return null;
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

  private void applyTableStyle(JTable table) {
    javax.swing.table.TableColumnModel columnModel = table.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      String colName = table.getColumnName(i);
      javax.swing.table.TableColumn col = columnModel.getColumn(i);

      if (colName.contains("ID") || colName.contains("수량")) {
        col.setPreferredWidth(60);
      } else if (colName.equals("매장")) {
        col.setPreferredWidth(100);
      } else if (colName.equals("상품") || colName.equals("상품명")) {
        col.setPreferredWidth(180);
      } else if (colName.equals("요청일시")) {
        col.setPreferredWidth(130);
      } else if (colName.equals("상태") || colName.equals("외부상태")) {
        col.setPreferredWidth(90);
      }
    }

    javax.swing.table.DefaultTableCellRenderer renderer =
        new javax.swing.table.DefaultTableCellRenderer() {
          @Override
          public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
              boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(javax.swing.JLabel.CENTER);

            if (value instanceof Boolean) {
              setText("");
            }

            int modelRow = table.convertRowIndexToModel(row);
            boolean isLowStock = false;

            int lowStockCol = -1;
            for (int i = 0; i < table.getColumnCount(); i++) {
              if (table.getColumnName(i).equals("부족여부")) {
                lowStockCol = i;
                break;
              }
            }

            if (lowStockCol != -1
                && "부족".equals(table.getModel().getValueAt(modelRow, lowStockCol))) {
              isLowStock = true;
            }

            if (!isSelected) {
              setBackground(isLowStock ? UiConstants.LOW_STOCK : UiConstants.PANEL_BACKGROUND);
              setForeground(java.awt.Color.BLACK);
              setFont(UiConstants.DEFAULT_FONT);

              String colName = table.getColumnName(column);
              if ((colName.equals("상태") || colName.equals("외부상태")) && value != null) {
                String status = value.toString();
                if (status.contains("승인") || status.contains("완료") || status.contains("정상")) {
                  setForeground(new java.awt.Color(34, 139, 34)); // Forest Green
                  setFont(getFont().deriveFont(java.awt.Font.BOLD));
                } else if (status.contains("반려") || status.contains("취소")) {
                  setForeground(new java.awt.Color(220, 20, 60)); // Crimson Red
                  setFont(getFont().deriveFont(java.awt.Font.BOLD));
                } else if (status.contains("대기") || status.contains("요청")) {
                  setForeground(java.awt.Color.GRAY);
                }
              }
            }
            return this;
          }
        };

    table.setDefaultRenderer(Object.class, renderer);
    table.setDefaultRenderer(Number.class, renderer);
    table.setDefaultRenderer(Boolean.class, renderer);

    ((javax.swing.table.DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
        .setHorizontalAlignment(javax.swing.JLabel.CENTER);
  }

}
