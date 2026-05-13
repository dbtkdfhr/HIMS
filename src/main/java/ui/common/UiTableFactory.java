package ui.common;

import java.awt.Component;
import java.util.function.IntPredicate;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class UiTableFactory {

  private UiTableFactory() {
  }

  public static DefaultTableModel model(String... columns) {
    return new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
  }

  public static JTable table(DefaultTableModel model) {
    JTable table = new JTable(model);
    table.setFont(UiConstants.DEFAULT_FONT);
    table.setRowHeight(28);
    table.getTableHeader().setFont(UiConstants.HEADER_FONT);
    table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    return table;
  }

  public static JScrollPane scroll(JTable table) {
    return new JScrollPane(table);
  }

  public static void applyRowHighlight(JTable table, IntPredicate predicate) {
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
          boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected,
            hasFocus, row, column);
        if (!isSelected) {
          int modelRow = table.convertRowIndexToModel(row);
          component.setBackground(predicate.test(modelRow) ? UiConstants.LOW_STOCK
              : UiConstants.PANEL_BACKGROUND);
        }
        return component;
      }
    });
  }
}
