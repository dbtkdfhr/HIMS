package ui;

import common.type.OrderStatus;
import employee.EmployeeDTO;
import exception.InputException;
import inventory.InventoryDTO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import order.request.OrderRequestDTO;
import store.receipt.StoreReceiptDTO;
import ui.common.UiConstants;
import ui.common.UiTableFactory;
import ui.data.MockDataStore;

public class StoreManagerPanel {

  public static final String[] MENUS = {
      "내 매장 재고 조회",
      "안전재고 부족 상품 조회",
      "발주 요청 생성",
      "입고 검수 대상 조회",
      "정상 입고 처리",
      "입고 수량 차이 처리",
      "입고 반려 처리",
      "입고 이력 조회",
      "판매 처리"
  };

  private final MockDataStore store;
  private final EmployeeDTO user;
  private final Consumer<String> logger;

  public StoreManagerPanel(MockDataStore store, EmployeeDTO user, Consumer<String> logger) {
    this.store = store;
    this.user = user;
    this.logger = logger;
  }

  public Map<String, JPanel> views() {
    Map<String, JPanel> views = new LinkedHashMap<>();
    views.put(MENUS[0], inventoryPanel(false));
    views.put(MENUS[1], inventoryPanel(true));
    views.put(MENUS[2], orderCreatePanel());
    views.put(MENUS[3], receiptTargetPanel());
    views.put(MENUS[4], receiptActionPanel("정상 입고 처리"));
    views.put(MENUS[5], receiptActionPanel("입고 수량 차이 처리"));
    views.put(MENUS[6], receiptActionPanel("입고 반려 처리"));
    views.put(MENUS[7], receiptHistoryPanel());
    views.put(MENUS[8], salePanel());
    return views;
  }

  private JPanel inventoryPanel(boolean lowOnly) {
    JPanel panel = page(lowOnly ? "안전재고 부족 상품 조회" : "내 매장 재고 조회");
    DefaultTableModel model = inventoryModel();
    JTable table = UiTableFactory.table(model);
    UiTableFactory.applyRowHighlight(table, row -> Boolean.TRUE.equals(model.getValueAt(row, 8)));
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> fillInventory(model, lowOnly));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillInventory(model, lowOnly);
    return panel;
  }

  private JPanel orderCreatePanel() {
    JPanel panel = page("발주 요청 생성");
    JComboBox<InventoryDTO> productBox = new JComboBox<>();
    JTextField quantityField = new JTextField(8);
    JTextArea reasonArea = new JTextArea(4, 30);
    JButton create = new JButton("발주 요청 생성");
    JPanel form = formPanel();

    for (InventoryDTO inventory : store.findInventoriesByStore(storeId())) {
      productBox.addItem(inventory);
    }
    productBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      JLabel label = new JLabel(value == null ? "" : value.getProductName() + " | 현재 "
          + value.getCurrentQuantity() + " | 안전 " + value.getSafetyQuantity());
      label.setOpaque(true);
      label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
      return label;
    });

    form.add(new JLabel("상품"));
    form.add(productBox);
    form.add(new JLabel("요청수량"));
    form.add(quantityField);
    form.add(new JLabel("요청사유"));
    form.add(new JScrollPane(reasonArea));
    form.add(new JLabel());
    form.add(create);

    create.addActionListener(event -> {
      try {
        InventoryDTO selected = (InventoryDTO) productBox.getSelectedItem();
        if (selected == null) {
          throw new InputException("상품을 선택해 주세요.");
        }
        int quantity = parsePositive(quantityField.getText(), "요청수량");
        String reason = required(reasonArea.getText(), "요청사유");
        OrderRequestDTO order = store.createOrderRequest(storeId(), selected.getProductId(),
            user.getEmployeeId(), quantity, reason);
        logger.accept("발주 요청 생성 완료: " + order.getOrderRequestId());
      } catch (RuntimeException e) {
        showError(panel, e);
      }
    });

    panel.add(form, BorderLayout.NORTH);
    return panel;
  }

  private JPanel receiptTargetPanel() {
    JPanel panel = page("입고 검수 대상 조회");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> fillOrders(model, OrderStatus.SENT.name()));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, OrderStatus.SENT.name());
    return panel;
  }

  private JPanel receiptActionPanel(String title) {
    JPanel panel = page(title);
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    JTextField quantityField = new JTextField(8);
    JTextField reasonField = new JTextField(24);
    JButton process = new JButton(title);
    JButton refresh = new JButton("대상 조회");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

    if ("입고 수량 차이 처리".equals(title)) {
      controls.add(new JLabel("실제 입고수량"));
      controls.add(quantityField);
      controls.add(new JLabel("차이사유"));
      controls.add(reasonField);
    } else if ("입고 반려 처리".equals(title)) {
      controls.add(new JLabel("반려사유"));
      controls.add(reasonField);
    }
    controls.add(process);
    controls.add(refresh);

    process.addActionListener(event -> {
      try {
        long orderId = selectedOrderId(table);
        if ("정상 입고 처리".equals(title)) {
          store.confirmReceipt(orderId, user.getEmployeeId());
        } else if ("입고 수량 차이 처리".equals(title)) {
          int quantity = parsePositive(quantityField.getText(), "실제 입고수량");
          store.markReceiptDifference(orderId, user.getEmployeeId(), quantity,
              required(reasonField.getText(), "차이사유"));
        } else {
          store.rejectReceipt(orderId, user.getEmployeeId(), required(reasonField.getText(), "반려사유"));
        }
        fillOrders(model, OrderStatus.SENT.name());
        logger.accept(title + " 완료: 발주요청 " + orderId);
      } catch (RuntimeException e) {
        showError(panel, e);
      }
    });
    refresh.addActionListener(event -> fillOrders(model, OrderStatus.SENT.name()));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillOrders(model, OrderStatus.SENT.name());
    return panel;
  }

  private JPanel receiptHistoryPanel() {
    JPanel panel = page("입고 이력 조회");
    DefaultTableModel model = UiTableFactory.model("입고ID", "발주ID", "상품", "입고수량", "차이수량",
        "상태", "사유");
    JTable table = UiTableFactory.table(model);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> fillReceipts(model));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    fillReceipts(model);
    return panel;
  }

  private JPanel salePanel() {
    JPanel panel = page("판매 처리");
    JComboBox<InventoryDTO> productBox = new JComboBox<>();
    JTextField quantityField = new JTextField(8);
    JButton process = new JButton("판매 처리");
    JPanel form = formPanel();

    for (InventoryDTO inventory : store.findInventoriesByStore(storeId())) {
      productBox.addItem(inventory);
    }
    productBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      JLabel label = new JLabel(value == null ? "" : value.getProductName() + " | 현재 "
          + value.getCurrentQuantity());
      label.setOpaque(true);
      label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
      return label;
    });

    form.add(new JLabel("상품"));
    form.add(productBox);
    form.add(new JLabel("판매수량"));
    form.add(quantityField);
    form.add(new JLabel());
    form.add(process);

    process.addActionListener(event -> {
      try {
        InventoryDTO selected = (InventoryDTO) productBox.getSelectedItem();
        if (selected == null) {
          throw new InputException("상품을 선택해 주세요.");
        }
        int quantity = parsePositive(quantityField.getText(), "판매수량");
        store.processSale(storeId(), selected.getProductId(), quantity);
        productBox.repaint();
        logger.accept("판매 처리 완료: " + selected.getProductName() + " " + quantity + "개");
      } catch (RuntimeException e) {
        showError(panel, e);
      }
    });

    panel.add(form, BorderLayout.NORTH);
    return panel;
  }

  private DefaultTableModel inventoryModel() {
    return UiTableFactory.model("매장", "상품ID", "상품명", "브랜드", "카테고리", "현재수량", "안전재고",
        "상태", "부족여부");
  }

  private DefaultTableModel orderModel() {
    return UiTableFactory.model("발주ID", "매장", "상품", "요청수량", "승인수량", "상태", "요청사유",
        "반려사유");
  }

  private void fillInventory(DefaultTableModel model, boolean lowOnly) {
    model.setRowCount(0);
    List<InventoryDTO> list = lowOnly ? store.findLowStockByStore(storeId())
        : store.findInventoriesByStore(storeId());
    for (InventoryDTO inventory : list) {
      model.addRow(new Object[]{
          store.findStoreName(inventory.getStoreId()),
          inventory.getProductId(),
          inventory.getProductName(),
          inventory.getBrandName(),
          inventory.getCategoryName(),
          inventory.getCurrentQuantity(),
          inventory.getSafetyQuantity(),
          inventory.getProductStatus().getDisplayName(),
          inventory.getCurrentQuantity() <= inventory.getSafetyQuantity()
      });
    }
  }

  private void fillOrders(DefaultTableModel model, String status) {
    model.setRowCount(0);
    for (OrderRequestDTO order : store.findOrdersByStore(storeId())) {
      if (!status.equals(order.getOrderStatus())) {
        continue;
      }
      model.addRow(new Object[]{
          order.getOrderRequestId(),
          store.findStoreName(order.getStoreId()),
          store.findProductName(order.getProductId()),
          order.getOrderQuantity(),
          order.getApprovedQuantity() == null ? "-" : order.getApprovedQuantity(),
          order.getOrderStatus(),
          nullToBlank(order.getRequestReason()),
          nullToBlank(order.getRejectReason())
      });
    }
  }

  private void fillReceipts(DefaultTableModel model) {
    model.setRowCount(0);
    for (StoreReceiptDTO receipt : store.findReceiptsByStore(storeId())) {
      OrderRequestDTO order = store.findOrder(receipt.getOrderRequestId());
      model.addRow(new Object[]{
          receipt.getStoreReceiptId(),
          receipt.getOrderRequestId(),
          order == null ? "-" : store.findProductName(order.getProductId()),
          receipt.getReceivedQuantity(),
          receipt.getDifferenceQuantity(),
          receipt.getReceiptStatus(),
          nullToBlank(receipt.getDifferenceReason())
      });
    }
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

  private JPanel formPanel() {
    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    panel.setOpaque(false);
    return panel;
  }

  private long storeId() {
    if (user.getStoreId() == null) {
      return 101L;
    }
    return user.getStoreId();
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
