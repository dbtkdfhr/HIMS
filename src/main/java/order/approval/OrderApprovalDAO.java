package order.approval;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderApprovalDAO {

  public void insertApprovalHistory(OrderApprovalDTO dto) throws SQLException {
    String sql = "INSERT INTO order_approval (order_approval_id, approval_employee_id, " +
        "order_request_id, approved_quantity, approval_status, approval_comment, created_at) " +
        "VALUES ( ?, ?, ?, ?, ?,?, SYSDATE)";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE)) {
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setLong(1, dto.getOrderApprovalId());
      pstmt.setLong(2, dto.getApprovalEmployeeId());
      pstmt.setLong(3, dto.getOrderRequestId());
      pstmt.setInt(4, dto.getApprovedQuantity());
      pstmt.setString(5, dto.getApprovalStatus());
      pstmt.setString(6, dto.getApprovalComment());

      pstmt.executeUpdate();
    }
  }
}
