package ui;

import employee.EmployeeDTO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import ui.data.MockDataStore;

public class HimsApplication {

  private final JFrame frame;
  private final MockDataStore store;

  public HimsApplication() {
    this.store = new MockDataStore();
    this.frame = new JFrame("HIMS 재고 및 입출고 관리 시스템");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1240, 820);
    frame.setLocationRelativeTo(null);
  }

  public void start() {
    showLogin();
    frame.setVisible(true);
  }

  public void showLogin() {
    setContent(new LoginPanel(store, this::showDashboard));
  }

  public void showDashboard(EmployeeDTO user) {
    setContent(new DashboardPanel(store, user, this::showLogin));
  }

  private void setContent(JPanel panel) {
    frame.setContentPane(panel);
    frame.revalidate();
    frame.repaint();
  }

  public static void launch() {
    SwingUtilities.invokeLater(() -> new HimsApplication().start());
  }
}
