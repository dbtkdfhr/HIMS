package store.inventory;

import common.DBConnection;
import common.GetNullableVariable;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StoreInventoryDAO {

  public Integer findCurrentQuantity(long storeId, long productId) throws SQLException {
    String sql = "";
    sql += "SELECT current_quantity ";
    sql += "FROM STORE_INVENTORY ";
    sql += "WHERE store_id = ? ";
    sql += "AND product_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, storeId);
      pstmt.setLong(2, productId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (!rs.next()) {
          return null;
        }

        return GetNullableVariable.getNullableInt(rs, "CURRENT_QUANTITY");
      }
    }
  }

  public int decreaseQuantity(Connection conn, long storeId, long productId, int quantity)
      throws SQLException {
    String decreaseQuantitySql = "";
    decreaseQuantitySql += "UPDATE STORE_INVENTORY ";
    decreaseQuantitySql += "SET current_quantity = current_quantity - ? ";
    decreaseQuantitySql += "WHERE store_id = ? ";
    decreaseQuantitySql += "AND product_id = ? ";
    decreaseQuantitySql += "AND current_quantity >= ? ";
    decreaseQuantitySql += "AND ? > 0";

    try (PreparedStatement pstmt = conn.prepareStatement(decreaseQuantitySql)) {
      pstmt.setInt(1, quantity);
      pstmt.setLong(2, storeId);
      pstmt.setLong(3, productId);
      pstmt.setInt(4, quantity);
      pstmt.setInt(5, quantity);

      return pstmt.executeUpdate();
    }
  }

  public int increaseQuantity(Connection conn, long storeId, long productId, int quantity)
      throws SQLException {
    String increaseQuantitySql = "";
    increaseQuantitySql += "UPDATE STORE_INVENTORY ";
    increaseQuantitySql += "SET current_quantity = current_quantity + ? ";
    increaseQuantitySql += "WHERE store_id = ? ";
    increaseQuantitySql += "AND product_id = ? ";
    increaseQuantitySql += "AND ? > 0";

    try (PreparedStatement pstmt = conn.prepareStatement(increaseQuantitySql)) {
      pstmt.setInt(1, quantity);
      pstmt.setLong(2, storeId);
      pstmt.setLong(3, productId);
      pstmt.setInt(4, quantity);

      return pstmt.executeUpdate();
    }
  }
}
