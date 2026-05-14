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

  public static <T> T executeTwo(
      DBType firstDbType,
      DBType secondDbType,
      TwoConnectionWork<T> work
  ) throws SQLException {
    Connection firstConn = null;
    Connection secondConn = null;

    try {
      firstConn = DBConnection.getConnection(firstDbType);
      secondConn = DBConnection.getConnection(secondDbType);

      firstConn.setAutoCommit(false);
      secondConn.setAutoCommit(false);

      T result = work.execute(firstConn, secondConn);

      firstConn.commit();
      secondConn.commit();

      return result;

    } catch (SQLException | RuntimeException e) {
      rollback(firstConn);
      rollback(secondConn);
      throw e;

    } finally {
      DBConnection.close(firstConn);
      DBConnection.close(secondConn);
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

  @FunctionalInterface
  public interface TwoConnectionWork<T> {

    T execute(Connection firstConn, Connection secondConn) throws SQLException;
  }

}
