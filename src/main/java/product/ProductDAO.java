package product;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ProductDAO {
  /* INSERT */
  // 상품 추가
  public int insertProduct(ProductDTO product) throws SQLException {
    String sql = "INSERT INTO product (" +
        "brand_id, " +
        "category_id, " +
        "product_name, " +
        "price, " +
        "season_type, " +
        "product_status" +
        ") VALUES (?, ?, ?, ?, ?, ?)";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setLong(1, product.getBrandId());
      pstmt.setLong(2, product.getCategoryId());
      pstmt.setString(3, product.getProductName());
      pstmt.setInt(4, product.getPrice());

      if (product.getSeasonType() == null) {
        pstmt.setNull(5, Types.VARCHAR);
      } else {
        pstmt.setString(5, product.getSeasonType());
      }

      pstmt.setString(6, product.getProductStatus());

      return pstmt.executeUpdate();
    }
  }
}
