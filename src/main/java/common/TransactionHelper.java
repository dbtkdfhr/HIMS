package common;

import common.type.DBType;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionHelper {

  private TransactionHelper() {
  }

  public static <T> T execute(DBType dbType, TransactionWork<T> work) throws SQLException {
    Connection conn = null;

    try {
      conn = DBConnection.getConnection(dbType);
      conn.setAutoCommit(false);

      T result = work.execute(conn);

      conn.commit();

      return result;
    } catch (SQLException | RuntimeException e) {
      rollback(conn);
      throw e;
    } finally {
      DBConnection.close(conn);
    }
  }

  private static void rollback(Connection conn) {
    if (conn == null) {
      return;
    }

    try {
      conn.rollback();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @FunctionalInterface
  public interface TransactionWork<T> {

    T execute(Connection conn) throws SQLException;
  }
}
