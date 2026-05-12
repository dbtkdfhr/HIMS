package common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class GetNullableVariable {

  public static Long getNullableLong(ResultSet resultSet, String columnName) throws SQLException {
    long value = resultSet.getLong(columnName);

    if (resultSet.wasNull()) {
      return null;
    }

    return value;
  }

  public static Integer getNullableInt(ResultSet resultSet, String columnName) throws SQLException {
    int value = resultSet.getInt(columnName);

    if (resultSet.wasNull()) {
      return null;
    }

    return value;
  }

  public static LocalDateTime getNullableLocalDateTime(ResultSet resultSet, String columnName)
      throws SQLException {
    Timestamp value = resultSet.getTimestamp(columnName);

    if (value == null) {
      return null;
    }

    return value.toLocalDateTime();
  }

}
