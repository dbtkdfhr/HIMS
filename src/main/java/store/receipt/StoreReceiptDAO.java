package store.receipt;

import common.DBConnection;
import common.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StoreReceiptDAO {

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
}
