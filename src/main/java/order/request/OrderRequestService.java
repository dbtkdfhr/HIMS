package order.request;

import java.sql.SQLException;
import java.util.List;
import exception.InputException;
import exception.NotFoundException;

public class OrderRequestService {

  private final OrderRequestDAO orderRequestDAO = new OrderRequestDAO();

  // [STORE-ORDER-02] 발주 요청 생성
  public boolean createOrderRequest(OrderRequestDTO dto) throws SQLException{
    if (dto.getOrderQuantity() <= 0) {
      throw new InputException("발주 요청 수량은 1개 이상이어야 합니다.");
    }

    try {
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
  }

  // [STORE-ORDER-03] 내 발주 요청 목록 조회
  public List<OrderRequestDTO> getMyOrderRequests(long storeId) throws SQLException{
    try {
      return orderRequestDAO.findAllByStoreId(storeId);
    }
  }
}
