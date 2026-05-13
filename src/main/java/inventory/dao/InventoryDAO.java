package inventory.dao;

import common.DBConnection;
import common.type.DBType;
import inventory.dto.InventoryDTO;
import inventory.dto.ProductStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

  // [INV-01] 내 매장 전체 재고 목록 조회
  public List<InventoryDTO> searchInventory(int storeId, String brandName, String categoryName,
      String keyword, boolean isLowStockOnly) {
    List<InventoryDTO> list = new ArrayList<>();

    StringBuilder sql = new StringBuilder("SELECT si.STORE_ID, si.PRODUCT_ID, "
        + "si.CURRENT_QUANTITY, si.SAFETY_QUANTITY, si.UPDATED_AT, "
        + "p.PRODUCT_NAME, p.PRICE, p.SEASON_TYPE, p.PRODUCT_STATUS, " + "b.BRAND_NAME "
        + "FROM STORE_INVENTORY si " + "JOIN PRODUCT p ON si.PRODUCT_ID = p.PRODUCT_ID "
        + "JOIN BRAND b ON p.BRAND_ID = b.BRAND_ID "
        + "JOIN CATEGORY c ON p.CATEGORY_ID = c.CATEGORY_ID " + "WHERE si.STORE_ID = ? ");

    List<Object> params = new ArrayList<>();
    params.add(storeId);

    // try-with-resources 로 자원 자동 반납
    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

      for (int i = 0; i < params.size(); i++) {
        pstmt.setObject(i + 1, params.get(i));
      }

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          list.add(mapRow(rs));
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println("오류발생" + e.getMessage());
    }

    return list;
  }

  // ResultSet → InventoryDTO 변환 (공통 매핑)
  private InventoryDTO mapRow(ResultSet rs) throws SQLException {
    InventoryDTO dto = new InventoryDTO();

    dto.setStoreId(rs.getLong("STORE_ID"));
    dto.setProductId(rs.getInt("PRODUCT_ID"));
    dto.setCurrentQuantity(rs.getInt("CURRENT_QUANTITY"));
    dto.setSafetyQuantity(rs.getInt("SAFETY_QUANTITY"));
    Date updatedAt = rs.getDate("UPDATED_AT");
    if (updatedAt != null) {
      dto.setUpdatedAt(updatedAt.toLocalDate());
    }
    dto.setProductName(rs.getString("PRODUCT_NAME"));
    dto.setPrice(rs.getInt("PRICE"));
    dto.setSeasonType(rs.getString("SEASON_TYPE"));
    dto.setProductStatus(ProductStatus.valueOf(rs.getString("PRODUCT_STATUS")));
    dto.setBrandName(rs.getString("BRAND_NAME"));

    return dto;
  }
}
