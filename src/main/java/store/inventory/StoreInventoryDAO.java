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
    decreaseQuantitySql += "UPDATE STORE_INVENTORY\n";
    decreaseQuantitySql += "SET CURRENT_QUANTITY = CURRENT_QUANTITY - ?,\n";
    decreaseQuantitySql += "UPDATED_AT = SYSDATE\n";
    decreaseQuantitySql += "WHERE STORE_ID = ?\n";
    decreaseQuantitySql += "AND PRODUCT_ID = ?\n";

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
