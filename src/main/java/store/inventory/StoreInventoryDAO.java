package store.inventory;

import common.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StoreInventoryDAO {

  public int decreaseQuantity(Connection conn, long storeId, long productId, int quantity)
      throws SQLException {
    String decreaseQuantitySql = "";
    decreaseQuantitySql += "UPDATE STORE_INVENTORY ";
    decreaseQuantitySql += "SET current_quantity = current_quantity - ?, ";
    decreaseQuantitySql += "updated_at = SYSDATE ";
    decreaseQuantitySql += "WHERE store_id = ? ";
    decreaseQuantitySql += "AND product_id = ? ";
    decreaseQuantitySql += "AND current_quantity >= ? ";
    decreaseQuantitySql += "AND ? > 0";

    PreparedStatement pstmt = null;

    try {
      pstmt = conn.prepareStatement(decreaseQuantitySql);
      pstmt.setInt(1, quantity);
      pstmt.setLong(2, storeId);
      pstmt.setLong(3, productId);
      pstmt.setInt(4, quantity);
      pstmt.setInt(5, quantity);

      return pstmt.executeUpdate();
    } finally {
      DBConnection.close(pstmt);
    }
  }

  public int increaseQuantity(Connection conn, long storeId, long productId, int quantity)
      throws SQLException {
    String increaseQuantitySql = "";
    increaseQuantitySql += "UPDATE STORE_INVENTORY ";
    increaseQuantitySql += "SET current_quantity = current_quantity + ?, ";
    increaseQuantitySql += "updated_at = SYSDATE ";
    increaseQuantitySql += "WHERE store_id = ? ";
    increaseQuantitySql += "AND product_id = ? ";
    increaseQuantitySql += "AND ? > 0";

    PreparedStatement pstmt = null;

    try {
      pstmt = conn.prepareStatement(increaseQuantitySql);
      pstmt.setInt(1, quantity);
      pstmt.setLong(2, storeId);
      pstmt.setLong(3, productId);
      pstmt.setInt(4, quantity);

      return pstmt.executeUpdate();
    } finally {
      DBConnection.close(pstmt);
    }
  }
}
