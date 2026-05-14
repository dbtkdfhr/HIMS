package ui;

import common.type.RoleType;
import employee.EmployeeDTO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import ui.common.UiConstants;
import ui.data.MockDataStore;

public class DashboardPanel extends JPanel {

  private final MockDataStore store;
  private final EmployeeDTO user;
  private final Runnable onLogout;
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel contentPanel = new JPanel(cardLayout);
  private final JTextArea logArea = new JTextArea(5, 20);
  private final Map<String, JPanel> views = new LinkedHashMap<>();

  public DashboardPanel(MockDataStore store, EmployeeDTO user, Runnable onLogout) {
    this.store = store;
    this.user = user;
    this.onLogout = onLogout;
    setLayout(new BorderLayout());
    setBackground(UiConstants.BACKGROUND);
    add(header(), BorderLayout.NORTH);
    add(menu(), BorderLayout.WEST);
    add(contentPanel, BorderLayout.CENTER);
    add(log(), BorderLayout.SOUTH);
    installViews();
  }

  public void writeLog(String text) {
    logArea.append(text + "\n");
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }

  private JPanel header() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

    JLabel title = new JLabel("HIMS 재고 및 입출고 관리");
    title.setFont(UiConstants.TITLE_FONT);
    panel.add(title, BorderLayout.WEST);

    String roleName = store.findRoleName(user.getRoleId());
    JLabel info = new JLabel(user.getEmployeeName() + " | " + roleName);
    info.setFont(UiConstants.HEADER_FONT);
    panel.add(info, BorderLayout.EAST);
    return panel;
  }

  private JPanel menu() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(230, 0));
    panel.setBackground(UiConstants.PANEL_BACKGROUND);
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 8));
    buttons.setOpaque(false);
    for (String name : menuNames()) {
      JButton button = new JButton(name);
      button.addActionListener(event -> show(name));
      buttons.add(button);
    }

    JButton logout = new JButton("로그아웃");
    logout.addActionListener(event -> onLogout.run());

    panel.add(buttons, BorderLayout.NORTH);
    panel.add(logout, BorderLayout.SOUTH);
    return panel;
  }

  private JScrollPane log() {
    logArea.setEditable(false);
    logArea.setFont(UiConstants.DEFAULT_FONT);
    return new JScrollPane(logArea);
  }

  private void installViews() {
    RoleType role = RoleType.fromRoleId(user.getRoleId());
    if (role == RoleType.STORE_MANAGER || role == RoleType.STAFF) {
      StoreManagerPanel panel = new StoreManagerPanel(store, user, this::writeLog);
      views.putAll(panel.views());
    } else if (role == RoleType.SUPPLIER_MANAGER) {
      VendorManagerPanel panel = new VendorManagerPanel(store, user, this::writeLog);
      views.putAll(panel.views());
      ExternalSupplierPanel externalPanel = new ExternalSupplierPanel(store, this::writeLog);
      views.putAll(externalPanel.views());
    } else if (role == RoleType.BRANCH_MANAGER) {
      BranchManagerPanel panel = new BranchManagerPanel(store, this::writeLog);
      views.putAll(panel.views());
    } else if (role == RoleType.SYSTEM_MANAGER) {
      SystemManagerPanel panel = new SystemManagerPanel(store, this::writeLog);
      views.putAll(panel.views());
    }

    for (Map.Entry<String, JPanel> entry : views.entrySet()) {
      contentPanel.add(entry.getValue(), entry.getKey());
    }
    if (!views.isEmpty()) {
      show(views.keySet().iterator().next());
    }
  }

  private void show(String name) {
    if (views.containsKey(name)) {
      cardLayout.show(contentPanel, name);
      writeLog("메뉴 이동: " + name);
    }
  }

  private String[] menuNames() {
    RoleType role = RoleType.fromRoleId(user.getRoleId());
    if (role == RoleType.STORE_MANAGER || role == RoleType.STAFF) {
      return StoreManagerPanel.MENUS;
    }
    if (role == RoleType.SUPPLIER_MANAGER) {
      return supplierManagerMenus();
    }
    if (role == RoleType.BRANCH_MANAGER) {
      return BranchManagerPanel.MENUS;
    }
    if (role == RoleType.SYSTEM_MANAGER) {
      return SystemManagerPanel.MENUS;
    }
    return new String[0];
  }

  private String[] supplierManagerMenus() {
    String[] menus = new String[VendorManagerPanel.MENUS.length
        + ExternalSupplierPanel.MENUS.length];
    System.arraycopy(VendorManagerPanel.MENUS, 0, menus, 0, VendorManagerPanel.MENUS.length);
    System.arraycopy(ExternalSupplierPanel.MENUS, 0, menus, VendorManagerPanel.MENUS.length,
        ExternalSupplierPanel.MENUS.length);
    return menus;
  }
}
