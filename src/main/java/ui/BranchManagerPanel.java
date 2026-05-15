package ui;

import exception.InputException;
import employee.EmployeeDTO;
import inventory.InventoryDTO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import common.type.OperationStatus;
import product.ProductDTO;
import store.StoreDTO;
import ui.common.CategorySelector;
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.common.SelectOption;
import ui.common.UiTableFactory;
import ui.data.UiServiceStore;

public class BranchManagerPanel {

  public static final String[] MENUS = {
      "내 지점 재고 현황 조회",
      "브랜드별 재고 조회",
      "카테고리별 재고 조회",
      "입점매장 정보 조회",
      "입점매장 등록 폼",
      "상품 등록 폼"
  };

  private final UiServiceStore store;
  private final EmployeeDTO user;
  private final Consumer<String> logger;

  public BranchManagerPanel(UiServiceStore store, EmployeeDTO user, Consumer<String> logger) {
    this.store = store;
    this.user = user;
    this.logger = logger;
  }

  public Map<String, JPanel> views() {
    Map<String, JPanel> views = new LinkedHashMap<>();
    views.put(MENUS[0], inventoryPanel("내 지점 재고 현황 조회", "전체"));
    views.put(MENUS[1], inventoryPanel("브랜드별 재고 조회", "브랜드"));
    views.put(MENUS[2], inventoryPanel("카테고리별 재고 조회", "카테고리"));
    views.put(MENUS[3], storeInfoPanel());
    views.put(MENUS[4], storeCreatePanel());
    views.put(MENUS[5], productCreatePanel());
    return views;
  }

  private JPanel inventoryPanel(String title, String filterType) {
    JPanel panel = page(title);
    DefaultTableModel model = UiTableFactory.model("매장", "상품ID", "상품명", "브랜드", "카테고리",
        "현재수량", "안전재고", "부족여부");
    JTable table = UiTableFactory.table(model);
    JButton search = new JButton("조회");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JComboBox<SelectOption> optionBox = new JComboBox<>();
    CategorySelector categorySelector = new CategorySelector(controls, search);
    controls.add(new JLabel(filterType + " 조건"));
    if ("전체".equals(filterType)) {
      search.setText("새로고침");
    } else if ("카테고리".equals(filterType)) {
      UiExceptionHandler.run(logger, () -> categorySelector.load(store.categories()));
    } else {
      controls.add(optionBox);
      UiExceptionHandler.run(logger, () -> fillOptionBox(optionBox, filterType));
    }
    if (!"카테고리".equals(filterType)) {
      controls.add(search);
    }
    search.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      if ("카테고리".equals(filterType)) {
        fillInventory(model, filterType, null, categorySelector.selectedCategoryIds());
        return;
      }
      fillInventory(model, filterType, selectedOption(optionBox, filterType), null);
    }));
    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillInventory(model, filterType, null, null));
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
    JComboBox<SelectOption> brandBox = new JComboBox<>();
    JTextField floorField = new JTextField(8);
    JTextField locationField = new JTextField(18);
    JComboBox<OperationStatus> statusBox = operationStatusBox();
    JButton create = new JButton("입점매장 등록");
    JPanel form = formPanel();
    DefaultTableModel masterModel = UiTableFactory.model("값1", "값2", "값3", "값4");
    JTable masterTable = UiTableFactory.table(masterModel);

    form.add(new JLabel("매장명"));
    form.add(nameField);
    form.add(new JLabel("브랜드"));
    form.add(brandBox);
    form.add(new JLabel("층 정보"));
    form.add(floorField);
    form.add(new JLabel("매장 위치"));
    form.add(locationField);
    form.add(new JLabel("운영상태"));
    form.add(statusBox);
    form.add(new JLabel());
    form.add(create);

    UiExceptionHandler.run(logger, () -> fillOptionBox(brandBox, "브랜드"));

    create.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        String name = required(nameField.getText(), "매장명");
        long branchId = branchId();
        long brandId = selectedRequired(brandBox, "브랜드").getId();
        String floor = required(floorField.getText(), "층 정보");
        String location = required(locationField.getText(), "매장 위치");
        StoreDTO dto = new StoreDTO();
        dto.setBranchId(branchId);
        dto.setBrandId(brandId);
        dto.setStoreName(name);
        dto.setFloorInfo(floor);
        dto.setStoreLocation(location);
        dto.setOperationStatus(((OperationStatus) statusBox.getSelectedItem()).name());
        store.createStore(dto);
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
    JComboBox<SelectOption> brandBox = new JComboBox<>();
    JComboBox<SelectOption> categoryBox = new JComboBox<>();
    JTextField priceField = new JTextField(8);
    JComboBox<String> seasonBox = new JComboBox<>(new String[]{"봄/여름", "가을/겨울", "상시"});
    JComboBox<String> statusBox = new JComboBox<>(new String[]{"ON_SALE", "STOPPED", "DISCONTINUED"});
    JButton create = new JButton("상품 등록");
    JPanel form = formPanel();
    DefaultTableModel masterModel = UiTableFactory.model("값1", "값2", "값3", "값4");
    JTable masterTable = UiTableFactory.table(masterModel);

    form.add(new JLabel("상품명"));
    form.add(nameField);
    form.add(new JLabel("브랜드"));
    form.add(brandBox);
    form.add(new JLabel("카테고리"));
    form.add(categoryBox);
    form.add(new JLabel("판매가"));
    form.add(priceField);
    form.add(new JLabel("시즌구분"));
    form.add(seasonBox);
    form.add(new JLabel("상품상태"));
    form.add(statusBox);
    form.add(new JLabel());
    form.add(create);

    UiExceptionHandler.run(logger, () -> {
      fillOptionBox(brandBox, "브랜드");
      fillOptionBox(categoryBox, "카테고리");
    });

    create.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        ProductDTO dto = new ProductDTO();
        dto.setProductName(required(nameField.getText(), "상품명"));
        dto.setBrandId(selectedRequired(brandBox, "브랜드").getId());
        dto.setCategoryId(selectedRequired(categoryBox, "카테고리").getId());
        dto.setPrice((int) parseLong(priceField.getText(), "판매가"));
        dto.setSeasonType(String.valueOf(seasonBox.getSelectedItem()));
        dto.setProductStatus(String.valueOf(statusBox.getSelectedItem()));
        store.createProduct(dto);
        fillMaster(masterModel, "상품");
        logger.accept("상품 등록 완료: " + dto.getProductName());
    }));

    panel.add(form, BorderLayout.NORTH);
    UiExceptionHandler.run(logger, () -> fillMaster(masterModel, "상품"));
    panel.add(UiTableFactory.scroll(masterTable), BorderLayout.CENTER);
    return panel;
  }

  private void fillInventory(DefaultTableModel model, String filterType, SelectOption option,
      Set<Long> categoryIds) throws Exception {
    model.setRowCount(0);
    for (InventoryDTO inventory : branchInventories()) {
      if (!matches(inventory, filterType, option, categoryIds)) {
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

  private boolean matches(InventoryDTO inventory, String filterType, SelectOption option,
      Set<Long> categoryIds)
      throws Exception {
    if (option == null || "전체".equals(filterType)) {
      return categoryIds == null || categoryIds.isEmpty()
          || categoryIds.contains(inventory.getCategoryId());
    }
    if ("브랜드".equals(filterType)) {
      return inventory.getBrandId() != null && option.getId() == inventory.getBrandId();
    }
    if ("카테고리".equals(filterType)) {
      return inventory.getCategoryId() != null && categoryIds != null
          && categoryIds.contains(inventory.getCategoryId());
    }
    return true;
  }

  private List<InventoryDTO> branchInventories() throws Exception {
    return store.findInventoriesByBranch(branchId());
  }

  private long branchId() {
    if (user.getBranchId() == null || user.getBranchId() <= 0) {
      throw new InputException("로그인한 지점관리자의 지점 정보가 올바르지 않습니다.");
    }
    return user.getBranchId();
  }

  private JComboBox<OperationStatus> operationStatusBox() {
    JComboBox<OperationStatus> box = new JComboBox<>(OperationStatus.values());
    box.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof OperationStatus status) {
          setText(status.getDisplayName());
        }
        return this;
      }
    });
    return box;
  }

  private void fillOptionBox(JComboBox<SelectOption> box, String type) throws Exception {
    box.removeAllItems();
    if ("브랜드".equals(type)) {
      for (brand.BrandDTO brand : store.brands()) {
        box.addItem(new SelectOption(brand.getBrandId(), brand.getBrandName()));
      }
      return;
    }
    if ("카테고리".equals(type)) {
      for (category.CategoryDTO category : store.categories()) {
        box.addItem(new SelectOption(category.getCategoryId(), category.getCategoryName()));
      }
    }
  }

  private SelectOption selectedOption(JComboBox<SelectOption> box, String filterType) {
    if ("전체".equals(filterType)) {
      return null;
    }
    return selectedRequired(box, filterType);
  }

  private SelectOption selectedRequired(JComboBox<SelectOption> box, String label) {
    SelectOption selected = (SelectOption) box.getSelectedItem();
    if (selected == null) {
      throw new InputException(label + "을 선택해 주세요.");
    }
    return selected;
  }

  private void fillStores(DefaultTableModel model) throws Exception {
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

  private void fillMaster(DefaultTableModel model, String type) throws Exception {
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
