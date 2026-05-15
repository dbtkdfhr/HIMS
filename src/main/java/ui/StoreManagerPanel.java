package ui;

import common.type.OrderStatus;
import common.type.ReceiptStatus;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import order.request.OrderRequestDTO;
import store.receipt.StoreReceiptDTO;
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.common.UiTableFactory;
import ui.data.UiServiceStore;

public class StoreManagerPanel {

  public static final String[] MENUS =
      {"내 매장 재고 조회", "발주 요청 생성", "내 발주 요청 현황", "입고 검수 및 처리", "입고 이력 조회", "판매 처리"};

  private final UiServiceStore store;
  private final EmployeeDTO user;
  private final Consumer<String> logger;

  public StoreManagerPanel(UiServiceStore store, EmployeeDTO user, Consumer<String> logger) {
    this.store = store;
    this.user = user;
    this.logger = logger;
  }

  public Map<String, JPanel> views() {
    Map<String, JPanel> views = new LinkedHashMap<>();
    views.put(MENUS[0], inventoryPanel());
    views.put(MENUS[1], orderCreatePanel());
    views.put(MENUS[2], orderStatusPanel());
    views.put(MENUS[3], unifiedReceiptPanel());
    views.put(MENUS[4], receiptHistoryPanel());
    views.put(MENUS[5], salePanel());
    return views;
  }

  private JPanel inventoryPanel() {
    JPanel panel = page("내 매장 재고 조회");
    DefaultTableModel model = inventoryModel();
    JTable table = UiTableFactory.table(model);
    table.setAutoCreateRowSorter(true);

    table.getRowSorter().setSortKeys(
        java.util.List.of(new javax.swing.RowSorter.SortKey(5, javax.swing.SortOrder.ASCENDING)));

    applyTableStyle(table);

    JButton refresh = new JButton("새로고침");
    javax.swing.JCheckBox lowStockCheck = new javax.swing.JCheckBox("부족 상품만 보기");
    lowStockCheck.setOpaque(false);

    JPanel toolbar = searchToolbar(refresh, table);
    toolbar.add(new JLabel(" | "));
    toolbar.add(lowStockCheck);

    refresh.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> fillInventory(model, lowStockCheck.isSelected())));

    lowStockCheck.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> fillInventory(model, lowStockCheck.isSelected())));

    panel.add(toolbar, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillInventory(model, lowStockCheck.isSelected()));
    return panel;
  }

  private JPanel orderCreatePanel() {
    JPanel panel = page("발주 요청 생성");
    JComboBox<InventoryDTO> productBox = new JComboBox<>();
    JTextField quantityField = new JTextField(8);
    JTextArea reasonArea = new JTextArea(4, 30);
    JButton create = new JButton("발주 요청 생성");
    JPanel form = formPanel();

    UiExceptionHandler.run(logger, () -> {
      for (InventoryDTO inventory : store.findInventoriesByStore(storeId())) {
        productBox.addItem(inventory);
      }
    });
    productBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      JLabel label = new JLabel(value == null ? ""
          : value.getProductName() + " | 현재 " + value.getCurrentQuantity() + " | 안전 "
              + value.getSafetyQuantity());
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

    create.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      InventoryDTO selected = (InventoryDTO) productBox.getSelectedItem();
      if (selected == null) {
        throw new InputException("상품을 선택해 주세요.");
      }
      int quantity = parsePositive(quantityField.getText(), "요청수량");
      String reason = required(reasonArea.getText(), "요청사유");
      OrderRequestDTO order = store.createOrderRequest(storeId(), selected.getProductId(),
          user.getEmployeeId(), quantity, reason);
      logger.accept("발주 요청 생성 완료: " + order.getOrderRequestId());
      javax.swing.JOptionPane.showMessageDialog(panel, "발주 요청이 성공적으로 생성되었습니다.", "성공",
          javax.swing.JOptionPane.INFORMATION_MESSAGE);
      quantityField.setText("");
      reasonArea.setText("");
    }));

    JPanel formContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
    formContainer.setOpaque(false);
    formContainer.add(form);
    panel.add(formContainer, BorderLayout.NORTH);
    return panel;
  }

  private JPanel orderStatusPanel() {
    JPanel panel = page("내 발주 요청 현황");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillAllOrders(model)));
    panel.add(searchToolbar(refresh, table), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillAllOrders(model));
    return panel;
  }

  private JPanel unifiedReceiptPanel() {
    JPanel panel = page("입고 검수 및 처리");
    DefaultTableModel model = orderModel();
    JTable table = UiTableFactory.table(model);
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);

    JButton confirmBtn = new JButton("정상 입고");
    JButton diffBtn = new JButton("수량 차이");
    JButton rejectBtn = new JButton("입고 반려");
    JButton refreshBtn = new JButton("새로고침");

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(confirmBtn);
    controls.add(diffBtn);
    controls.add(rejectBtn);
    controls.add(new JLabel(" | "));
    controls.add(refreshBtn);

    confirmBtn.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      long orderId = selectedOrderId(table);
      if (JOptionPane.showConfirmDialog(panel, "정상 입고 처리하시겠습니까?", "입고 확인",
          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        store.confirmReceipt(orderId, user.getEmployeeId());
        fillOrders(model, OrderStatus.RECEIVED.name());
        logger.accept("정상 입고 완료: 발주번호 " + orderId);
      }
    }));

    diffBtn.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      long orderId = selectedOrderId(table);
      String qtyStr = JOptionPane.showInputDialog(panel, "실제 입고 수량을 입력하세요:", "수량 차이 처리",
          JOptionPane.QUESTION_MESSAGE);
      if (qtyStr == null)
        return;
      int quantity = parsePositive(qtyStr, "실제 입고수량");
      String reason = JOptionPane.showInputDialog(panel, "차이 사유를 입력하세요:", "수량 차이 처리",
          JOptionPane.QUESTION_MESSAGE);
      if (reason == null || reason.trim().isEmpty())
        return;

      store.markReceiptDifference(orderId, user.getEmployeeId(), quantity, reason.trim());
      fillOrders(model, OrderStatus.RECEIVED.name());
      logger.accept("수량 차이 입고 완료: 발주번호 " + orderId);
    }));

    rejectBtn.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      long orderId = selectedOrderId(table);
      String reason = JOptionPane.showInputDialog(panel, "반려 사유를 입력하세요:", "입고 반려 처리",
          JOptionPane.WARNING_MESSAGE);
      if (reason == null || reason.trim().isEmpty())
        return;

      store.rejectReceipt(orderId, user.getEmployeeId(), reason.trim());
      fillOrders(model, OrderStatus.RECEIVED.name());
      logger.accept("입고 반려 완료: 발주번호 " + orderId);
    }));

    refreshBtn.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> fillOrders(model, OrderStatus.RECEIVED.name())));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillOrders(model, OrderStatus.RECEIVED.name()));
    return panel;
  }

  private JPanel receiptHistoryPanel() {
    JPanel panel = page("입고 이력 조회");
    DefaultTableModel model =
        UiTableFactory.model("입고ID", "발주ID", "상품", "입고수량", "차이수량", "상태", "사유");
    JTable table = UiTableFactory.table(model);
    table.setAutoCreateRowSorter(true);
    applyTableStyle(table);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillReceipts(model)));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillReceipts(model));
    return panel;
  }

  private JPanel salePanel() {
    JPanel panel = page("판매 처리");
    JComboBox<InventoryDTO> productBox = new JComboBox<>();
    JTextField quantityField = new JTextField(8);
    JButton process = new JButton("판매 처리");
    JPanel form = formPanel();

    UiExceptionHandler.run(logger, () -> {
      for (InventoryDTO inventory : store.findInventoriesByStore(storeId())) {
        productBox.addItem(inventory);
      }
    });
    productBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      JLabel label = new JLabel(
          value == null ? "" : value.getProductName() + " | 현재 " + value.getCurrentQuantity());
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

    process.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      InventoryDTO selected = (InventoryDTO) productBox.getSelectedItem();
      if (selected == null) {
        throw new InputException("상품을 선택해 주세요.");
      }
      int quantity = parsePositive(quantityField.getText(), "판매수량");
      store.processSale(storeId(), selected.getProductId(), quantity);
      productBox.repaint();
      logger.accept("판매 처리 완료: " + selected.getProductName() + " " + quantity + "개");
      javax.swing.JOptionPane.showMessageDialog(panel, "판매 처리가 완료되었습니다.", "성공",
          javax.swing.JOptionPane.INFORMATION_MESSAGE);
      quantityField.setText("");
    }));

    JPanel formContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
    formContainer.setOpaque(false);
    formContainer.add(form);
    panel.add(formContainer, BorderLayout.NORTH);
    return panel;
  }

  private DefaultTableModel inventoryModel() {
    return UiTableFactory.model("매장", "상품ID", "상품명", "브랜드", "카테고리", "현재수량", "안전재고", "상태", "부족여부");
  }

  private DefaultTableModel orderModel() {
    return UiTableFactory.model("발주ID", "매장", "상품", "요청수량", "승인수량", "상태", "외부상태", "요청일시", "요청사유",
        "반려사유");
  }

  private void fillInventory(DefaultTableModel model, boolean lowOnly) throws Exception {
    model.setRowCount(0);
    List<InventoryDTO> list =
        lowOnly ? store.findLowStockByStore(storeId()) : store.findInventoriesByStore(storeId());
    for (InventoryDTO inventory : list) {
      model.addRow(new Object[] {store.findStoreName(inventory.getStoreId()),
          inventory.getProductId(), inventory.getProductName(), inventory.getBrandName(),
          inventory.getCategoryName(), inventory.getCurrentQuantity(),
          inventory.getSafetyQuantity(), inventory.getProductStatus().getDisplayName(),
          inventory.getCurrentQuantity() <= inventory.getSafetyQuantity() ? "부족" : "정상"});
    }
  }

  private void fillAllOrders(DefaultTableModel model) throws Exception {
    model.setRowCount(0);
    for (OrderRequestDTO order : store.findOrdersByStore(storeId())) {
      model.addRow(new Object[] {order.getOrderRequestId(), store.findStoreName(order.getStoreId()),
          store.findProductName(order.getProductId()), order.getOrderQuantity(),
          order.getApprovedQuantity() == null ? "-" : order.getApprovedQuantity(),
          OrderStatus.valueOf(order.getOrderStatus()).getDisplayName(),
          store.findExternalOrderStatus(order.getOrderRequestId()),
          order.getRequestedAt() == null ? "-"
              : order.getRequestedAt()
                  .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
          nullToBlank(order.getRequestReason()), nullToBlank(order.getRejectReason())});
    }
  }

  private void fillOrders(DefaultTableModel model, String status) throws Exception {
    fillOrders(model, status, null);
  }

  private void fillOrders(DefaultTableModel model, String status, String externalStatus)
      throws Exception {
    model.setRowCount(0);
    for (OrderRequestDTO order : store.findOrdersByStore(storeId())) {
      if (!status.equals(order.getOrderStatus())) {
        continue;
      }
      if (externalStatus != null
          && !externalStatus.equals(store.findExternalOrderStatus(order.getOrderRequestId()))) {
        continue;
      }
      model.addRow(new Object[] {order.getOrderRequestId(), store.findStoreName(order.getStoreId()),
          store.findProductName(order.getProductId()), order.getOrderQuantity(),
          order.getApprovedQuantity() == null ? "-" : order.getApprovedQuantity(),
          OrderStatus.valueOf(order.getOrderStatus()).getDisplayName(),
          store.findExternalOrderStatus(order.getOrderRequestId()),
          order.getRequestedAt() == null ? "-"
              : order.getRequestedAt()
                  .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
          nullToBlank(order.getRequestReason()), nullToBlank(order.getRejectReason())});
    }
  }

  private void fillReceipts(DefaultTableModel model) throws Exception {
    model.setRowCount(0);
    for (StoreReceiptDTO receipt : store.findReceiptsByStore(storeId())) {
      OrderRequestDTO order = store.findOrder(receipt.getOrderRequestId());
      model.addRow(new Object[] {receipt.getStoreReceiptId(), receipt.getOrderRequestId(),
          order == null ? "-" : store.findProductName(order.getProductId()),
          receipt.getReceivedQuantity(), receipt.getDifferenceQuantity(),
          ReceiptStatus.valueOf(receipt.getReceiptStatus()).getDisplayName(),
          nullToBlank(receipt.getDifferenceReason())});
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

  private JPanel searchToolbar(JButton button, JTable table) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setOpaque(false);
    panel.add(button);

    panel.add(new JLabel("   🔍 검색: "));
    JTextField searchField = new JTextField(15);
    panel.add(searchField);

    javax.swing.table.TableRowSorter<?> sorter =
        (javax.swing.table.TableRowSorter<?>) table.getRowSorter();
    searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      public void insertUpdate(javax.swing.event.DocumentEvent e) {
        filter();
      }

      public void removeUpdate(javax.swing.event.DocumentEvent e) {
        filter();
      }

      public void changedUpdate(javax.swing.event.DocumentEvent e) {
        filter();
      }

      private void filter() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
          sorter.setRowFilter(null);
        } else {
          sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + text));
        }
      }
    });

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

    // 헤더(제목)도 가운데 정렬
    ((javax.swing.table.DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
        .setHorizontalAlignment(javax.swing.JLabel.CENTER);
  }
}
