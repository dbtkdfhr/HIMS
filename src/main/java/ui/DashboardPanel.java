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
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import ui.common.UiConstants;
import ui.data.UiServiceStore;

public class DashboardPanel extends JPanel {

  private final UiServiceStore store;
  private final EmployeeDTO user;
  private final Runnable onLogout;
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel contentPanel = new JPanel(cardLayout);
  private final javax.swing.JTextPane logArea = new javax.swing.JTextPane();
  private final Map<String, Supplier<JPanel>> viewFactories = new LinkedHashMap<>();
  private final Map<String, JPanel> loadedViews = new LinkedHashMap<>();

  public DashboardPanel(UiServiceStore store, EmployeeDTO user, Runnable onLogout) {
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
    try {
      javax.swing.text.Document doc = logArea.getDocument();
      javax.swing.text.SimpleAttributeSet attributes = new javax.swing.text.SimpleAttributeSet();
      if (text.startsWith("오류:")) {
        javax.swing.text.StyleConstants.setForeground(attributes, Color.RED);
      } else {
        javax.swing.text.StyleConstants.setForeground(attributes, Color.BLACK);
      }
      doc.insertString(doc.getLength(), text + "\n", attributes);
      logArea.setCaretPosition(doc.getLength());
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    addMenuButtons(buttons);

    JButton logout = new JButton("로그아웃");
    logout.addActionListener(event -> onLogout.run());

    panel.add(buttons, BorderLayout.NORTH);
    panel.add(logout, BorderLayout.SOUTH);
    return panel;
  }

  private void addMenuButtons(JPanel buttons) {
    RoleType role = RoleType.fromRoleId(user.getRoleId());
    if (role == RoleType.SYSTEM_MANAGER) {
      addSectionLabel(buttons, "직원 관리");
      for (int i = 0; i <= 5; i++) {
        addMenuButton(buttons, SystemManagerPanel.MENUS[i]);
      }

      addSectionLabel(buttons, "재고 관리");
      for (int i = 6; i < SystemManagerPanel.MENUS.length; i++) {
        addMenuButton(buttons, SystemManagerPanel.MENUS[i]);
      }
      return;
    }

    for (String name : menuNames()) {
      addMenuButton(buttons, name);
    }
  }

  private void addSectionLabel(JPanel buttons, String title) {
    JLabel label = new JLabel(title);
    label.setFont(UiConstants.HEADER_FONT);
    label.setForeground(Color.DARK_GRAY);
    label.setBorder(BorderFactory.createEmptyBorder(10, 4, 0, 4));
    buttons.add(label);
  }

  private void addMenuButton(JPanel buttons, String name) {
    JButton button = new JButton(name);
    button.addActionListener(event -> show(name));
    buttons.add(button);
  }

  private JScrollPane log() {
    logArea.setEditable(false);
    logArea.setFont(UiConstants.DEFAULT_FONT);
    JScrollPane scroll = new JScrollPane(logArea);
    scroll.setPreferredSize(new Dimension(0, 100));
    return scroll;
  }

  private void installViews() {
    RoleType role = RoleType.fromRoleId(user.getRoleId());
    if (role == RoleType.STORE_MANAGER) {
      StoreManagerPanel panel = new StoreManagerPanel(store, user, this::writeLog);
      viewFactories.putAll(panel.views());
    } else if (role == RoleType.SUPPLIER_MANAGER) {
      VendorManagerPanel panel = new VendorManagerPanel(store, user, this::writeLog);
      viewFactories.putAll(panel.views());
    } else if (role == RoleType.BRANCH_MANAGER) {
      BranchManagerPanel panel = new BranchManagerPanel(store, user, this::writeLog);
      viewFactories.putAll(panel.views());
    } else if (role == RoleType.SYSTEM_MANAGER) {
      SystemManagerPanel panel = new SystemManagerPanel(store, this::writeLog);
      viewFactories.putAll(panel.views());
    }

    if (!viewFactories.isEmpty()) {
      show(viewFactories.keySet().iterator().next());
    }
  }

  private void show(String name) {
    if (!viewFactories.containsKey(name)) {
      return;
    }
    if (!loadedViews.containsKey(name)) {
      JPanel view = viewFactories.get(name).get();
      loadedViews.put(name, view);
      contentPanel.add(view, name);
    }
    cardLayout.show(contentPanel, name);
    writeLog("메뉴 이동: " + name);
  }

  private String[] menuNames() {
    RoleType role = RoleType.fromRoleId(user.getRoleId());
    if (role == RoleType.STORE_MANAGER) {
      return StoreManagerPanel.MENUS;
    }
    if (role == RoleType.SUPPLIER_MANAGER) {
      return VendorManagerPanel.MENUS;
    }
    if (role == RoleType.BRANCH_MANAGER) {
      return BranchManagerPanel.MENUS;
    }
    if (role == RoleType.SYSTEM_MANAGER) {
      return SystemManagerPanel.MENUS;
    }
    return new String[0];
  }
}
