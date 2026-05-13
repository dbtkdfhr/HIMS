package store;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoreDAO {

  public String findStoreNameById(long storeId) throws SQLException {
    String sql = "SELECT STORE_NAME FROM STORE WHERE STORE_ID = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, storeId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("STORE_NAME");
        }
      }
    }

    return "알 수 없음(" + storeId + ")";
  }

  public List<String> findAllStoreSummaries() throws SQLException {
    List<String> list = new ArrayList<>();
    String sql = "SELECT STORE_ID, STORE_NAME FROM STORE ORDER BY STORE_ID";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        list.add(rs.getLong("STORE_ID") + ". " + rs.getString("STORE_NAME"));
      }
    }

    return list;
  }
}
