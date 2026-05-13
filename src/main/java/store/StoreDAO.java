package store;

import static common.GetNullableVariable.getNullableLocalDateTime;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StoreDAO {
  /* SELECT */
  // 지점 id로 하위 매장 리스트 조회
  public List<StoreDTO> getStoresByBranchId(long branchId) throws SQLException {
    String sql = "SELECT " +
        "store_id, " +
        "branch_id, " +
        "brand_id, " +
        "store_name, " +
        "floor_info, " +
        "store_location, " +
        "operation_status, " +
        "created_at, " +
        "updated_at " +
        "FROM store " +
        "WHERE branch_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setLong(1, branchId);

      try (ResultSet rs = pstmt.executeQuery()) {
        List<StoreDTO> stores = new ArrayList<>();

        while (rs.next()) {
          StoreDTO storeDTO = new StoreDTO();

          storeDTO.setStoreId(rs.getLong("store_id"));
          storeDTO.setBranchId(rs.getLong("branch_id"));
          storeDTO.setBrandId(rs.getLong("brand_id"));
          storeDTO.setStoreName(rs.getString("store_name"));
          storeDTO.setFloorInfo(rs.getString("floor_info"));
          storeDTO.setStoreLocation(rs.getString("store_location"));
          storeDTO.setOperationStatus(rs.getString("operation_status"));
          storeDTO.setCreatedAt(getNullableLocalDateTime(rs, "created_at"));
          storeDTO.setUpdatedAt(getNullableLocalDateTime(rs, "updated_at"));

          stores.add(storeDTO);
        }

        return stores;
      }
    }
  }

  /* INSERT */
  // 입점 매장 등록
  public int insertStore(StoreDTO store) throws SQLException {
    String sql = "INSERT INTO store (" +
        "branch_id, " +
        "brand_id, " +
        "store_name, " +
        "floor_info, " +
        "store_location, " +
        "operation_status" +
        ") VALUES (?, ?, ?, ?, ?, ?)";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setLong(1, store.getBranchId());
      pstmt.setLong(2, store.getBrandId());
      pstmt.setString(3, store.getStoreName());

      if (store.getFloorInfo() == null) {
        pstmt.setNull(4, Types.VARCHAR);
      } else {
        pstmt.setString(4, store.getFloorInfo());
      }

      if (store.getStoreLocation() == null) {
        pstmt.setNull(5, Types.VARCHAR);
      } else {
        pstmt.setString(5, store.getStoreLocation());
      }

      pstmt.setString(6, store.getOperationStatus());

      return pstmt.executeUpdate();
    }
  }

  public String findStoreNameById(long storeId) throws SQLException {
    String sql = "SELECT store_name FROM STORE WHERE store_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, storeId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("STORE_NAME");
        }
      }
    }

    return "알 수 없음(" + storeId + ")";
  }

  public List<String> findAllStoreSummaries() throws SQLException {
    List<String> list = new ArrayList<>();
    String sql = "SELECT store_id, store_name FROM STORE ORDER BY store_id";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        list.add(rs.getLong("STORE_ID") + ". " + rs.getString("STORE_NAME"));
      }
    }

    return list;
  }
}
