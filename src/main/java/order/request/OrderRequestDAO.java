package order.request;

import common.DBConnection;
import common.type.DBType;
import common.type.OrderStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    try {
      connection = DBConnection.getConnection(DBType.ORACLE);
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setLong(1, orderRequestId);
      preparedStatement.setString(2, orderStatus);

      resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        orderRequestDTOList.add(mapToOrderRequestDTO(resultSet));
      }
    } catch (SQLException e) {
      throw e;
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      DBConnection.close(resultSet);
      DBConnection.close(preparedStatement);
      DBConnection.close(connection);
    }

    return orderRequestDTOList;
  }

  public OrderRequestDTO findByOrderRequestId(Connection connection, long orderRequestId)
      throws SQLException {
    String sql = "SELECT * FROM order_request WHERE order_request_id = ?";

    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    try {
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setLong(1, orderRequestId);
      resultSet = preparedStatement.executeQuery();

      if (resultSet.next()) {
        return mapToOrderRequestDTO(resultSet);
      }

      return null;
    } finally {
      DBConnection.close(resultSet);
      DBConnection.close(preparedStatement);
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
    sql += "WHERE orq.STORE_ID = ? ";
    sql += "AND orq.ORDER_STATUS = 'SENT' ";
    sql += "AND NOT EXISTS (";
    sql += "SELECT 1 ";
    sql += "FROM STORE_RECEIPT sr ";
    sql += "WHERE sr.ORDER_REQUEST_ID = orq.ORDER_REQUEST_ID";
    sql += ") ";
    sql += "ORDER BY orq.SENT_TO_SUPPLIER_AT DESC";

    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;

    try {
      connection = DBConnection.getConnection(DBType.ORACLE);
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setLong(1, storeId);
      resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        orderRequestDTOList.add(mapToOrderRequestDTO(resultSet));
      }
    } finally {
      DBConnection.close(resultSet);
      DBConnection.close(preparedStatement);
      DBConnection.close(connection);
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

  private Long getNullableLong(ResultSet resultSet, String columnName) throws SQLException {
    long value = resultSet.getLong(columnName);

    if (resultSet.wasNull()) {
      return null;
    }

    return value;
  }

  private Integer getNullableInt(ResultSet resultSet, String columnName) throws SQLException {
    int value = resultSet.getInt(columnName);

    if (resultSet.wasNull()) {
      return null;
    }

    return value;
  }

  private LocalDateTime getNullableLocalDateTime(ResultSet resultSet, String columnName)
      throws SQLException {
    Timestamp value = resultSet.getTimestamp(columnName);

    if (value == null) {
      return null;
    }

    return value.toLocalDateTime();
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
