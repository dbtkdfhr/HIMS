package order;

import common.DBConnection;
import common.type.DBType;
import common.type.OrderStatus;
import exception.InputException;
import java.sql.Connection;
import lombok.RequiredArgsConstructor;
import order.approval.OrderApprovalDAO;
import order.approval.OrderApprovalDTO;
import order.external.ExternalOrderReceiptDAO;
import order.external.ExternalOrderReceiptDTO;
import order.external.ExternalSupplierInventoryDAO;
import order.request.OrderRequestDAO;
import order.request.OrderRequestDTO;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class OrderManagementService {
  private final OrderRequestDAO orderRequestDAO;
  private final ExternalOrderReceiptDAO externalOrderReceiptDAO;
  private final OrderApprovalDAO orderApprovalDAO;

  public OrderManagementService() {
    this(new OrderRequestDAO(), new ExternalOrderReceiptDAO(), new OrderApprovalDAO());
  }

  // 발주 요청 전체 목록 조회하기
  public List<OrderRequestDTO> getAllOrderRequests() throws SQLException {
    return orderRequestDAO.findAll();
  }

  //아직 처리되지 않은 발주 요청 보기
  public List<OrderRequestDTO> findPendingOrders() throws SQLException {
    // REQUESTED 상태인 데이터만 필터링하여 조회
    return orderRequestDAO.findAllByStatus(OrderStatus.REQUESTED);
  }

  /*
  발주 승인 프로세스
  1. 내부 시스템(Oracle): 발주 상태 APPROVED 변경 및 승인 이력 저장
  2. 외부 시스템(MariaDB): 공급사 시스템에 접수증 삽입
  */
  public void approveOrder(long orderRequestId, int approvedQuantity, long employeeId) throws SQLException {
    OrderRequestDTO requestedOrder = orderRequestDAO.findByOrderRequestId(orderRequestId);
    if (requestedOrder == null) {
      throw new InputException("발주 요청을 찾을 수 없습니다.");
    }
    if (!OrderStatus.REQUESTED.name().equals(requestedOrder.getOrderStatus())) {
      throw new InputException("요청 상태의 발주만 승인할 수 있습니다.");
    }
    if (approvedQuantity <= 0) {
      throw new InputException("승인 수량은 1개 이상이어야 합니다.");
    }
    if (approvedQuantity > requestedOrder.getOrderQuantity()) {
      throw new InputException("승인수량은 요청수량을 초과할 수 없습니다.");
    }

    try (
        Connection oracleConn = DBConnection.getConnection(DBType.ORACLE);
        Connection mariaConn = DBConnection.getConnection(DBType.MARIADB)
    ) {
      oracleConn.setAutoCommit(false);
      mariaConn.setAutoCommit(false);

      try {
        // 1. Oracle 발주 요청 상태 APPROVED 변경
        orderRequestDAO.updateStatusAndQuantity(
            oracleConn,
            orderRequestId,
            approvedQuantity,
            employeeId
        );

        // 2. Oracle 승인 이력 저장
        OrderApprovalDTO approvalDTO = new OrderApprovalDTO();
        approvalDTO.setApprovalEmployeeId(employeeId);
        approvalDTO.setOrderRequestId(orderRequestId);
        approvalDTO.setApprovedQuantity(approvedQuantity);
        approvalDTO.setApprovalStatus(OrderStatus.APPROVED.name());

        orderApprovalDAO.insertApprovalHistory(oracleConn, approvalDTO);

        // 3. MariaDB 외부 공급사 접수증 생성
        OrderRequestDTO orderRequest = orderRequestDAO.findByOrderRequestId(oracleConn, orderRequestId);

        String storeName = orderRequestDAO.findStoreNameByStoreId(orderRequest.getStoreId());
        ExternalOrderReceiptDTO receiptDTO = new ExternalOrderReceiptDTO();
        receiptDTO.setSupplierId(orderRequest.getSupplierIntegrationId());
        receiptDTO.setSupplierProductId(orderRequest.getProductId());
        receiptDTO.setInternalOrderRequestId(orderRequest.getOrderRequestId());
        receiptDTO.setRequestStoreName(storeName);
        receiptDTO.setRequestQuantity(orderRequest.getOrderQuantity());
        receiptDTO.setApprovedQuantity(approvedQuantity);
        receiptDTO.setReceiptStatus("RECEIVED");

        externalOrderReceiptDAO.insertExternalReceipt(mariaConn, receiptDTO);

        // 4. 두 DB 작업 모두 성공하면 commit
        oracleConn.commit();
        mariaConn.commit();

      } catch (SQLException | RuntimeException e) {
        rollback(oracleConn);
        rollback(mariaConn);
        throw e;
      }
    }
  }

  /*
  발주 반려 프로세스
     반려 사유 검증 후
     내부 시스템 Oracle에서 발주 상태를 REJECTED로 변경하고 반려 이력을 저장한다.
  */
  public void rejectOrder(long orderRequestId, String rejectReason, long employeeId) throws SQLException {
    if(rejectReason == null || rejectReason.trim().isEmpty())
      throw new InputException("반려 사유를 반드시 입력해야 합니다.");

    OrderRequestDTO requestedOrder = orderRequestDAO.findByOrderRequestId(orderRequestId);
    if (requestedOrder == null) {
      throw new InputException("발주 요청을 찾을 수 없습니다.");
    }
    if (!OrderStatus.REQUESTED.name().equals(requestedOrder.getOrderStatus())) {
      throw new InputException("요청 상태의 발주만 반려할 수 있습니다.");
    }

    try(Connection oracleConn = DBConnection.getConnection(DBType.ORACLE);){
      oracleConn.setAutoCommit(false);

      // 1. Oracle 발주 요청 상태 REJECTED 변경
      try{
        orderRequestDAO.updateRejectStatus(
            oracleConn,
            orderRequestId,
            rejectReason,
            employeeId
        );
      // 2. Oracle 반려 이력 저장
        OrderApprovalDTO approvalDTO = new OrderApprovalDTO();
        approvalDTO.setApprovalEmployeeId(employeeId);
        approvalDTO.setOrderRequestId(orderRequestId);
        approvalDTO.setApprovalStatus(OrderStatus.REJECTED.name());
        approvalDTO.setApprovalComment(rejectReason);

        orderApprovalDAO.insertApprovalHistory(oracleConn, approvalDTO);
        oracleConn.commit();
      }catch (SQLException | RuntimeException e) {
        rollback(oracleConn);
        throw e;
      }
    }
  }

  private void rollback(Connection conn) {
    if (conn != null) {
      try {
        conn.rollback();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
