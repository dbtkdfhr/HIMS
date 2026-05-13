package product;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

  public String findProductNameById(long productId) throws SQLException {
    String sql = "SELECT PRODUCT_NAME FROM PRODUCT WHERE PRODUCT_ID = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, productId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("PRODUCT_NAME");
        }
      }
    }

    return "알 수 없음(" + productId + ")";
  }
}
