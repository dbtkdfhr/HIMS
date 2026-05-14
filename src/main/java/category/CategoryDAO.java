package category;

import static common.GetNullableVariable.getNullableLocalDateTime;
import static common.GetNullableVariable.getNullableLong;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
  private static final String CATEGORY_COLUMNS =
      "category_id, " +
          "parent_category_id, " +
          "category_name, " +
          "category_level, " +
          "is_active, " +
          "created_at, " +
          "updated_at ";

  /* SELECT */
  // 카테고리 전체 목록 조회
  public List<CategoryDTO> getAllCategories() throws SQLException {
    String sql = "SELECT " + CATEGORY_COLUMNS +
        "FROM category " +
        "ORDER BY category_level, parent_category_id NULLS FIRST, category_id";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      try (ResultSet rs = pstmt.executeQuery()) {
        return mapCategories(rs);
      }
    }
  }

  // 카테고리 레벨 기준 목록 조회
  public List<CategoryDTO> getCategoriesByLevel(int categoryLevel) throws SQLException {
    String sql = "SELECT " + CATEGORY_COLUMNS +
        "FROM category " +
        "WHERE category_level = ? " +
        "ORDER BY parent_category_id NULLS FIRST, category_id";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, categoryLevel);

      try (ResultSet rs = pstmt.executeQuery()) {
        return mapCategories(rs);
      }
    }
  }

  // 상위 카테고리 ID 기준 하위 카테고리 목록 조회
  public List<CategoryDTO> getCategoriesByParentId(Long parentCategoryId) throws SQLException {
    String sql = "SELECT " + CATEGORY_COLUMNS +
        "FROM category " +
        "WHERE " + (parentCategoryId == null ? "parent_category_id IS NULL " : "parent_category_id = ? ") +
        "ORDER BY category_level, category_id";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      if (parentCategoryId != null) {
        pstmt.setLong(1, parentCategoryId);
      }

      try (ResultSet rs = pstmt.executeQuery()) {
        return mapCategories(rs);
      }
    }
  }

  /* UPDATE */
  // category_id기준으로 is_active값 변경
  public int updateCategoryActive(CategoryDTO category) throws SQLException {
    String sql = "UPDATE category SET " +
        "is_active = ?, " +
        "updated_at = SYSDATE " +
        "WHERE category_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setString(1, category.getIsActive());
      pstmt.setLong(2, category.getCategoryId());

      return pstmt.executeUpdate();
    }
  }

  private List<CategoryDTO> mapCategories(ResultSet rs) throws SQLException {
    List<CategoryDTO> categories = new ArrayList<>();

    while (rs.next()) {
      categories.add(mapCategory(rs));
    }

    return categories;
  }

  private CategoryDTO mapCategory(ResultSet rs) throws SQLException {
    CategoryDTO categoryDTO = new CategoryDTO();

    categoryDTO.setCategoryId(rs.getLong("category_id"));
    categoryDTO.setParentCategoryId(getNullableLong(rs, "parent_category_id"));
    categoryDTO.setCategoryName(rs.getString("category_name"));
    categoryDTO.setCategoryLevel(rs.getInt("category_level"));
    categoryDTO.setIsActive(rs.getString("is_active"));
    categoryDTO.setCreatedAt(getNullableLocalDateTime(rs, "created_at"));
    categoryDTO.setUpdatedAt(getNullableLocalDateTime(rs, "updated_at"));

    return categoryDTO;
  }
}
