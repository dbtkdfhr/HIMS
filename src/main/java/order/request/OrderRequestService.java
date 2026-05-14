package order.request;

import common.type.OrderStatus;
import exception.InputException;
import exception.NotFoundException;
import java.sql.SQLException;
import java.util.List;

public class OrderRequestService {

  private final OrderRequestDAO orderRequestDAO = new OrderRequestDAO();

  public OrderRequestDTO getOrderRequest(long orderRequestId) throws SQLException {
    OrderRequestDTO orderRequestDTO = orderRequestDAO.findByOrderRequestId(orderRequestId);
    if (orderRequestDTO == null) {
      throw new NotFoundException("발주 요청을 찾을 수 없습니다.");
    }

    return orderRequestDTO;
  }

  // [STORE-ORDER-02] 발주 요청 생성
  public boolean createOrderRequest(OrderRequestDTO dto) throws SQLException {
    if (dto.getOrderQuantity() <= 0) {
      throw new InputException("발주 요청 수량은 1개 이상이어야 합니다.");
    }

    if (!orderRequestDAO.isProductExist(dto.getProductId())) {
      throw new NotFoundException("입력하신 상품 ID가 존재하지 않습니다.");
    }

    // 2. 발주처 연동 ID 자동 조회 및 세팅
    if (dto.getSupplierIntegrationId() <= 0) {
      Long supplierId = orderRequestDAO.findSupplierIdByProductId(dto.getProductId());
      if (supplierId == null) {
        throw new NotFoundException("해당 상품 브랜드와 연동된 발주처를 찾을 수 없습니다.");
      }
      dto.setSupplierIntegrationId(supplierId);
    }

    int result = orderRequestDAO.insertOrderRequest(dto);
    return result > 0;
  }

  // [STORE-ORDER-03] 내 발주 요청 목록 조회
  public List<OrderRequestDTO> getMyOrderRequests(long storeId) throws SQLException {
    return orderRequestDAO.findAllByStoreId(storeId);
  }

  public List<OrderRequestDTO> getAllOrderRequests() throws SQLException {
    return orderRequestDAO.findAll();
  }

  public List<OrderRequestDTO> getOrderRequestsByStatus(OrderStatus status) throws SQLException {
    if (status == null) {
      return getAllOrderRequests();
    }

    return orderRequestDAO.findAllByStatus(status);
  }

  public List<OrderRequestDTO> getOrdersBeforeDone(long storeId) throws SQLException {
    return orderRequestDAO.findByOrderRequestIdAndOrderStatus(storeId,
        OrderStatus.RECEIVED.name());
  }
}
