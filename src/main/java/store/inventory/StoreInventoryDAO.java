package store.inventory;

import common.DBConnection;
import common.DBType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StoreInventoryDAO {

  public int decreaseQuantity(int storeId, int productId, int quantity)
      throws SQLException {
    String decreaseQuantitySql = "";
    decreaseQuantitySql += "UPDATE STORE_INVENTORY ";
    decreaseQuantitySql += "SET current_quantity = current_quantity - ?, ";
    decreaseQuantitySql += "updated_at = SYSDATE ";
    decreaseQuantitySql += "WHERE store_id = ? ";
    decreaseQuantitySql += "AND product_id = ? ";

    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = DBConnection.getConnection(DBType.ORACLE);
      pstmt = conn.prepareStatement(decreaseQuantitySql);
      pstmt.setInt(1, quantity);
      pstmt.setInt(2, storeId);
      pstmt.setInt(3, productId);

      return pstmt.executeUpdate();
    } finally {
      DBConnection.close(conn);
      DBConnection.close(pstmt);
    }
  }
}
