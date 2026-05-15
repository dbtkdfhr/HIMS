package ui;

import common.type.RoleType;
import branch.BranchDTO;
import employee.EmployeeDTO;
import exception.InputException;
import exception.NotFoundException;
import inventory.InventoryDTO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import store.StoreDTO;
import ui.common.CategorySelector;
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.common.UiTableFactory;
import ui.data.UiServiceStore;

public class SystemManagerPanel {
  private static final String INITIAL_PASSWORD = "pass1234";
  private static final String RESET_PASSWORD = "changeme";

  public static final String[] MENUS = {
      "직원 계정 목록",
      "직원 계정 생성 폼",
      "직원 권한 변경 폼",
      "매장담당자 부서변경",
      "직원 계정 정지 처리",
      "직원 비밀번호 초기화",
      "전체 재고 현황 조회",
  };

  private final UiServiceStore store;
  private final Consumer<String> logger;

  public SystemManagerPanel(UiServiceStore store, Consumer<String> logger) {
    this.store = store;
    this.logger = logger;
  }

  public Map<String, Supplier<JPanel>> views() {
    Map<String, Supplier<JPanel>> views = new LinkedHashMap<>();
    views.put(MENUS[0], this::employeeListPanel);
    views.put(MENUS[1], this::employeeCreatePanel);
    views.put(MENUS[2], this::roleChangePanel);
    views.put(MENUS[3], this::storeManagerTransferPanel);
    views.put(MENUS[4], this::deactivatePanel);
    views.put(MENUS[5], this::passwordResetPanel);
    views.put(MENUS[6], () -> inventoryPanel("전체 재고 현황 조회", "전체"));
    return views;
  }

  private JPanel employeeListPanel() {
    JPanel panel = page("직원 계정 목록");
    DefaultTableModel model = employeeModel();
    JTable table = UiTableFactory.table(model);
    JButton refresh = new JButton("새로고침");
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillEmployees(model)));
    panel.add(toolbar(refresh), BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillEmployees(model));
    return panel;
  }

  private JPanel employeeCreatePanel() {
    JPanel panel = page("직원 계정 생성 폼");
    JTextField loginIdField = new JTextField(14);
    JTextField nameField = new JTextField(14);
    JComboBox<BranchDTO> branchBox = branchBox();
    JComboBox<StoreDTO> storeBox = emptyStoreBox();
    JComboBox<RoleType> roleBox = createRoleBox();
    JLabel storeLabel = new JLabel("매장");
    JButton create = new JButton("직원 생성");
    JPanel form = formPanel();

    form.add(new JLabel("로그인ID"));
    form.add(loginIdField);
    form.add(new JLabel("직원명"));
    form.add(nameField);
    form.add(new JLabel("권한"));
    form.add(roleBox);
    form.add(new JLabel("지점"));
    form.add(branchBox);
    form.add(storeLabel);
    form.add(storeBox);
    form.add(new JLabel());
    form.add(create);

    branchBox.addActionListener(event -> UiExceptionHandler.run(logger, () ->
        fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem())));
    roleBox.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      updateStoreSelectorVisibility((RoleType) roleBox.getSelectedItem(), storeLabel, storeBox);
      fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem());
    }));
    UiExceptionHandler.run(logger, () ->
        fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem()));
    updateStoreSelectorVisibility((RoleType) roleBox.getSelectedItem(), storeLabel, storeBox);

    create.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        RoleType role = (RoleType) roleBox.getSelectedItem();
        if (role == RoleType.SYSTEM_MANAGER) {
          throw new InputException("시스템 관리자는 생성할 수 없습니다.");
        }
        BranchDTO branch = (BranchDTO) branchBox.getSelectedItem();
        Long branchId = requiresBranch(role) ? selectedBranchId(branch) : null;
        Long storeId = requiresStore(role) ? selectedStoreId((StoreDTO) storeBox.getSelectedItem())
            : null;
        EmployeeDTO employee = store.createEmployee(required(loginIdField.getText(), "로그인ID"),
            required(nameField.getText(), "직원명"), role, branchId, storeId);
        showCreatedEmployeeDialog(panel, employee);
        logger.accept("직원 계정 생성 완료: " + employee.getEmployeeName());
    }));

    panel.add(form, BorderLayout.NORTH);
    return panel;
  }

  private JPanel roleChangePanel() {
    JPanel panel = page("직원 권한 변경 폼");
    DefaultTableModel model = employeeModel();
    JTable table = UiTableFactory.table(model);
    JComboBox<RoleType> roleBox = changeRoleBox();
    JComboBox<BranchDTO> branchBox = branchBox();
    JComboBox<StoreDTO> storeBox = emptyStoreBox();
    JLabel storeLabel = new JLabel("매장");
    JButton change = new JButton("권한 변경");
    JButton refresh = new JButton("새로고침");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("변경 권한"));
    controls.add(roleBox);
    controls.add(new JLabel("지점"));
    controls.add(branchBox);
    controls.add(storeLabel);
    controls.add(storeBox);
    controls.add(change);
    controls.add(refresh);

    branchBox.addActionListener(event -> UiExceptionHandler.run(logger, () ->
        fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem())));
    roleBox.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      updateStoreSelectorVisibility((RoleType) roleBox.getSelectedItem(), storeLabel, storeBox);
      fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem());
    }));
    UiExceptionHandler.run(logger, () ->
        fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem()));
    updateStoreSelectorVisibility((RoleType) roleBox.getSelectedItem(), storeLabel, storeBox);

    change.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        long employeeId = selectedEmployeeId(table);
        RoleType role = (RoleType) roleBox.getSelectedItem();
        BranchDTO branch = (BranchDTO) branchBox.getSelectedItem();
        Long branchId = requiresBranch(role) ? selectedBranchId(branch) : null;
        Long storeId = requiresStore(role) ? selectedStoreId((StoreDTO) storeBox.getSelectedItem())
            : null;
        EmployeeDTO target = null;
        for (EmployeeDTO employee : store.employees()) {
          if (employee.getEmployeeId() == employeeId) {
            target = employee;
            break;
          }
        }
        if (target == null) {
          throw new NotFoundException("직원을 찾을 수 없습니다.");
        }
        store.changeEmployeeRole(employeeId, role, branchId, storeId);
        fillStaffEmployees(model);
        logger.accept("직원 권한 변경 완료: " + target.getEmployeeName());
    }));
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      fillBranchOptions(branchBox);
      fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem());
      fillStaffEmployees(model);
    }));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillStaffEmployees(model));
    return panel;
  }

  private JPanel deactivatePanel() {
    JPanel panel = page("직원 계정 정지 처리");
    DefaultTableModel model = employeeModel();
    JTable table = UiTableFactory.table(model);
    JButton deactivate = new JButton("계정 정지");
    JButton refresh = new JButton("새로고침");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(deactivate);
    controls.add(refresh);

    deactivate.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        long employeeId = selectedEmployeeId(table);
        store.deactivateEmployee(employeeId);
        fillEmployees(model);
        logger.accept("직원 계정 정지 완료: " + employeeId);
    }));
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillEmployees(model)));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillEmployees(model));
    return panel;
  }

  private JPanel passwordResetPanel() {
    JPanel panel = page("직원 비밀번호 초기화");
    DefaultTableModel model = employeeModel();
    JTable table = UiTableFactory.table(model);
    JButton reset = new JButton("비밀번호 초기화");
    JButton refresh = new JButton("새로고침");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("초기 비밀번호: " + RESET_PASSWORD));
    controls.add(reset);
    controls.add(refresh);

    reset.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      long employeeId = selectedEmployeeId(table);
      store.resetEmployeePassword(employeeId, RESET_PASSWORD);
      fillEmployees(model);
      logger.accept("직원 비밀번호 초기화 완료: " + employeeId);
    }));
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillEmployees(model)));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillEmployees(model));
    return panel;
  }

  private JPanel storeManagerTransferPanel() {
    JPanel panel = page("매장담당자 부서변경");
    DefaultTableModel model = employeeModel();
    JTable table = UiTableFactory.table(model);
    JComboBox<BranchDTO> branchBox = branchBox();
    JComboBox<StoreDTO> storeBox = emptyStoreBox();
    JButton change = new JButton("소속 매장 변경");
    JButton refresh = new JButton("새로고침");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("변경 지점"));
    controls.add(branchBox);
    controls.add(new JLabel("변경 매장"));
    controls.add(storeBox);
    controls.add(change);
    controls.add(refresh);

    branchBox.addActionListener(event -> UiExceptionHandler.run(logger, () ->
        fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem())));
    UiExceptionHandler.run(logger, () ->
        fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem()));

    change.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      long employeeId = selectedEmployeeId(table);
      selectedBranchId((BranchDTO) branchBox.getSelectedItem());
      StoreDTO selectedStore = (StoreDTO) storeBox.getSelectedItem();
      if (selectedStore == null) {
        throw new InputException("변경할 매장을 선택해 주세요.");
      }
      store.changeStoreManagerStore(employeeId, selectedStore.getStoreId());
      fillStoreManagers(model);
      logger.accept("매장담당자 소속 매장 변경 완료: " + employeeId + " -> "
          + selectedStore.getStoreName());
    }));
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
      fillBranchOptions(branchBox);
      fillStoreOptionsByBranch(storeBox, (BranchDTO) branchBox.getSelectedItem());
      fillStoreManagers(model);
    }));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillStoreManagers(model));
    return panel;
  }

  private JPanel inventoryPanel(String title, String defaultFilterType) {
    JPanel panel = page(title);
    DefaultTableModel model = UiTableFactory.model("매장", "상품ID", "상품명", "브랜드", "카테고리",
        "현재수량", "안전재고", "부족여부");
    JTable table = UiTableFactory.table(model);
    JComboBox<String> filterBox = inventoryFilterBox(defaultFilterType);
    JTextField keywordField = new JTextField(16);
    JLabel keywordLabel = new JLabel("검색어");
    JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    CategorySelector categorySelector = new CategorySelector(categoryPanel);
    JButton search = new JButton("조회");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("필터"));
    controls.add(filterBox);
    controls.add(keywordLabel);
    controls.add(keywordField);
    controls.add(categoryPanel);
    controls.add(search);
    filterBox.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> updateInventoryFilterControls(filterBox, keywordLabel, keywordField, categoryPanel,
            categorySelector)));
    search.addActionListener(event -> UiExceptionHandler.run(logger,
        () -> {
          String filterType = String.valueOf(filterBox.getSelectedItem());
          if ("카테고리".equals(filterType)) {
            fillInventory(model, filterType, null, categorySelector.selectedCategoryIds());
            return;
          }
          fillInventory(model, filterType, keywordField.getText(), null);
        }));
    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> {
      updateInventoryFilterControls(filterBox, keywordLabel, keywordField, categoryPanel,
          categorySelector);
      fillInventory(model, defaultFilterType, "", null);
    });
    return panel;
  }

  private DefaultTableModel employeeModel() {
    return UiTableFactory.model("직원ID", "로그인ID", "직원명", "권한", "지점명", "매장명", "사용여부");
  }

  private void fillEmployees(DefaultTableModel model) throws Exception {
    model.setRowCount(0);
    for (EmployeeDTO employee : store.employees()) {
      if (employee.getRoleId() == RoleType.SYSTEM_MANAGER.getRoleId()) {
        continue;
      }
      addEmployeeRow(model, employee);
    }
  }

  private void fillStoreManagers(DefaultTableModel model) throws Exception {
    model.setRowCount(0);
    for (EmployeeDTO employee : store.employees()) {
      if (employee.getRoleId() == RoleType.STORE_MANAGER.getRoleId()) {
        addEmployeeRow(model, employee);
      }
    }
  }

  private void fillStaffEmployees(DefaultTableModel model) throws Exception {
    model.setRowCount(0);
    for (EmployeeDTO employee : store.employees()) {
      if (employee.getRoleId() == RoleType.STAFF.getRoleId()) {
        addEmployeeRow(model, employee);
      }
    }
  }

  private void addEmployeeRow(DefaultTableModel model, EmployeeDTO employee) throws Exception {
    model.addRow(new Object[]{
        employee.getEmployeeId(),
        employee.getLoginId(),
        employee.getEmployeeName(),
        store.findRoleName(employee.getRoleId()),
        employee.getBranchName() == null ? "-" : employee.getBranchName(),
        employee.getStoreId() == null ? "-" : store.findStoreName(employee.getStoreId()),
        employee.getIsActive()
    });
  }

  private void fillInventory(DefaultTableModel model, String filterType, String keyword,
      Set<Long> categoryIds) throws Exception {
    model.setRowCount(0);
    String normalized = keyword == null ? "" : keyword.trim();
    for (InventoryDTO inventory : store.inventories()) {
      if (!matches(inventory, filterType, normalized, categoryIds)) {
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

  private boolean matches(InventoryDTO inventory, String filterType, String keyword,
      Set<Long> categoryIds)
      throws Exception {
    if ("카테고리".equals(filterType)) {
      return categoryIds == null || categoryIds.isEmpty()
          || inventory.getCategoryId() != null && categoryIds.contains(inventory.getCategoryId());
    }
    if (keyword.isEmpty() || "전체".equals(filterType)) {
      return true;
    }
    if ("지점".equals(filterType)) {
      return contains(inventory.getBranchName(), keyword);
    }
    if ("매장".equals(filterType)) {
      return contains(store.findStoreName(inventory.getStoreId()), keyword);
    }
    if ("브랜드".equals(filterType)) {
      return contains(inventory.getBrandName(), keyword);
    }
    if ("상품".equals(filterType)) {
      return contains(inventory.getProductName(), keyword);
    }
    return true;
  }

  private boolean contains(String value, String keyword) {
    return value != null && value.contains(keyword);
  }

  private JComboBox<String> inventoryFilterBox(String defaultFilterType) {
    JComboBox<String> filterBox = new JComboBox<String>(new String[]{
        "전체",
        "지점",
        "매장",
        "브랜드",
        "카테고리",
        "상품"
    });
    filterBox.setSelectedItem(defaultFilterType);
    return filterBox;
  }

  private void updateInventoryFilterControls(JComboBox<String> filterBox, JLabel keywordLabel,
      JTextField keywordField, JPanel categoryPanel, CategorySelector categorySelector)
      throws Exception {
    boolean categoryFilter = "카테고리".equals(String.valueOf(filterBox.getSelectedItem()));
    keywordLabel.setVisible(!categoryFilter);
    keywordField.setVisible(!categoryFilter);
    categoryPanel.setVisible(categoryFilter);
    if (categoryFilter && categoryPanel.getComponentCount() == 0) {
      categorySelector.load(store.categories());
    }
    categoryPanel.revalidate();
    categoryPanel.repaint();
  }

  private JComboBox<RoleType> roleBox() {
    JComboBox<RoleType> roleBox = new JComboBox<RoleType>(new RoleType[]{
        RoleType.BRANCH_MANAGER,
        RoleType.SUPPLIER_MANAGER,
        RoleType.STORE_MANAGER,
        RoleType.SYSTEM_MANAGER,
        RoleType.STAFF
    });
    roleBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof RoleType) {
          RoleType roleType = (RoleType) value;
          setText(roleType.getRoleName());
        }
        return this;
      }
    });
    return roleBox;
  }

  private JComboBox<RoleType> createRoleBox() {
    JComboBox<RoleType> roleBox = new JComboBox<RoleType>(new RoleType[]{
        RoleType.BRANCH_MANAGER,
        RoleType.SUPPLIER_MANAGER,
        RoleType.STORE_MANAGER,
        RoleType.STAFF
    });
    roleBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof RoleType) {
          RoleType roleType = (RoleType) value;
          setText(roleType.getRoleName());
        }
        return this;
      }
    });
    return roleBox;
  }

  private JComboBox<RoleType> changeRoleBox() {
    JComboBox<RoleType> roleBox = new JComboBox<RoleType>(new RoleType[]{
        RoleType.BRANCH_MANAGER,
        RoleType.SUPPLIER_MANAGER,
        RoleType.STORE_MANAGER
    });
    roleBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof RoleType) {
          RoleType roleType = (RoleType) value;
          setText(roleType.getRoleName());
        }
        return this;
      }
    });
    return roleBox;
  }

  private JComboBox<BranchDTO> branchBox() {
    final JComboBox<BranchDTO> branchBox = new JComboBox<BranchDTO>();
    branchBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof BranchDTO) {
          BranchDTO branchDTO = (BranchDTO) value;
          setText(branchDTO.getBranchId() + ". " + branchDTO.getBranchName());
        }
        return this;
      }
    });
    UiExceptionHandler.run(logger, new UiExceptionHandler.UiAction() {
      @Override
      public void run() throws Exception {
        fillBranchOptions(branchBox);
      }
    });
    return branchBox;
  }

  private void fillBranchOptions(JComboBox<BranchDTO> branchBox) throws Exception {
    branchBox.removeAllItems();
    for (BranchDTO branchDTO : store.branches()) {
      branchBox.addItem(branchDTO);
    }
  }

  private JComboBox<StoreDTO> storeBox() {
    final JComboBox<StoreDTO> storeBox = emptyStoreBox();
    UiExceptionHandler.run(logger, new UiExceptionHandler.UiAction() {
      @Override
      public void run() throws Exception {
        fillStoreOptions(storeBox);
      }
    });
    return storeBox;
  }

  private JComboBox<StoreDTO> emptyStoreBox() {
    JComboBox<StoreDTO> storeBox = new JComboBox<StoreDTO>();
    storeBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof StoreDTO) {
          StoreDTO storeDTO = (StoreDTO) value;
          setText(storeDTO.getStoreId() + ". " + storeDTO.getStoreName());
        }
        return this;
      }
    });
    return storeBox;
  }

  private void fillStoreOptions(JComboBox<StoreDTO> storeBox) throws Exception {
    storeBox.removeAllItems();
    for (StoreDTO storeDTO : store.stores()) {
      storeBox.addItem(storeDTO);
    }
  }

  private void fillStoreOptionsByBranch(JComboBox<StoreDTO> storeBox, BranchDTO branch)
      throws Exception {
    storeBox.removeAllItems();
    if (branch == null) {
      return;
    }
    for (StoreDTO storeDTO : store.stores()) {
      if (storeDTO.getBranchId() == branch.getBranchId()) {
        storeBox.addItem(storeDTO);
      }
    }
  }

  private void updateStoreSelectorVisibility(RoleType role, JLabel storeLabel,
      JComboBox<StoreDTO> storeBox) {
    boolean visible = requiresStore(role);
    storeLabel.setVisible(visible);
    storeBox.setVisible(visible);
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

  private void showCreatedEmployeeDialog(JPanel parent, EmployeeDTO employee) {
    JOptionPane.showMessageDialog(
        parent,
        "직원 계정이 생성되었습니다.\n"
            + "로그인 ID: " + employee.getLoginId() + "\n"
            + "초기 비밀번호: " + INITIAL_PASSWORD,
        "직원 계정 생성 완료",
        JOptionPane.INFORMATION_MESSAGE
    );
  }

  private JPanel formPanel() {
    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    panel.setOpaque(false);
    return panel;
  }

  private long selectedEmployeeId(JTable table) {
    int row = table.getSelectedRow();
    if (row < 0) {
      throw new InputException("직원을 선택해 주세요.");
    }
    return ((Number) table.getValueAt(row, 0)).longValue();
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

  private boolean requiresBranch(RoleType role) {
    return role == RoleType.STAFF
        || role == RoleType.BRANCH_MANAGER
        || role == RoleType.SUPPLIER_MANAGER
        || role == RoleType.STORE_MANAGER;
  }

  private boolean requiresStore(RoleType role) {
    return role == RoleType.STORE_MANAGER;
  }

  private Long selectedBranchId(BranchDTO branch) {
    if (branch == null) {
      throw new InputException("지점을 선택해 주세요.");
    }
    return branch.getBranchId();
  }

  private Long selectedStoreId(StoreDTO storeDTO) {
    if (storeDTO == null) {
      throw new InputException("매장을 선택해 주세요.");
    }
    return storeDTO.getStoreId();
  }

  private String required(String value, String label) {
    String text = value == null ? "" : value.trim();
    if (text.isEmpty()) {
      throw new InputException(label + "을 입력해 주세요.");
    }
    return text;
  }

}
