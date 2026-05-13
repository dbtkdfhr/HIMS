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

  /* SELECT */
  // 카테고리 레벨 기준 목록 조회
  public List<CategoryDTO> getAllCategories() throws SQLException {
    String sql = "SELECT " +
        "category_id, " +
        "parent_category_id, " +
        "category_name, " +
        "category_level, " +
        "is_active, " +
        "created_at, " +
        "updated_at " +
        "FROM category ";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      try (ResultSet rs = pstmt.executeQuery()) {
        List<CategoryDTO> categories = new ArrayList<>();

        while (rs.next()) {
          CategoryDTO categoryDTO = new CategoryDTO();

          categoryDTO.setCategoryId(rs.getLong("category_id"));
          categoryDTO.setParentCategoryId(getNullableLong(rs, "parent_category_id"));
          categoryDTO.setCategoryName(rs.getString("category_name"));
          categoryDTO.setCategoryLevel(rs.getInt("category_level"));
          categoryDTO.setIsActive(rs.getString("is_active"));
          categoryDTO.setCreatedAt(getNullableLocalDateTime(rs, "created_at"));
          categoryDTO.setUpdatedAt(getNullableLocalDateTime(rs, "updated_at"));

          categories.add(categoryDTO);
        }

        return categories;
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
}
