package inventory.branch;

import common.DBConnection;
import common.GetNullableVariable;
import common.type.DBType;
import common.type.ProductStatus;
import inventory.InventoryDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchInventoryDAO {

  // [BM-INV-01] 지점 관리자 전체 입점매장 재고 목록 조회
  // [BM-INV-02] 지점/브랜드/카테고리/매장/상품명 기준 검색
  // [BM-INV-03] 재고 부족 상품만 조회
  public List<InventoryDTO> searchAllStoreInventory(Long branchId, String brandName,
      String categoryName, String storeName, String productName, boolean isLowStockOnly)
      throws SQLException {
    List<InventoryDTO> list = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT br.branch_id, br.branch_name, " + "si.store_id, s.store_name, s.floor_info, s.store_location, si.product_id, "
            + "si.current_quantity, si.safety_quantity, si.updated_at, si.is_low_stock, "
            + "p.product_name, p.price, p.season_type, p.product_status, b.brand_name, "
            + "c.category_name " + "FROM store_inventory si "
            + "JOIN store s ON si.store_id = s.store_id "
            + "JOIN branch br ON s.branch_id = br.branch_id "
            + "JOIN product p ON si.product_id = p.product_id "
            + "JOIN brand b ON p.brand_id = b.brand_id "
            + "JOIN category c ON p.category_id = c.category_id " + "WHERE 1 = 1 ");

    List<Object> params = new ArrayList<>();

    if (isLowStockOnly) {
      sql.append("AND si.is_low_stock = 'Y' ");
    }
    if (branchId != null) {
      sql.append("AND br.branch_id = ? ");
      params.add(branchId);
    }
    if (isNotBlank(brandName)) {
      sql.append("AND b.brand_name LIKE ? ");
      params.add("%" + brandName + "%");
    }
    if (isNotBlank(categoryName)) {
      sql.append("AND c.category_name LIKE ? ");
      params.add("%" + categoryName + "%");
    }
    if (isNotBlank(storeName)) {
      sql.append("AND s.store_name LIKE ? ");
      params.add("%" + storeName + "%");
    }
    if (isNotBlank(productName)) {
      sql.append("AND p.product_name LIKE ? ");
      params.add("%" + productName + "%");
    }

    sql.append("ORDER BY br.branch_name ASC, s.store_name ASC, "
        + "si.is_low_stock DESC, p.product_name ASC");

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
    dto.setBrandName(rs.getString("brand_name"));
    dto.setCategoryName(rs.getString("category_name"));
    dto.setLowStock("Y".equals(rs.getString("is_low_stock")));

    return dto;
  }

  private boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }
}
