package product;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
  public List<ProductDTO> findAll() throws SQLException {
    String sql = "SELECT product_id, brand_id, category_id, product_name, price, "
        + "season_type, product_status, created_at, updated_at "
        + "FROM PRODUCT ORDER BY product_id";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      List<ProductDTO> products = new ArrayList<>();

      while (rs.next()) {
        ProductDTO product = new ProductDTO();
        product.setProductId(rs.getLong("PRODUCT_ID"));
        product.setBrandId(rs.getLong("BRAND_ID"));
        product.setCategoryId(rs.getLong("CATEGORY_ID"));
        product.setProductName(rs.getString("PRODUCT_NAME"));
        product.setPrice(rs.getInt("PRICE"));
        product.setSeasonType(rs.getString("SEASON_TYPE"));
        product.setProductStatus(rs.getString("PRODUCT_STATUS"));
        product.setCreatedAt(common.GetNullableVariable.getNullableLocalDateTime(rs, "CREATED_AT"));
        product.setUpdatedAt(common.GetNullableVariable.getNullableLocalDateTime(rs, "UPDATED_AT"));
        products.add(product);
      }

      return products;
    }
  }

  /* INSERT */
  // 상품 추가
  public int insertProduct(ProductDTO product) throws SQLException {
    String sql = "INSERT INTO PRODUCT (" +
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

  public String findProductNameById(long productId) throws SQLException {
    String sql = "SELECT product_name FROM PRODUCT WHERE product_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, productId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("PRODUCT_NAME");
        }
      }
    }

    return "알 수 없음(" + productId + ")";
  }

  /* UPDATE */
  public int updateProductPrice(ProductDTO product) throws SQLException {
    String sql = "UPDATE PRODUCT SET " +
        "price = ?, " +
        "updated_at = SYSDATE " +
        "WHERE product_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, product.getPrice());
      pstmt.setLong(2, product.getProductId());

      return pstmt.executeUpdate();
    }
  }

  public int updateProductStatus(ProductDTO product) throws SQLException {
    String sql = "UPDATE PRODUCT SET " +
        "product_status = ?, " +
        "updated_at = SYSDATE " +
        "WHERE product_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setString(1, product.getProductStatus());
      pstmt.setLong(2, product.getProductId());

      return pstmt.executeUpdate();
    }
  }

  public int updateProductSeasonType(ProductDTO product) throws SQLException {
    String sql = "UPDATE PRODUCT SET " +
        "season_type = ?, " +
        "updated_at = SYSDATE " +
        "WHERE product_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      if (product.getSeasonType() == null) {
        pstmt.setNull(1, Types.VARCHAR);
      } else {
        pstmt.setString(1, product.getSeasonType());
      }

      pstmt.setLong(2, product.getProductId());

      return pstmt.executeUpdate();
    }
  }

  // 상품 basicInfo(가격, 상태, 시즌) 수정
  public int updateProductBasicInfo(ProductDTO product) throws SQLException {
    String sql = "UPDATE PRODUCT SET " +
        "price = ?, " +
        "product_status = ?, " +
        "season_type = ?, " +
        "updated_at = SYSDATE " +
        "WHERE product_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, product.getPrice());
      pstmt.setString(2, product.getProductStatus());

      if (product.getSeasonType() == null) {
        pstmt.setNull(3, Types.VARCHAR);
      } else {
        pstmt.setString(3, product.getSeasonType());
      }

      pstmt.setLong(4, product.getProductId());

      return pstmt.executeUpdate();
    }
  }
}
