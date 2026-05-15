package order.external;

import common.DBConnection;
import common.TransactionHelper;
import common.type.DBType;
import common.type.External_OrderStatus;
import common.type.OrderStatus;
import exception.InputException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import order.request.OrderRequestDAO;
import order.request.OrderRequestDTO;

@RequiredArgsConstructor
public class ExternalOrderService {

  private final ExternalOrderReceiptDAO receiptDAO;
  private final ExternalSupplierInventoryDAO inventoryDAO;
  private final OrderRequestDAO orderRequestDAO;

  public ExternalOrderService() {
    this(new ExternalOrderReceiptDAO(), new ExternalSupplierInventoryDAO(), new OrderRequestDAO());
  }

  public List<ExternalOrderReceiptDTO> getExternalOrderReceipts() throws SQLException {
    try (Connection mariaConn = DBConnection.getConnection(DBType.MARIADB)) {
      return receiptDAO.findAll(mariaConn);
    }
  }

  public ExternalOrderReceiptDTO getExternalOrderReceipt(long orderRequestId) throws SQLException {
    try (Connection mariaConn = DBConnection.getConnection(DBType.MARIADB)) {
      return receiptDAO.findByInternalOrderRequestId(mariaConn, orderRequestId);
    }
  }

  public String findExternalOrderStatus(long orderRequestId) throws SQLException {
    ExternalOrderReceiptDTO receipt = getExternalOrderReceipt(orderRequestId);
    if (receipt == null) {
      return "-";
    }

    return External_OrderStatus.valueOf(receipt.getReceiptStatus()).getDisplayName();
  }

  // 발주 목록 보기 (+ 재고 조회)
  public List<SupplierDashboardDTO> getSupplierDashboard() throws SQLException {
    List<SupplierDashboardDTO> list = new ArrayList<>();

    try(Connection mariaConn = DBConnection.getConnection(DBType.MARIADB))
    {
      List<ExternalOrderReceiptDTO> receipts =
          receiptDAO.findAllByStatus(mariaConn, External_OrderStatus.RECEIVED);

      for (ExternalOrderReceiptDTO receipt : receipts) {

        // 2. 공급처 상품 ID 기준으로 실시간 재고 조회
        ExternalSupplierInventoryDTO inventory = inventoryDAO.findInventoryById(
            mariaConn,
            receipt.getSupplierProductId()
        );

        // 3. 대시보드 DTO 구성
        int currentStock = 0;

        if (inventory != null) {
          currentStock = inventory.getQuantity();
        }

        SupplierDashboardDTO dto = new SupplierDashboardDTO();
        dto.setOrderRequestId(receipt.getInternalOrderRequestId());
        dto.setProductId(receipt.getSupplierProductId());
        dto.setApprovedQuantity(receipt.getApprovedQuantity());
        dto.setCurrentStock(currentStock);
        dto.setExternalStatus(receipt.getReceiptStatus());

        list.add(dto);
      }
    }
      return list;
  }

  //출고처리
  public void processShipping(long orderRequestId, long supplierProductId, int quantity)
      throws SQLException {

    TransactionHelper.executeTwo(
        DBType.MARIADB,
        DBType.ORACLE,
        (mariaConn, oracleConn) -> {

          // 1. MariaDB 접수증 상태 SHIPPED 변경
          int receiptUpdatedCount = receiptDAO.updateStatus(
              mariaConn,
              orderRequestId,
              External_OrderStatus.SHIPPED
          );

          if (receiptUpdatedCount == 0) {
            throw new SQLException("출고 처리할 수 없습니다. 접수증이 없거나 이미 처리된 상태입니다.");
          }

          // 2. MariaDB 공급처 재고 차감
          inventoryDAO.decreaseStock(mariaConn, supplierProductId, quantity);

          // 3. Oracle 발주 상태 RECEIVED 변경
          int orderUpdatedCount = orderRequestDAO.updateStatus(
              oracleConn,
              orderRequestId,
              OrderStatus.RECEIVED
          );

          if (orderUpdatedCount == 0) {
            throw new SQLException("Oracle 발주 요청 상태 변경에 실패했습니다.");
          }

          return null;
        }
    );
  }

  // 출고 거절
  public void rejectShipping(long orderRequestId) throws SQLException {
    rejectShipping(orderRequestId, null);
  }

  public void rejectShipping(long orderRequestId, String rejectReason) throws SQLException {
    if (rejectReason != null && rejectReason.isBlank()) {
      throw new InputException("거절 사유를 입력해 주세요.");
    }

    TransactionHelper.executeTwo(
        DBType.MARIADB,
        DBType.ORACLE,
        (mariaConn, oracleConn) -> {

          // 1. MariaDB 접수증 상태 REJECTED 변경
          int receiptUpdatedCount = receiptDAO.updateStatus(
              mariaConn,
              orderRequestId,
              External_OrderStatus.REJECTED
          );

          if (receiptUpdatedCount == 0) {
            throw new SQLException("거절 처리할 수 없습니다. 접수증이 없거나 이미 처리된 상태입니다.");
          }

          // 2. Oracle 발주 상태 CANCELED 변경
          int orderUpdatedCount = orderRequestDAO.updateCanceledByExternalReject(
              oracleConn,
              orderRequestId,
              rejectReason
          );

          if (orderUpdatedCount == 0) {
            throw new SQLException("Oracle 발주 요청 상태 변경에 실패했습니다.");
          }

          return null;
        }
    );
  }

  // 출고 후 MariaDB 기록
  public void createExternalReceiptAfterApproval(
      Connection oracleConn,
      Connection mariaConn,
      long orderRequestId,
      int approvedQuantity
  ) throws SQLException {

    OrderRequestDTO orderRequest =
        orderRequestDAO.findByOrderRequestId(oracleConn, orderRequestId);

    if (orderRequest == null) {
      throw new SQLException("발주 요청을 찾을 수 없습니다.");
    }

    String StoreName = orderRequestDAO.findStoreNameByStoreId(orderRequest.getStoreId());
    ExternalOrderReceiptDTO receiptDTO = new ExternalOrderReceiptDTO();
    receiptDTO.setSupplierId(orderRequest.getSupplierIntegrationId());
    receiptDTO.setSupplierProductId(orderRequest.getProductId());
    receiptDTO.setInternalOrderRequestId(orderRequest.getOrderRequestId());
    receiptDTO.setRequestStoreName(StoreName);
    receiptDTO.setRequestQuantity(orderRequest.getOrderQuantity());
    receiptDTO.setApprovedQuantity(approvedQuantity);
    receiptDTO.setReceiptStatus(External_OrderStatus.RECEIVED.name());

    receiptDAO.insertExternalReceipt(mariaConn, receiptDTO);
  }
}
