package brand;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

  public List<BrandDTO> findAll() throws SQLException {
    String sql = "SELECT brand_id, brand_name FROM BRAND ORDER BY brand_name";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      List<BrandDTO> brands = new ArrayList<>();

      while (rs.next()) {
        BrandDTO brand = new BrandDTO();
        brand.setBrandId(rs.getLong("brand_id"));
        brand.setBrandName(rs.getString("brand_name"));
        brands.add(brand);
      }

      return brands;
    }
  }
}
