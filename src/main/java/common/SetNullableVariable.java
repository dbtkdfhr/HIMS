package common;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetNullableVariable {
  public static void setNullableLong(PreparedStatement pstmt, int parameterIndex, Long value)
      throws SQLException {
    if (value == null) {
      pstmt.setNull(parameterIndex, java.sql.Types.NUMERIC);
    } else {
      pstmt.setLong(parameterIndex, value);
    }
  }
}
