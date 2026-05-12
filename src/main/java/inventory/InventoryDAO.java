package inventory;

import common.DBConnection;
import common.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

  // [INV-01] 내 매장 전체 재고 목록 조회 || [INV-02] 안전재고 미만 상품만 조회 || [INV-03] 재고 검색 (브랜드명 / 카테고리명 / 상품명 키워드)
  public List<InventoryDTO> searchInventory(int storeId, String brandName, String categoryName,
      String keyword, boolean isLowStockOnly) {
    List<InventoryDTO> list = new ArrayList<>();

    StringBuilder sql = new StringBuilder("SELECT si.store_id, si.product_id, "
        + "si.current_quantity, si.safety_quantity, si.updated_at, si.is_low_stock, "
        + "p.product_name, p.price, p.season_type, p.product_status, b.brand_name, "
        + "c.category_name " + "FROM STORE_INVENTORY si "
        + "JOIN PRODUCT p ON si.PRODUCT_ID = p.PRODUCT_ID "
        + "JOIN BRAND b ON p.BRAND_ID = b.BRAND_ID "
        + "JOIN CATEGORY c ON p.CATEGORY_ID = c.CATEGORY_ID " + "WHERE si.STORE_ID = ? ");

    List<Object> params = new ArrayList<>();
    params.add(storeId);

    if (isLowStockOnly) {
      sql.append("AND si.IS_LOW_STOCK = 'Y' ");
    }
    if (brandName != null && !brandName.isEmpty()) {
      sql.append("AND b.BRAND_NAME = ? ");
      params.add(brandName);
    }
    if (categoryName != null && !categoryName.isEmpty()) {
      sql.append("AND c.CATEGORY_NAME = ? ");
      params.add(categoryName);
    }
    if (keyword != null && !keyword.isEmpty()) {
      sql.append("AND p.PRODUCT_NAME LIKE ? ");
      params.add("%" + keyword + "%");
    }
    sql.append("ORDER BY IS_LOW_STOCK DESC, p.PRODUCT_NAME ASC");

    // 2단계: try-with-resources 로 자원 자동 반납
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
    }

    return list;
  }

  // [INV-04] 안전재고 수량 변경
  public int updateSafetyQuantity(int storeId, int productId, int newSafetyQty) {
    String sql = "UPDATE STORE_INVENTORY " + "SET SAFETY_QUANTITY = ?, UPDATED_AT = SYSDATE "
        + "WHERE STORE_ID = ? AND PRODUCT_ID = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, newSafetyQty);
      pstmt.setInt(2, storeId);
      pstmt.setInt(3, productId);

      return pstmt.executeUpdate(); // 1: 성공, 0: 실패

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return 0;
  }

  // ResultSet → InventoryDTO 변환 (공통 매핑)
  private InventoryDTO mapRow(ResultSet rs) throws SQLException {
    InventoryDTO dto = new InventoryDTO();

    dto.setStoreId(rs.getLong("STORE_ID"));
    dto.setProductId(rs.getLong("PRODUCT_ID"));
    dto.setCurrentQuantity(rs.getInt("CURRENT_QUANTITY"));
    dto.setSafetyQuantity(rs.getInt("SAFETY_QUANTITY"));
    dto.setUpdatedAt(rs.getTimestamp("UPDATED_AT").toLocalDateTime());
    dto.setProductName(rs.getString("PRODUCT_NAME"));
    dto.setPrice(rs.getInt("PRICE"));
    dto.setSeasonType(rs.getString("SEASON_TYPE"));
    dto.setProductStatus(ProductStatus.valueOf(rs.getString("PRODUCT_STATUS")));
    dto.setBrandName(rs.getString("BRAND_NAME"));
    dto.setCategoryName(rs.getString("CATEGORY_NAME"));
    dto.setLowStock("Y".equals(rs.getString("IS_LOW_STOCK")));

    return dto;
  }
}
