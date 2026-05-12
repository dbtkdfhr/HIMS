package store.receipt;

import common.DBConnection;
import common.type.DBType;
import common.type.ReceiptStatus;
import exception.DuplicateException;
import exception.MismatchQuantityException;
import exception.NotFoundException;
import exception.NotReceptableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import order.request.OrderRequestDAO;
import order.request.OrderRequestDTO;
import store.inventory.StoreInventoryDAO;

public class StoreReceiptService {

  private final StoreReceiptDAO storeReceiptDAO;
  private final StoreInventoryDAO storeInventoryDAO;
  private final OrderRequestDAO orderRequestDAO;

  public StoreReceiptService() {
    storeReceiptDAO = new StoreReceiptDAO();
    storeInventoryDAO = new StoreInventoryDAO();
    orderRequestDAO = new OrderRequestDAO();
  }

  public List<OrderRequestDTO> findReceiptTargetOrderRequests(long storeId) throws SQLException {
    return orderRequestDAO.findReceiptTargetOrderRequests(storeId);
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

    if (!orderRequestDTO.getOrderStatus().equals("SENT")) {
      throw new NotReceptableException("입고 처리 가능한 발주 상태가 아닙니다.");
    }

    if (storeReceiptDAO.existsByOrderRequestId(conn, orderRequestId)) {
      throw new DuplicateException("이미 입고 처리된 발주 요청입니다.");
    }

    return orderRequestDTO;
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
