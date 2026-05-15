package ui;

import common.type.RoleType;
import employee.EmployeeDTO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import ui.common.UiConstants;
import ui.common.UiExceptionHandler;
import ui.data.UiServiceStore;

public class LoginPanel extends JPanel {

  private final UiServiceStore store;
  private final Consumer<EmployeeDTO> onLogin;
  private final JTextField loginIdField = new JTextField(18);
  private final JPasswordField passwordField = new JPasswordField(18);
  private final JButton loginButton = new JButton("로그인");

  public LoginPanel(UiServiceStore store, Consumer<EmployeeDTO> onLogin) {
    this.store = store;
    this.onLogin = onLogin;
    setLayout(new BorderLayout());
    setBackground(UiConstants.BACKGROUND);
    add(title(), BorderLayout.NORTH);
    add(form(), BorderLayout.CENTER);
    javax.swing.SwingUtilities.invokeLater(
        () -> getRootPane().setDefaultButton(loginButton));
  }

  private JPanel title() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 48));
    panel.setOpaque(false);
    JLabel label = new JLabel("현대백화점 브랜드 매장 재고 및 입출고 관리 시스템");
    label.setFont(UiConstants.TITLE_FONT);
    panel.add(label);
    return panel;
  }

  private JPanel form() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(8, 8, 8, 8);
    c.anchor = GridBagConstraints.WEST;

    addRow(panel, c, 0, "로그인 ID", loginIdField);
    addRow(panel, c, 1, "비밀번호", passwordField);

    loginButton.addActionListener(event -> login());
    c.gridx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(loginButton, c);

    return panel;
  }

  private void addRow(JPanel panel, GridBagConstraints c, int row, String label, java.awt.Component field) {
    c.gridx = 0;
    c.gridy = row;
    c.fill = GridBagConstraints.NONE;
    JLabel text = new JLabel(label);
    text.setFont(UiConstants.HEADER_FONT);
    panel.add(text, c);

    c.gridx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(field, c);
  }

  private void login() {
    String loginId = loginIdField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();
    if (loginId.isEmpty() || password.isEmpty()) {
      JOptionPane.showMessageDialog(this, "로그인 ID와 비밀번호를 입력해 주세요.");
      return;
    }
    try {
      EmployeeDTO user = store.authenticate(loginId, password);
      if (RoleType.fromRoleId(user.getRoleId()) == RoleType.STAFF) {
        JOptionPane.showMessageDialog(this, "권한이 없습니다 시스템 관리자에게 문의해 주세요");
        return;
      }
      onLogin.accept(user);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, UiExceptionHandler.messageFor(e));
    }
  }
}
