package ui;

import common.type.RoleType;
import employee.EmployeeDTO;
import exception.InputException;
import exception.NotFoundException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.common.UiTableFactory;
import ui.data.MockDataStore;

public class SystemManagerPanel {

  public static final String[] MENUS = {
      "직원 계정 목록",
      "직원 계정 생성 폼",
      "직원 권한 변경 폼",
      "직원 계정 정지 처리"
  };

  private final MockDataStore store;
  private final Consumer<String> logger;

  public SystemManagerPanel(MockDataStore store, Consumer<String> logger) {
    this.store = store;
    this.logger = logger;
  }

  public Map<String, JPanel> views() {
    Map<String, JPanel> views = new LinkedHashMap<>();
    views.put(MENUS[0], employeeListPanel());
    views.put(MENUS[1], employeeCreatePanel());
    views.put(MENUS[2], roleChangePanel());
    views.put(MENUS[3], deactivatePanel());
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
    JTextField storeIdField = new JTextField(8);
    JComboBox<RoleType> roleBox = roleBox();
    JButton create = new JButton("직원 생성");
    JPanel form = formPanel();

    form.add(new JLabel("로그인ID"));
    form.add(loginIdField);
    form.add(new JLabel("직원명"));
    form.add(nameField);
    form.add(new JLabel("권한"));
    form.add(roleBox);
    form.add(new JLabel("매장ID"));
    form.add(storeIdField);
    form.add(new JLabel());
    form.add(create);

    create.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        RoleType role = (RoleType) roleBox.getSelectedItem();
        Long storeId = storeIdField.getText().trim().isEmpty() ? null
            : parseLong(storeIdField.getText(), "매장ID");
        EmployeeDTO employee = store.createEmployee(required(loginIdField.getText(), "로그인ID"),
            required(nameField.getText(), "직원명"), role, storeId);
        logger.accept("직원 계정 생성 완료: " + employee.getEmployeeName());
    }));

    panel.add(form, BorderLayout.NORTH);
    return panel;
  }

  private JPanel roleChangePanel() {
    JPanel panel = page("직원 권한 변경 폼");
    DefaultTableModel model = employeeModel();
    JTable table = UiTableFactory.table(model);
    JComboBox<RoleType> roleBox = roleBox();
    JButton change = new JButton("권한 변경");
    JButton refresh = new JButton("새로고침");
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
    controls.add(new JLabel("변경 권한"));
    controls.add(roleBox);
    controls.add(change);
    controls.add(refresh);

    change.addActionListener(event -> UiExceptionHandler.run(logger, () -> {
        long employeeId = selectedEmployeeId(table);
        RoleType role = (RoleType) roleBox.getSelectedItem();
        for (EmployeeDTO employee : store.employees()) {
          if (employee.getEmployeeId() == employeeId) {
            employee.setRoleId(role.getRoleId());
            fillEmployees(model);
            logger.accept("직원 권한 변경 완료: " + employee.getEmployeeName());
            return;
          }
        }
        throw new NotFoundException("직원을 찾을 수 없습니다.");
    }));
    refresh.addActionListener(event -> UiExceptionHandler.run(logger, () -> fillEmployees(model)));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(UiTableFactory.scroll(table), BorderLayout.CENTER);
    UiExceptionHandler.run(logger, () -> fillEmployees(model));
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

  private DefaultTableModel employeeModel() {
    return UiTableFactory.model("직원ID", "로그인ID", "직원명", "권한", "매장ID", "사용여부");
  }

  private void fillEmployees(DefaultTableModel model) {
    model.setRowCount(0);
    for (EmployeeDTO employee : store.employees()) {
      model.addRow(new Object[]{
          employee.getEmployeeId(),
          employee.getLoginId(),
          employee.getEmployeeName(),
          store.findRoleName(employee.getRoleId()),
          employee.getStoreId() == null ? "-" : employee.getStoreId(),
          employee.getIsActive()
      });
    }
  }

  private JComboBox<RoleType> roleBox() {
    return new JComboBox<>(new RoleType[]{
        RoleType.BRANCH_MANAGER,
        RoleType.SUPPLIER_MANAGER,
        RoleType.STORE_MANAGER,
        RoleType.SYSTEM_MANAGER,
        RoleType.STAFF
    });
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

  private String required(String value, String label) {
    String text = value == null ? "" : value.trim();
    if (text.isEmpty()) {
      throw new InputException(label + "을 입력해 주세요.");
    }
    return text;
  }

}
