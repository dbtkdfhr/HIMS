package ui;

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
import ui.data.UiServiceStore;

public class ExternalSupplierDashboardPanel extends JPanel {

  private final CardLayout cardLayout = new CardLayout();
  private final JPanel contentPanel = new JPanel(cardLayout);
  private final JTextArea logArea = new JTextArea(5, 20);
  private final Map<String, JPanel> views = new LinkedHashMap<>();

  public ExternalSupplierDashboardPanel(UiServiceStore store) {
    setLayout(new BorderLayout());
    setBackground(UiConstants.BACKGROUND);
    add(header(), BorderLayout.NORTH);
    add(menu(), BorderLayout.WEST);
    add(contentPanel, BorderLayout.CENTER);
    add(log(), BorderLayout.SOUTH);
    ExternalSupplierPanel panel = new ExternalSupplierPanel(store, this::writeLog);
    views.putAll(panel.views());
    installViews();
  }

  private JPanel header() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
    JLabel title = new JLabel("외부 발주처 시스템");
    title.setFont(UiConstants.TITLE_FONT);
    panel.add(title, BorderLayout.WEST);
    JLabel info = new JLabel("외부 발주처");
    info.setFont(UiConstants.HEADER_FONT);
    panel.add(info, BorderLayout.EAST);
    return panel;
  }

  private JPanel menu() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(220, 0));
    panel.setBackground(UiConstants.PANEL_BACKGROUND);
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 8));
    buttons.setOpaque(false);
    for (String name : ExternalSupplierPanel.MENUS) {
      JButton button = new JButton(name);
      button.addActionListener(event -> show(name));
      buttons.add(button);
    }
    panel.add(buttons, BorderLayout.NORTH);
    return panel;
  }

  private JScrollPane log() {
    logArea.setEditable(false);
    logArea.setFont(UiConstants.DEFAULT_FONT);
    return new JScrollPane(logArea);
  }

  private void installViews() {
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

  private void writeLog(String text) {
    logArea.append(text + "\n");
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }
}
