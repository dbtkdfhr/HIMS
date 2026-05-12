package inventory;

import java.util.List;

public class InventoryService {

  private final InventoryDAO inventoryDAO = new InventoryDAO();


  // [INV-01] 내 매장 전체 재고 목록 조회
  public List<InventoryDTO> getInventoryList(int storeId) {
    return inventoryDAO.searchInventory(storeId, null, null, null, false);
  }

  // [INV-02] 안전재고 미만 상품만 조회
  public List<InventoryDTO> getLowStockList(int storeId) {
    return inventoryDAO.searchInventory(storeId, null, null, null, true);
  }

  // [INV-03] 재고 검색 (브랜드명 / 카테고리명 / 상품명 키워드)
  public List<InventoryDTO> searchInventory(int storeId, String brandName, String categoryName,
      String keyword) {
    return inventoryDAO.searchInventory(storeId, brandName, categoryName, keyword, false);
  }

  // [INV-04] 안전재고 수량 변경
  // 0 미만 입력 방지 유효성 검사 포함
  public boolean updateSafetyQuantity(int storeId, int productId, int newSafetyQty) {
    if (newSafetyQty < 0) {
      System.out.println("[오류] 안전재고 수량은 0 이상이어야 합니다.");
      return false;
    }
    int result = inventoryDAO.updateSafetyQuantity(storeId, productId, newSafetyQty);
    return result > 0;
  }
}
