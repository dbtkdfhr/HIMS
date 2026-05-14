package ui;

import exception.InputException;
import inventory.InventoryDTO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import product.ProductDTO;
import store.StoreDTO;
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.common.UiTableFactory;
import ui.data.MockDataStore;

public class BranchManagerPanel {

  public static final String[] MENUS = {
      "전체 재고 현황 조회",
      "지점별 재고 조회",
      "브랜드별 재고 조회",
      "카테고리별 재고 조회",
      "입점매장 정보 조회",
      "입점매장 등록 폼",
      "상품 등록 폼"
  };

  private final MockDataStore store;
  private final Consumer<String> logger;

  public BranchManagerPanel(MockDataStore store, Consumer<String> logger) {
    this.store = store;
    this.logger = logger;
  }

  public Map<String, JPanel> views() {
    Map<String, JPanel> views = new LinkedHashMap<>();
    views.put(MENUS[0], inventoryPanel("전체 재고 현황 조회", "전체"));
    views.put(MENUS[1], inventoryPanel("지점별 재고 조회", "지점"));
    views.put(MENUS[2], inventoryPanel("브랜드별 재고 조회", "브랜드"));
    views.put(MENUS[3], inventoryPanel("카테고리별 재고 조회", "카테고리"));
    views.put(MENUS[4], storeInfoPanel());
    views.put(MENUS[5], storeCreatePanel());
    views.put(MENUS[6], productCreatePanel());
    return views;
  }

  private JPanel inventoryPanel(String title, String filterType) {
    JPanel panel = page(title);
    DefaultTableModel model = UiTableFactory.model("매장", "상품ID", "상품명", "브랜드", "카테고리",
        "현재수량", "안전재고", "부족여부");
    JTable table = UiTableFactory.table(model);
    JTextField keywordField = new JTextField(16);
    JButton search = new JButton("조회");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel(filterType + " 조건"));
    controls.add(keywordField);
    controls.add(search);
    search.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> fillInventory(model, filterType, keywordField.getText())));
    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillInventory(model, filterType, ""));
    return panel;
  }

  private JPanel storeInfoPanel() {
    JPanel panel = page("입점매장 정보 조회");
    DefaultTableModel model = UiTableFactory.model("매장ID", "지점ID", "브랜드ID", "매장명", "층",
        "위치", "상태");
    JTable table = UiTableFactory.table(model);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillStores(model)));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillStores(model));
    return panel;
  }

  private JPanel storeCreatePanel() {
    JPanel panel = page("입점매장 등록 폼");
    JTextField nameField = new JTextField(18);
    JTextField branchField = new JTextField(8);
    JTextField brandField = new JTextField(8);
    JTextField floorField = new JTextField(8);
    JTextField locationField = new JTextField(18);
    JComboBox<String> statusBox = new JComboBox<>(new String[]{"운영중", "운영중지"});
    JButton create = new JButton("입점매장 등록");
    JPanel form = formPanel();
    DefaultTableModel masterModel = UiTableFactory.model("값1", "값2", "값3", "값4");
    JTable masterTable = UiTableFactory.table(masterModel);

    form.add(new JLabel("매장명"));
    form.add(nameField);
    form.add(new JLabel("지점ID"));
    form.add(branchField);
    form.add(new JLabel("브랜드ID"));
    form.add(brandField);
    form.add(new JLabel("층 정보"));
    form.add(floorField);
    form.add(new JLabel("매장 위치"));
    form.add(locationField);
    form.add(new JLabel("운영상태"));
    form.add(statusBox);
    form.add(new JLabel());
    form.add(create);

    create.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        String name = required(nameField.getText(), "매장명");
        long branchId = parseLong(branchField.getText(), "지점ID");
        long brandId = parseLong(brandField.getText(), "브랜드ID");
        String floor = required(floorField.getText(), "층 정보");
        String location = required(locationField.getText(), "매장 위치");
        StoreDTO dto = new StoreDTO();
        dto.setStoreId(nextStoreId());
        dto.setBranchId(branchId);
        dto.setBrandId(brandId);
        dto.setStoreName(name);
        dto.setFloorInfo(floor);
        dto.setStoreLocation(location);
        dto.setOperationStatus(String.valueOf(statusBox.getSelectedItem()));
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        store.stores().add(dto);
        store.addMasterRecord("입점매장", String.valueOf(dto.getStoreId()), name, location,
            dto.getOperationStatus());
        fillMaster(masterModel, "입점매장");
        logger.accept("입점매장 등록 완료: " + name);
    }));

    panel.add(form, BorderLayout.NORTH);
    UiExceptionHandler.run(logger, () -> fillMaster(masterModel, "입점매장"));
    panel.add(UiTableFactory.scroll(masterTable), BorderLayout.CENTER);
    return panel;
  }

  private JPanel productCreatePanel() {
    JPanel panel = page("상품 등록 폼");
    JTextField nameField = new JTextField(18);
    JTextField brandField = new JTextField(8);
    JTextField categoryField = new JTextField(8);
    JTextField priceField = new JTextField(8);
    JComboBox<String> seasonBox = new JComboBox<>(new String[]{"봄/여름", "가을/겨울", "상시"});
    JComboBox<String> statusBox = new JComboBox<>(new String[]{"ON_SALE", "STOPPED", "DISCONTINUED"});
    JButton create = new JButton("상품 등록");
    JPanel form = formPanel();
    DefaultTableModel masterModel = UiTableFactory.model("값1", "값2", "값3", "값4");
    JTable masterTable = UiTableFactory.table(masterModel);

    form.add(new JLabel("상품명"));
    form.add(nameField);
    form.add(new JLabel("브랜드ID"));
    form.add(brandField);
    form.add(new JLabel("카테고리ID"));
    form.add(categoryField);
    form.add(new JLabel("판매가"));
    form.add(priceField);
    form.add(new JLabel("시즌구분"));
    form.add(seasonBox);
    form.add(new JLabel("상품상태"));
    form.add(statusBox);
    form.add(new JLabel());
    form.add(create);

    create.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(nextProductId());
        dto.setProductName(required(nameField.getText(), "상품명"));
        dto.setBrandId(parseLong(brandField.getText(), "브랜드ID"));
        dto.setCategoryId(parseLong(categoryField.getText(), "카테고리ID"));
        dto.setPrice((int) parseLong(priceField.getText(), "판매가"));
        dto.setSeasonType(String.valueOf(seasonBox.getSelectedItem()));
        dto.setProductStatus(String.valueOf(statusBox.getSelectedItem()));
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        store.products().add(dto);
        store.addMasterRecord("상품", String.valueOf(dto.getProductId()), dto.getProductName(),
            String.valueOf(dto.getPrice()), dto.getProductStatus());
        fillMaster(masterModel, "상품");
        logger.accept("상품 등록 완료: " + dto.getProductName());
    }));

    panel.add(form, BorderLayout.NORTH);
    UiExceptionHandler.run(logger, () -> fillMaster(masterModel, "상품"));
    panel.add(UiTableFactory.scroll(masterTable), BorderLayout.CENTER);
    return panel;
  }

  private void fillInventory(DefaultTableModel model, String filterType, String keyword) {
    model.setRowCount(0);
    String normalized = keyword == null ? "" : keyword.trim();
    for (InventoryDTO inventory : store.inventories()) {
      if (!matches(inventory, filterType, normalized)) {
        continue;
      }
      model.addRow(new Object[]{
          store.findStoreName(inventory.getStoreId()),
          inventory.getProductId(),
          inventory.getProductName(),
          inventory.getBrandName(),
          inventory.getCategoryName(),
          inventory.getCurrentQuantity(),
          inventory.getSafetyQuantity(),
          inventory.getCurrentQuantity() <= inventory.getSafetyQuantity()
      });
    }
  }

  private boolean matches(InventoryDTO inventory, String filterType, String keyword) {
    if (keyword.isEmpty() || "전체".equals(filterType)) {
      return true;
    }
    if ("지점".equals(filterType)) {
      return store.findStoreName(inventory.getStoreId()).contains(keyword);
    }
    if ("브랜드".equals(filterType)) {
      return inventory.getBrandName().contains(keyword);
    }
    if ("카테고리".equals(filterType)) {
      return inventory.getCategoryName().contains(keyword);
    }
    return true;
  }

  private void fillStores(DefaultTableModel model) {
    model.setRowCount(0);
    for (StoreDTO dto : store.stores()) {
      model.addRow(new Object[]{
          dto.getStoreId(),
          dto.getBranchId(),
          dto.getBrandId(),
          dto.getStoreName(),
          dto.getFloorInfo(),
          dto.getStoreLocation(),
          dto.getOperationStatus()
      });
    }
  }

  private void fillMaster(DefaultTableModel model, String type) {
    model.setRowCount(0);
    for (String[] row : store.masterRecords().getOrDefault(type, java.util.Collections.emptyList())) {
      model.addRow(row);
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

  private long nextStoreId() {
    long max = 100;
    for (StoreDTO dto : store.stores()) {
      max = Math.max(max, dto.getStoreId());
    }
    return max + 1;
  }

  private long nextProductId() {
    long max = 1000;
    for (ProductDTO dto : store.products()) {
      max = Math.max(max, dto.getProductId());
    }
    return max + 1;
  }

  private long parseLong(String value, String label) {
    try {
      long number = Long.parseLong(value.trim());
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

}
