package store.receipt;

import static common.GetNullableVariable.getNullableLocalDateTime;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoreReceiptDAO {

  public List<StoreReceiptDTO> findAll() throws SQLException {
    List<StoreReceiptDTO> list = new ArrayList<>();

    String sql = "SELECT * FROM STORE_RECEIPT";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        list.add(mapToStoreReceiptDTO(rs));
      }
    }

    return list;
  }

  public List<StoreReceiptDTO> findByStoreId(long storeId) throws SQLException {
    List<StoreReceiptDTO> list = new ArrayList<>();

    String sql = "";
    sql += "SELECT sr.* ";
    sql += "FROM STORE_RECEIPT sr ";
    sql += "JOIN ORDER_REQUEST orq ON sr.order_request_id = orq.order_request_id ";
    sql += "WHERE orq.store_id = ? ";
    sql += "ORDER BY sr.created_at DESC";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, storeId);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          list.add(mapToStoreReceiptDTO(rs));
        }
      }
    }

    return list;
  }

  public StoreReceiptDTO findByOrderRequestId(long orderRequestId) throws SQLException {
    String sql = "SELECT * FROM STORE_RECEIPT WHERE order_request_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, orderRequestId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapToStoreReceiptDTO(rs);
        }

        return null;
      }
    }
  }

  public int insertStoreReceipt(Connection conn, StoreReceiptDTO storeReceiptDTO)
      throws SQLException {
    String sql = "INSERT INTO STORE_RECEIPT (";
    sql += "order_request_id, ";
    sql += "confirm_employee_id, ";
    sql += "received_quantity, ";
    sql += "difference_quantity, ";
    sql += "difference_reason, ";
    sql += "receipt_status";
    sql += ") VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, storeReceiptDTO.getOrderRequestId());
      pstmt.setLong(2, storeReceiptDTO.getConfirmEmployeeId());
      pstmt.setInt(3, storeReceiptDTO.getReceivedQuantity());
      pstmt.setInt(4, storeReceiptDTO.getDifferenceQuantity());
      pstmt.setString(5, storeReceiptDTO.getDifferenceReason());
      pstmt.setString(6, storeReceiptDTO.getReceiptStatus());

      return pstmt.executeUpdate();
    }
  }

  public boolean existsByOrderRequestId(Connection conn, long orderRequestId) throws SQLException {
    String sql = "SELECT 1 FROM STORE_RECEIPT WHERE order_request_id = ?";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, orderRequestId);

      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  private StoreReceiptDTO mapToStoreReceiptDTO(ResultSet resultSet) throws SQLException {
    StoreReceiptDTO dto = new StoreReceiptDTO();

    dto.setStoreReceiptId(resultSet.getLong("STORE_RECEIPT_ID"));
    dto.setOrderRequestId(resultSet.getLong("ORDER_REQUEST_ID"));
    dto.setConfirmEmployeeId(resultSet.getLong("CONFIRM_EMPLOYEE_ID"));
    dto.setReceivedQuantity(resultSet.getInt("RECEIVED_QUANTITY"));
    dto.setDifferenceQuantity(resultSet.getInt("DIFFERENCE_QUANTITY"));
    dto.setDifferenceReason(resultSet.getString("DIFFERENCE_REASON"));
    dto.setReceiptStatus(resultSet.getString("RECEIPT_STATUS"));

    dto.setCreatedAt(getNullableLocalDateTime(resultSet, "CREATED_AT"));
    dto.setUpdatedAt(getNullableLocalDateTime(resultSet, "UPDATED_AT"));

    return dto;
  }
}
