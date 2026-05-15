package inventory;

import common.DBConnection;
import common.GetNullableVariable;
import common.type.DBType;
import common.type.ProductStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

  /**
   * 통합 재고 검색 메서드
   * 모든 필터 조건은 Optional(null 허용)이며, 조건이 있을 때만 WHERE 절에 추가됨
   */
  public List<InventoryDTO> searchInventory(Long branchId, Integer storeId, String brandName,
      String categoryName, String storeName, String productName, boolean isLowStockOnly)
      throws SQLException {
    List<InventoryDTO> list = new ArrayList<>();

    StringBuilder sql = new StringBuilder("SELECT br.branch_id, br.branch_name, "
        + "si.store_id, s.store_name, s.floor_info, s.store_location, si.product_id, "
        + "si.current_quantity, si.safety_quantity, si.updated_at, si.is_low_stock, "
        + "p.product_name, p.price, p.season_type, p.product_status, b.brand_id, b.brand_name, "
        + "c.category_id, c.category_name " + "FROM STORE_INVENTORY si "
        + "JOIN STORE s ON si.store_id = s.store_id "
        + "JOIN BRANCH br ON s.branch_id = br.branch_id "
        + "JOIN PRODUCT p ON si.product_id = p.product_id "
        + "JOIN BRAND b ON p.brand_id = b.brand_id "
        + "JOIN CATEGORY c ON p.category_id = c.category_id " + "WHERE 1 = 1 ");

    List<Object> params = new ArrayList<>();

    if (branchId != null) {
      sql.append("AND br.branch_id = ? ");
      params.add(branchId);
    }
    if (storeId != null) {
      sql.append("AND si.store_id = ? ");
      params.add(storeId);
    }
    if (isLowStockOnly) {
      sql.append("AND si.is_low_stock = 'Y' ");
    }
    if (brandName != null && !brandName.isEmpty()) {
      sql.append("AND b.brand_name = ? ");
      params.add(brandName);
    }
    if (categoryName != null && !categoryName.isEmpty()) {
      sql.append("AND c.category_name = ? ");
      params.add(categoryName);
    }
    if (storeName != null && !storeName.isEmpty()) {
      sql.append("AND s.store_name LIKE ? ");
      params.add("%" + storeName + "%");
    }
    if (productName != null && !productName.isEmpty()) {
      sql.append("AND p.product_name LIKE ? ");
      params.add("%" + productName + "%");
    }

    sql.append("ORDER BY si.is_low_stock DESC, br.branch_name ASC, s.store_name ASC, p.product_name ASC");

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
    }

    return list;
  }

  /**
   * 안전재고 수량 변경
   */
  public int updateSafetyQuantity(int storeId, int productId, int newSafetyQty)
      throws SQLException {
    String sql = "UPDATE STORE_INVENTORY " + "SET safety_quantity = ?, updated_at = SYSDATE "
        + "WHERE store_id = ? AND product_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, newSafetyQty);
      pstmt.setInt(2, storeId);
      pstmt.setInt(3, productId);

      return pstmt.executeUpdate();
    }
  }

  /**
   * 현재재고 수량 변경
   */
  public int updateCurrentQuantity(int storeId, int productId, int newCurrentQty)
      throws SQLException {
    String sql = "UPDATE STORE_INVENTORY " + "SET current_quantity = ?, updated_at = SYSDATE "
        + "WHERE store_id = ? AND product_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, newCurrentQty);
      pstmt.setInt(2, storeId);
      pstmt.setInt(3, productId);

      return pstmt.executeUpdate();
    }
  }

  private InventoryDTO mapRow(ResultSet rs) throws SQLException {
    InventoryDTO dto = new InventoryDTO();

    dto.setBranchId(GetNullableVariable.getNullableLong(rs, "branch_id"));
    dto.setBranchName(rs.getString("branch_name"));
    dto.setStoreId(GetNullableVariable.getNullableLong(rs, "store_id"));
    dto.setStoreName(rs.getString("store_name"));
    dto.setFloorInfo(rs.getString("floor_info"));
    dto.setStoreLocation(rs.getString("store_location"));
    dto.setProductId(GetNullableVariable.getNullableLong(rs, "product_id"));
    dto.setCurrentQuantity(rs.getInt("current_quantity"));
    dto.setSafetyQuantity(rs.getInt("safety_quantity"));
    dto.setUpdatedAt(GetNullableVariable.getNullableLocalDateTime(rs, "updated_at"));
    dto.setProductName(rs.getString("product_name"));
    dto.setPrice(rs.getInt("price"));
    dto.setSeasonType(rs.getString("season_type"));
    dto.setProductStatus(ProductStatus.valueOf(rs.getString("product_status")));
    dto.setBrandId(GetNullableVariable.getNullableLong(rs, "brand_id"));
    dto.setBrandName(rs.getString("brand_name"));
    dto.setCategoryId(GetNullableVariable.getNullableLong(rs, "category_id"));
    dto.setCategoryName(rs.getString("category_name"));
    dto.setLowStock("Y".equals(rs.getString("is_low_stock")));

    return dto;
  }
}
