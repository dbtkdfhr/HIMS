package common;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class ResultSetUtils {

  private ResultSetUtils() {
  }

  public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
    ResultSetMetaData metaData = rs.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
        return true;
      }
    }
    return false;
  }
}
