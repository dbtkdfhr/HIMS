package order.request;

import static common.GetNullableVariable.getNullableInt;
import static common.GetNullableVariable.getNullableLocalDateTime;
import static common.GetNullableVariable.getNullableLong;

import common.DBConnection;
import common.type.DBType;
import common.type.OrderStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderRequestDAO {

  public List<OrderRequestDTO> findAll() throws SQLException {
    List<OrderRequestDTO> orderRequestDTOList = new ArrayList<>();

    String sql = "SELECT * FROM order_request ORDER BY requested_at DESC";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        orderRequestDTOList.add(mapToOrderRequestDTO(rs));
      }
    }
    return orderRequestDTOList;
  }

  public List<OrderRequestDTO> findByOrderRequestIdAndOrderStatus(long orderRequestId,
      String orderStatus)
      throws SQLException {
    List<OrderRequestDTO> orderRequestDTOList = new ArrayList<>();

    String sql = "SELECT * FROM ORDER_REQUEST WHERE order_request_id = ? AND order_status = ?";

    try (Connection connection = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setLong(1, orderRequestId);
      preparedStatement.setString(2, orderStatus);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          orderRequestDTOList.add(mapToOrderRequestDTO(resultSet));
        }
      }
    }

    return orderRequestDTOList;
  }

  public OrderRequestDTO findByOrderRequestId(Connection connection, long orderRequestId)
      throws SQLException {
    String sql = "SELECT * FROM order_request WHERE order_request_id = ?";

    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setLong(1, orderRequestId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return mapToOrderRequestDTO(resultSet);
        }

        return null;
      }
    }
  }

  public OrderRequestDTO findByOrderRequestId(long orderRequestId) throws SQLException {
    try (Connection conn = DBConnection.getConnection(DBType.ORACLE)) {
      return findByOrderRequestId(conn, orderRequestId);
    }
  }

  public List<OrderRequestDTO> findReceiptTargetOrderRequests(long storeId) throws SQLException {
    List<OrderRequestDTO> orderRequestDTOList = new ArrayList<>();

    String sql = "";
    sql += "SELECT orq.* ";
    sql += "FROM ORDER_REQUEST orq ";
    sql += "WHERE orq.store_id = ? ";
    sql += "AND orq.order_status = 'SENT' ";
    sql += "AND NOT EXISTS (";
    sql += "SELECT 1 ";
    sql += "FROM STORE_RECEIPT sr ";
    sql += "WHERE sr.order_request_id = orq.order_request_id";
    sql += ") ";
    sql += "ORDER BY orq.sent_to_supplier_at DESC";

    try (Connection connection = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setLong(1, storeId);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          orderRequestDTOList.add(mapToOrderRequestDTO(resultSet));
        }
      }
    }

    return orderRequestDTOList;
  }

  private OrderRequestDTO mapToOrderRequestDTO(ResultSet resultSet) throws SQLException {
    OrderRequestDTO dto = new OrderRequestDTO();

    dto.setOrderRequestId(resultSet.getLong("ORDER_REQUEST_ID"));
    dto.setStoreId(resultSet.getLong("STORE_ID"));
    dto.setSupplierIntegrationId(resultSet.getLong("SUPPLIER_INTEGRATION_ID"));
    dto.setProductId(resultSet.getLong("PRODUCT_ID"));
    dto.setRequestEmployeeId(resultSet.getLong("REQUEST_EMPLOYEE_ID"));
    dto.setApprovalEmployeeId(getNullableLong(resultSet, "APPROVAL_EMPLOYEE_ID"));
    dto.setApprovalRoleId(getNullableLong(resultSet, "APPROVAL_ROLE_ID"));
    dto.setExternalOrderId(resultSet.getString("EXTERNAL_ORDER_ID"));
    dto.setOrderQuantity(resultSet.getInt("ORDER_QUANTITY"));
    dto.setApprovedQuantity(getNullableInt(resultSet, "APPROVED_QUANTITY"));
    dto.setRequestReason(resultSet.getString("REQUEST_REASON"));
    dto.setRejectReason(resultSet.getString("REJECT_REASON"));
    dto.setOrderStatus(resultSet.getString("ORDER_STATUS"));
    dto.setRequestedAt(getNullableLocalDateTime(resultSet, "REQUESTED_AT"));
    dto.setApprovedAt(getNullableLocalDateTime(resultSet, "APPROVED_AT"));
    dto.setRejectedAt(getNullableLocalDateTime(resultSet, "REJECTED_AT"));
    dto.setSentToSupplierAt(getNullableLocalDateTime(resultSet, "SENT_TO_SUPPLIER_AT"));

    return dto;
  }

  public List<OrderRequestDTO> findAllByStatus(OrderStatus orderStatus) throws SQLException {
    List<OrderRequestDTO> orderRequestDTOList = new ArrayList<>();
    String sql = "SELECT * FROM ORDER_REQUEST WHERE order_status = ? ORDER BY requested_at DESC";
    try (Connection conn = DBConnection.getConnection(
        DBType.ORACLE); PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, orderStatus.name());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          orderRequestDTOList.add(mapToOrderRequestDTO(rs));
        }
      }
    }
    return orderRequestDTOList;
  }

  public int updateStatusAndQuantity(long requestId, int approvedQuantity, long employeeId)
      throws SQLException {
    String sql = "UPDATE order_request SET order_status = ?, approved_quantity = ?, " +
        "approval_employee_id = ?, approved_at = SYSDATE WHERE order_request_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, OrderStatus.APPROVED.name());
      pstmt.setInt(2, approvedQuantity);
      pstmt.setLong(3, employeeId);
      pstmt.setLong(4, requestId);

      return pstmt.executeUpdate();
    }
  }

  public int updateRejectStatus(long requestId, String rejectReason, long employeeId)
      throws SQLException {
    String sql = "UPDATE order_request SET order_status = ?, reject_reason = ?, " +
        "approval_employee_id = ?, rejected_at = SYSDATE WHERE order_request_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, OrderStatus.REJECTED.name());
      pstmt.setString(2, rejectReason);
      pstmt.setLong(3, employeeId);
      pstmt.setLong(4, requestId);

      return pstmt.executeUpdate();
    }
  }
}
