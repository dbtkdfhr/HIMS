package store.receipt;

import common.DBConnection;
import common.type.DBType;
import common.type.OrderStatus;
import common.type.ReceiptStatus;
import employee.EmployeeDAO;
import exception.DuplicateException;
import exception.MismatchQuantityException;
import exception.NotFoundException;
import exception.NotReceptableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import order.request.OrderRequestDAO;
import order.request.OrderRequestDTO;
import product.ProductDAO;
import store.StoreDAO;
import store.inventory.StoreInventoryDAO;

public class StoreReceiptService {

  private final StoreReceiptDAO storeReceiptDAO;
  private final StoreInventoryDAO storeInventoryDAO;
  private final OrderRequestDAO orderRequestDAO;
  private final StoreDAO storeDAO;
  private final ProductDAO productDAO;
  private final EmployeeDAO employeeDAO;

  public StoreReceiptService() {
    storeReceiptDAO = new StoreReceiptDAO();
    storeInventoryDAO = new StoreInventoryDAO();
    orderRequestDAO = new OrderRequestDAO();
    storeDAO = new StoreDAO();
    productDAO = new ProductDAO();
    employeeDAO = new EmployeeDAO();
  }

  public List<OrderRequestDTO> findReceiptTargetOrderRequests(long storeId) throws SQLException {
    return orderRequestDAO.findReceiptTargetOrderRequests(storeId);
  }

  public List<String> findReceiptTargetSummaries(long storeId) throws SQLException {
    List<String> result = new ArrayList<>();
    List<OrderRequestDTO> orderRequestDTOList = findReceiptTargetOrderRequests(storeId);

    for (OrderRequestDTO orderRequestDTO : orderRequestDTOList) {
      result.add(formatOrderRequest(orderRequestDTO));
    }

    return result;
  }

  public int receiveReceipt(long orderRequestId, long confirmEmployeeId, String differenceReason)
      throws SQLException {
    return processReceipt(orderRequestId, confirmEmployeeId, null, differenceReason,
        ReceiptStatus.RECEIVED);
  }

  public int partialReceiveReceipt(long orderRequestId, long confirmEmployeeId,
      int receivedQuantity,
      String differenceReason) throws SQLException {
    return processReceipt(orderRequestId, confirmEmployeeId, receivedQuantity, differenceReason,
        ReceiptStatus.PARTIAL_RECEIVED);
  }

  public int cancelReceipt(long orderRequestId, long confirmEmployeeId, String differenceReason)
      throws SQLException {
    return processReceipt(orderRequestId, confirmEmployeeId, null, differenceReason,
        ReceiptStatus.CANCELED);
  }

  private int processReceipt(long orderRequestId, long confirmEmployeeId, Integer receivedQuantity,
      String differenceReason, ReceiptStatus receiptStatus) throws SQLException {
    Connection conn = null;

    try {
      conn = DBConnection.getConnection(DBType.ORACLE);
      conn.setAutoCommit(false);

      OrderRequestDTO orderRequestDTO = findReceiptableOrderRequest(conn, orderRequestId);
      int expectedQuantity = getExpectedQuantity(orderRequestDTO);
      int actualReceivedQuantity = getActualReceivedQuantity(receiptStatus, receivedQuantity,
          expectedQuantity);
      int differenceQuantity = getDifferenceQuantity(receiptStatus, expectedQuantity,
          actualReceivedQuantity);
      StoreReceiptDTO storeReceiptDTO = createStoreReceiptDTO(
          orderRequestDTO,
          confirmEmployeeId,
          actualReceivedQuantity,
          differenceQuantity,
          differenceReason,
          receiptStatus.getLabel()
      );

      int insertCount = storeReceiptDAO.insertStoreReceipt(conn, storeReceiptDTO);

      if (shouldIncreaseInventory(receiptStatus, actualReceivedQuantity)) {
        increaseInventory(conn, orderRequestDTO, actualReceivedQuantity);
      }

      conn.commit();

      return insertCount;
    } catch (SQLException | RuntimeException e) {
      rollback(conn);
      throw e;
    } finally {
      DBConnection.close(conn);
    }
  }

  public List<StoreReceiptDTO> findReceiptHistory() throws SQLException {
    return storeReceiptDAO.findAll();
  }

  public List<StoreReceiptDTO> findReceiptHistoryByStoreId(long storeId) throws SQLException {
    return storeReceiptDAO.findByStoreId(storeId);
  }

  public List<String> findReceiptHistorySummaries(long storeId) throws SQLException {
    List<String> result = new ArrayList<>();
    List<StoreReceiptDTO> storeReceiptDTOList = findReceiptHistoryByStoreId(storeId);

    for (StoreReceiptDTO storeReceiptDTO : storeReceiptDTOList) {
      result.add(formatStoreReceipt(storeReceiptDTO));
    }

    return result;
  }

  public String getOrderRequestSummary(long orderRequestId) throws SQLException, NotFoundException {
    OrderRequestDTO orderRequestDTO = orderRequestDAO.findByOrderRequestId(orderRequestId);

    if (orderRequestDTO == null) {
      throw new NotFoundException("존재하지 않는 발주 요청입니다.");
    }

    return formatOrderRequest(orderRequestDTO);
  }

  public String getReceiptSummaryByOrderRequestId(long orderRequestId) throws SQLException {
    StoreReceiptDTO storeReceiptDTO = storeReceiptDAO.findByOrderRequestId(orderRequestId);

    if (storeReceiptDTO == null) {
      return "입고 확인 이력을 찾을 수 없습니다.";
    }

    return formatStoreReceipt(storeReceiptDTO);
  }

  public int processSale(long storeId, long productId, int quantity) throws SQLException {
    if (quantity <= 0) {
      throw new MismatchQuantityException("판매 수량은 1개 이상이어야 합니다.");
    }

    Connection conn = null;

    try {
      conn = DBConnection.getConnection(DBType.ORACLE);
      conn.setAutoCommit(false);

      int updateCount = storeInventoryDAO.decreaseQuantity(conn, storeId, productId, quantity);

      if (updateCount == 0) {
        throw new MismatchQuantityException("판매 가능한 재고가 부족하거나 재고 정보가 없습니다.");
      }

      conn.commit();

      return updateCount;
    } catch (SQLException | RuntimeException e) {
      rollback(conn);
      throw e;
    } finally {
      DBConnection.close(conn);
    }
  }

  private OrderRequestDTO findReceiptableOrderRequest(Connection conn, long orderRequestId)
      throws SQLException {
    OrderRequestDTO orderRequestDTO = orderRequestDAO.findByOrderRequestId(conn, orderRequestId);

    if (orderRequestDTO == null) {
      throw new NotFoundException("존재하지 않는 발주 요청입니다.");
    }

    if (!orderRequestDTO.getOrderStatus().equals(OrderStatus.SENT.name())) {
      throw new NotReceptableException("입고 처리 가능한 발주 상태가 아닙니다.");
    }

    if (storeReceiptDAO.existsByOrderRequestId(conn, orderRequestId)) {
      throw new DuplicateException("이미 입고 처리된 발주 요청입니다.");
    }

    return orderRequestDTO;
  }

  private String formatOrderRequest(OrderRequestDTO dto) throws SQLException {
    String text = "";
    text += "발주요청ID: " + dto.getOrderRequestId() + "\n";
    text += "매장: " + storeDAO.findStoreNameById(dto.getStoreId()) + "\n";
    text += "상품: " + productDAO.findProductNameById(dto.getProductId()) + "\n";
    text += "요청직원: " + employeeDAO.findEmployeeNameById(dto.getRequestEmployeeId()) + "\n";
    text += "발주수량: " + dto.getOrderQuantity() + "\n";
    text += "승인수량: " + nullable(dto.getApprovedQuantity()) + "\n";
    text += "발주상태: " + dto.getOrderStatus() + "\n";
    text += "요청사유: " + nullable(dto.getRequestReason()) + "\n";
    text += "반려사유: " + nullable(dto.getRejectReason()) + "\n";
    text += "외부발주접수ID: " + nullable(dto.getExternalOrderId()) + "\n";
    text += "외부전송일자: " + nullable(dto.getSentToSupplierAt());

    return text;
  }

  private String formatStoreReceipt(StoreReceiptDTO dto) throws SQLException {
    OrderRequestDTO orderRequestDTO = orderRequestDAO.findByOrderRequestId(dto.getOrderRequestId());

    String text = "";
    text += "입고확인ID: " + dto.getStoreReceiptId() + "\n";
    text += "발주요청ID: " + dto.getOrderRequestId() + "\n";

    if (orderRequestDTO != null) {
      text += "매장: " + storeDAO.findStoreNameById(orderRequestDTO.getStoreId()) + "\n";
      text += "상품: " + productDAO.findProductNameById(orderRequestDTO.getProductId()) + "\n";
    }

    text += "확인직원: " + employeeDAO.findEmployeeNameById(dto.getConfirmEmployeeId()) + "\n";
    text += "입고수량: " + dto.getReceivedQuantity() + "\n";
    text += "차이수량: " + dto.getDifferenceQuantity() + "\n";
    text += "차이사유: " + nullable(dto.getDifferenceReason()) + "\n";
    text += "입고상태: " + dto.getReceiptStatus() + "\n";
    text += "입고확인일자: " + nullable(dto.getCreatedAt());

    return text;
  }

  private String nullable(Object value) {
    if (value == null) {
      return "-";
    }

    return String.valueOf(value);
  }

  private StoreReceiptDTO createStoreReceiptDTO(OrderRequestDTO orderRequestDTO,
      long confirmEmployeeId, int receivedQuantity, int differenceQuantity, String differenceReason,
      String receiptStatus) {
    StoreReceiptDTO storeReceiptDTO = new StoreReceiptDTO();
    storeReceiptDTO.setOrderRequestId(orderRequestDTO.getOrderRequestId());
    storeReceiptDTO.setConfirmEmployeeId(confirmEmployeeId);
    storeReceiptDTO.setReceivedQuantity(receivedQuantity);
    storeReceiptDTO.setDifferenceQuantity(differenceQuantity);
    storeReceiptDTO.setDifferenceReason(differenceReason);
    storeReceiptDTO.setReceiptStatus(receiptStatus);

    return storeReceiptDTO;
  }

  private int getExpectedQuantity(OrderRequestDTO orderRequestDTO) {
    if (orderRequestDTO.getApprovedQuantity() != null) {
      return orderRequestDTO.getApprovedQuantity();
    }

    return orderRequestDTO.getOrderQuantity();
  }

  private int getActualReceivedQuantity(ReceiptStatus receiptStatus, Integer receivedQuantity,
      int expectedQuantity) {
    if (receiptStatus == ReceiptStatus.RECEIVED) {
      return expectedQuantity;
    }

    if (receiptStatus == ReceiptStatus.CANCELED) {
      return 0;
    }

    if (receivedQuantity == null || receivedQuantity < 0) {
      throw new MismatchQuantityException("입고 수량은 0개 이상이어야 합니다.");
    }

    if (receivedQuantity == expectedQuantity) {
      throw new MismatchQuantityException("부분 입고 수량은 승인 수량과 달라야 합니다.");
    }

    return receivedQuantity;
  }

  private int getDifferenceQuantity(ReceiptStatus receiptStatus, int expectedQuantity,
      int actualReceivedQuantity) {
    if (receiptStatus == ReceiptStatus.RECEIVED) {
      return 0;
    }

    return expectedQuantity - actualReceivedQuantity;
  }

  private boolean shouldIncreaseInventory(ReceiptStatus receiptStatus, int actualReceivedQuantity) {
    return receiptStatus != ReceiptStatus.CANCELED && actualReceivedQuantity > 0;
  }

  private void increaseInventory(Connection conn, OrderRequestDTO orderRequestDTO, int quantity)
      throws SQLException {
    int updateCount = storeInventoryDAO.increaseQuantity(
        conn,
        orderRequestDTO.getStoreId(),
        orderRequestDTO.getProductId(),
        quantity
    );

    if (updateCount == 0) {
      throw new NotFoundException("재고 정보가 존재하지 않습니다.");
    }
  }

  private void rollback(Connection conn) {
    if (conn == null) {
      return;
    }

    try {
      conn.rollback();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
