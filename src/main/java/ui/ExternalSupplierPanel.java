package ui;

import exception.InputException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import order.request.OrderRequestDTO;
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

  public Map<String, JPanel> views() {
    Map<String, JPanel> views = new LinkedHashMap<>();
    views.put(MENUS[0], receiptListPanel());
    views.put(MENUS[1], decisionPanel());
    return views;
  }

  private JPanel receiptListPanel() {
    JPanel panel = page("외부 발주처 접수 목록");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
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
    return UiTableFactory.model("발주ID", "매장", "상품", "요청수량", "승인수량", "외부접수ID", "내부상태",
        "외부상태", "사유");
  }

  private void fillExternalOrders(DefaultTableModel model, boolean onlyReceived) throws Exception {
    model.setRowCount(0);
    for (OrderRequestDTO order : store.orders()) {
      if (isExternalOrder(order, onlyReceived)) {
        model.addRow(row(order));
      }
    }
  }

  private boolean isExternalOrder(OrderRequestDTO order, boolean onlyReceived) throws Exception {
    String externalStatus = store.findExternalOrderStatus(order.getOrderRequestId());
    if (onlyReceived) {
      return RECEIVED_STATUS.equals(externalStatus);
    }
    return RECEIVED_STATUS.equals(externalStatus)
        || SHIPPED_STATUS.equals(externalStatus)
        || REJECTED_STATUS.equals(externalStatus);
  }

  private Object[] row(OrderRequestDTO order) throws Exception {
    return new Object[]{
        order.getOrderRequestId(),
        store.findStoreName(order.getStoreId()),
        store.findProductName(order.getProductId()),
        order.getOrderQuantity(),
        order.getApprovedQuantity() == null ? "-" : order.getApprovedQuantity(),
        order.getExternalOrderId() == null ? "-" : order.getExternalOrderId(),
        order.getOrderStatus(),
        store.findExternalOrderStatus(order.getOrderRequestId()),
        store.findExternalRejectReason(order.getOrderRequestId())
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
}
