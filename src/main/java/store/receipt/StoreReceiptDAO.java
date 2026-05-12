package store.receipt;

import static common.GetNullableVariable.getNullableLocalDateTime;

import common.DBConnection;
import common.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoreReceiptDAO {

  public List<StoreReceiptDTO> findAll() throws SQLException {
    List<StoreReceiptDTO> list = new ArrayList<>();

    Connection conn = null;
    PreparedStatement pstmt = null;

    ResultSet rs = null;

    try {
      conn = DBConnection.getConnection(DBType.ORACLE);
      String sql = "SELECT * FROM STORE_RECEIPT";
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        StoreReceiptDTO dto = new StoreReceiptDTO();
        dto.setStoreReceiptId(rs.getLong("STORE_RECEIPT_ID"));
        dto.setOrderRequestId(rs.getLong("ORDER_REQUEST_ID"));
        dto.setConfirmEmployeeId(rs.getLong("CONFIRM_EMPLOYEE_ID"));
        dto.setReceivedQuantity(rs.getInt("RECEIVED_QUANTITY"));
        dto.setDifferenceQuantity(rs.getInt("DIFFERENCE_QUANTITY"));
        dto.setDifferenceReason(rs.getString("DIFFERENCE_REASON"));
        dto.setReceiptStatus(rs.getString("RECEIPT_STATUS"));

        list.add(dto);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DBConnection.close(conn);
      DBConnection.close(pstmt);
      DBConnection.close(rs);
    }

    return list;
  }

  public int insertStoreReceipt(StoreReceiptDTO storeReceiptDTO) throws SQLException {
    String sql = "INSERT INTO STORE_RECEIPT (";
    sql += "ORDER_REQUEST_ID, ";
    sql += "CONFIRM_EMPLOYEE_ID, ";
    sql += "RECEIVED_QUANTITY, ";
    sql += "DIFFERENCE_QUANTITY, ";
    sql += "DIFFERENCE_REASON, ";
    sql += "RECEIPT_STATUS";
    sql += ") VALUES (?, ?, ?, ?, ?, ?)";

    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = DBConnection.getConnection(DBType.ORACLE);
      pstmt = conn.prepareStatement(sql);
      pstmt.setLong(1, storeReceiptDTO.getOrderRequestId());
      pstmt.setLong(2, storeReceiptDTO.getConfirmEmployeeId());
      pstmt.setInt(3, storeReceiptDTO.getReceivedQuantity());
      pstmt.setInt(4, storeReceiptDTO.getDifferenceQuantity());
      pstmt.setString(5, storeReceiptDTO.getDifferenceReason());
      pstmt.setString(6, storeReceiptDTO.getReceiptStatus());

      return pstmt.executeUpdate();
    } finally {
      DBConnection.close(conn);
      DBConnection.close(pstmt);
    }
  }

  private StoreReceiptDTO mapToOrderRequestDTO(ResultSet resultSet) throws SQLException {
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
