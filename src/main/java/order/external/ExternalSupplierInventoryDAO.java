package order.external;

import common.DBConnection;
import common.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExternalSupplierInventoryDAO {
  public void decreaseStock(long productId, int quantity) throws SQLException{
    String sql = "UPDATE supplier_inventory SET current_quantity = current_quantity - ? WHERE supplier_product_id = ? AND current_quantity >= ?";

    try(Connection conn = DBConnection.getConnection(DBType.MARIADB); PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, quantity);
      pstmt.setLong(2, productId);
      pstmt.setLong(3, quantity);

      int result = pstmt.executeUpdate();
      if (result == 0) {
        throw new SQLException("재고가 부족하여 승인 처리에 실패했습니다.");
      }
    }
  }

  public void insertExternalRecipt(int orderRequestId) throws SQLException{
    String sql = "INSERT INTO external_order_receipt (internal_order_request_id, receipt_status) " +
        "VALUES (?, 'RECEIVED')";

    try (Connection conn = DBConnection.getConnection(DBType.MARIADB);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, orderRequestId);
      pstmt.executeUpdate();
    }
  }

  public List<ExternalSupplierInventoryDTO> findAllInventory() throws SQLException{
    List<ExternalSupplierInventoryDTO> inventoryList = new ArrayList<>();
    String sql = "SELECT supplier_id, supplier_product_id, current_quantity FROM supplier_inventory";
    try (Connection conn = DBConnection.getConnection(DBType.MARIADB);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        ExternalSupplierInventoryDTO dto = new ExternalSupplierInventoryDTO();
        dto.setSupplierId(rs.getLong("supplier_id"));
        dto.setSupplierProductId(rs.getLong("supplier_product_id"));
        dto.setQuantity(rs.getInt("current_quantity"));
        inventoryList.add(dto);
      }
    }
    return inventoryList;
  }

  public ExternalSupplierInventoryDTO findInventoryById(long productId) throws SQLException{
    String sql = "SELECT supplier_id, supplier_product_id, current_quantity FROM supplier_inventory WHERE supplier_product_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.MARIADB);
    PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, productId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          ExternalSupplierInventoryDTO dto = new ExternalSupplierInventoryDTO();
          dto.setSupplierId(rs.getLong("supplier_id"));
          dto.setSupplierProductId(rs.getLong("supplier_product_id"));
          dto.setQuantity(rs.getInt("current_quantity"));
          return dto;
        }
      }
    }return null;
  }
}
