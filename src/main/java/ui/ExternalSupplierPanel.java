package ui;

import exception.InputException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import order.external.ExternalOrderReceiptDTO;
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.common.UiTableFactory;
import ui.data.UiServiceStore;

public class ExternalSupplierPanel {

  public static final String[] MENUS = {
      "외부 발주처 접수 목록",
      "외부 발주처 승인/거절"
  };

  private static final String RECEIVED_STATUS = "접수 완료";
  private static final String SHIPPED_STATUS = "출고 완료";
  private static final String REJECTED_STATUS = "출고 반려";

  private final UiServiceStore store;
  private final Consumer<String> logger;

  public ExternalSupplierPanel(UiServiceStore store, Consumer<String> logger) {
    this.store = store;
    this.logger = logger;
  }

  public Map<String, Supplier<JPanel>> views() {
    Map<String, Supplier<JPanel>> views = new LinkedHashMap<>();
    views.put(MENUS[0], this::receiptListPanel);
    views.put(MENUS[1], this::decisionPanel);
    return views;
  }

  private JPanel receiptListPanel() {
    JPanel panel = page("외부 발주처 접수 목록");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillExternalOrders(model, false)));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillExternalOrders(model, false));
    return panel;
  }

  private JPanel decisionPanel() {
    JPanel panel = page("외부 발주처 승인/거절");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);
    JTextField reasonField = new JTextField(24);
    JButton approve = new JButton("승인");
    JButton reject = new JButton("거절");
    JButton refresh = new JButton("새로고침");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.setOpaque(false);
    controls.add(approve);
    controls.add(reject);
    controls.add(refresh);

    JPanel rejectReasonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    rejectReasonPanel.setOpaque(false);
    rejectReasonPanel.add(new JLabel("거절 사유"));
    rejectReasonPanel.add(reasonField);
    JButton rejectConfirm = new JButton("거절 확정");
    JButton cancelReject = new JButton("취소");
    rejectReasonPanel.add(rejectConfirm);
    rejectReasonPanel.add(cancelReject);
    rejectReasonPanel.setVisible(false);

    JPanel north = new JPanel(new BorderLayout());
    north.setOpaque(false);
    north.add(controls, BorderLayout.NORTH);
    north.add(rejectReasonPanel, BorderLayout.CENTER);

    approve.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      long orderId = selectedOrderId(table);
      store.approveExternalOrder(orderId);
      fillExternalOrders(model, true);
      logger.accept("외부 발주처 승인 완료 : "  + orderId);
    }));
    reject.addActionListener(event -> {
      rejectReasonPanel.setVisible(true);
      SwingUtilities.invokeLater(reasonField::requestFocusInWindow);
    });
    rejectConfirm.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      long orderId = selectedOrderId(table);
      store.rejectExternalOrder(orderId, required(reasonField.getText(), "거절사유"));
      reasonField.setText("");
      rejectReasonPanel.setVisible(false);
      fillExternalOrders(model, true);
      logger.accept("외부 발주처 거절 완료 : " + orderId);
    }));
    cancelReject.addActionListener(event -> {
      reasonField.setText("");
      rejectReasonPanel.setVisible(false);
    });
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillExternalOrders(model, true)));

    panel.add(north, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillExternalOrders(model, true));
    return panel;
  }

  private DefaultTableModel orderModel() {
    return UiTableFactory.model("발주ID", "매장", "상품", "요청수량", "승인수량", "외부접수ID", "상태");
  }

  private void fillExternalOrders(DefaultTableModel model, boolean onlyReceived) throws Exception {
    model.setRowCount(0);
    for (ExternalOrderReceiptDTO receipt : store.externalOrderReceipts()) {
      if (isExternalOrder(receipt, onlyReceived)) {
        model.addRow(row(receipt));
      }
    }
  }

  private boolean isExternalOrder(ExternalOrderReceiptDTO receipt, boolean onlyReceived) {
    String externalStatus = receipt.getReceiptStatus();
    if (onlyReceived) {
      return "RECEIVED".equals(externalStatus);
    }
    return "RECEIVED".equals(externalStatus)
        || "SHIPPED".equals(externalStatus)
        || "REJECTED".equals(externalStatus);
  }

  private Object[] row(ExternalOrderReceiptDTO receipt) throws Exception {
    Long approvedQuantity =
        "REJECTED".equals(receipt.getReceiptStatus()) ? null : (long) receipt.getApprovedQuantity();

    String storeName = receipt.getRequestStoreName();
    String productName;
    try {
      productName = store.findProductName(receipt.getSupplierProductId());
    } catch (Exception ignored) {
      productName = String.valueOf(receipt.getSupplierProductId());
    }

    String externalStatusEng = store.findExternalOrderStatus(receipt.getInternalOrderRequestId());
    String externalStatus = "-";
    if (externalStatusEng != null && !externalStatusEng.isEmpty()) {
        try {
            externalStatus = common.type.External_OrderStatus.valueOf(externalStatusEng).getDisplayName();
        } catch(Exception e) {
            externalStatus = externalStatusEng;
        }
    }

    return new Object[]{
        receipt.getInternalOrderRequestId(), // 0. 발주ID
        storeName,                           // 1. 매장
        productName,                         // 2. 상품
        receipt.getRequestQuantity(),        // 3. 요청수량
        approvedQuantity == null ? "-" : approvedQuantity, // 4. 승인수량
        receipt.getExternalOrderReceiptId(), // 5. 외부접수ID
        externalStatus                       // 6. 상태
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
      throw new InputException("외부 발주 요청을 선택해 주세요.");
    }
    return ((Number) table.getValueAt(row, 0)).longValue();
  }

  private String required(String value, String label) {
    String text = value == null ? "" : value.trim();
    if (text.isEmpty()) {
      throw new InputException(label + "을 입력해 주세요.");
    }
    return text;
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
                } else if (status.contains("반려") || status.contains("취소") || status.contains("거절")) {
                  setForeground(new java.awt.Color(220, 20, 60)); // Crimson Red
                  setFont(getFont().deriveFont(java.awt.Font.BOLD));
                } else if (status.contains("대기") || status.contains("요청") || status.contains("접수")) {
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
