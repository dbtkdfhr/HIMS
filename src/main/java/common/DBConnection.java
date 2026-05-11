package common;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
  public static Connection getConnection(DBType dbType) {
    Connection conn = null;

    try {
      switch (dbType) {
        case ORACLE:
          conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/FREEPDB1", "system", "1004");
          break;
        case MARIADB:
          conn = DriverManager.getConnection("jdbc:mariadb://127.0.0.1:3307/musinsa", "musinsa", "musinsa1234");
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return conn;
  }

  public static Connection getConnection(
      DBType dbType,
      String id,
      String pwd
  ) {
    Connection conn = null;

    try {
      switch (dbType) {
        case ORACLE:
          conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/FREEPDB1", id, pwd);
          break;
        case MARIADB:
          conn = DriverManager.getConnection("jdbc:mariadb://127.0.0.1:3307/musinsa", id, pwd);
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return conn;
  }

  public static void close(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void close(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void close(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void close(PreparedStatement pstmt) {
    if (pstmt != null) {
      try {
        pstmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void close(CallableStatement cstmt) {
    if (cstmt != null) {
      try {
        cstmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
