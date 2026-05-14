package inventory.store;

import java.sql.SQLException;
import java.util.List;
import exception.InputException;
import inventory.InventoryDTO;

public class InventoryService {

  private final InventoryDAO inventoryDAO = new InventoryDAO();

  // [INV-01] 내 매장 전체 재고 목록 조회
  public List<InventoryDTO> getInventoryList(int storeId) throws SQLException {
    return inventoryDAO.searchInventory(storeId, null, null, null, false);
  }

  // [INV-02] 안전재고 미만 상품만 조회
  public List<InventoryDTO> getLowStockList(int storeId) throws SQLException {
    return inventoryDAO.searchInventory(storeId, null, null, null, true);
  }

  // [INV-03] 재고 검색 (브랜드명 / 카테고리명 / 상품명 키워드)
  public List<InventoryDTO> searchInventory(int storeId, String brandName, String categoryName,
      String keyword) throws SQLException {
    return inventoryDAO.searchInventory(storeId, brandName, categoryName, keyword, false);
  }

  // [INV-04] 안전재고 수량 변경
  public boolean updateSafetyQuantity(int storeId, int productId, int newSafetyQty)
      throws SQLException {
    if (newSafetyQty < 0) {
      throw new InputException("안전재고 수량은 0 이상이어야 합니다.");
    }
    int result = inventoryDAO.updateSafetyQuantity(storeId, productId, newSafetyQty);
    return result > 0;
  }
}
