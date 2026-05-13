package inventory;

import java.util.List;
import exception.MismatchQuantityException;

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

  // [BM-INV-01] 지점 관리자 전체 입점매장 재고 목록 조회
  public List<InventoryDTO> getAllStoreInventoryList() {
    return inventoryDAO.searchAllStoreInventory(null, null, null, null, null, false);
  }

  // [BM-INV-02] 지점/브랜드/카테고리/매장/상품명 기준 검색
  public List<InventoryDTO> searchAllStoreInventory(String branchName, String brandName,
      String categoryName, String storeName, String productName) {
    return inventoryDAO.searchAllStoreInventory(
        branchName,
        brandName,
        categoryName,
        storeName,
        productName,
        false
    );
  }

  // [BM-INV-03] 재고 부족 상품만 조회
  public List<InventoryDTO> getAllStoreLowStockList() {
    return inventoryDAO.searchAllStoreInventory(null, null, null, null, null, true);
  }

  // [BM-INV-03] 검색 조건을 포함한 재고 부족 상품 조회
  public List<InventoryDTO> searchAllStoreLowStockInventory(String branchName, String brandName,
      String categoryName, String storeName, String productName) {
    return inventoryDAO.searchAllStoreInventory(
        branchName,
        brandName,
        categoryName,
        storeName,
        productName,
        true
    );
  }

  // [INV-04] 안전재고 수량 변경
  // 0 미만 입력 방지 유효성 검사 포함
  public boolean updateSafetyQuantity(int storeId, int productId, int newSafetyQty) {
    if (newSafetyQty < 0) {
      throw new MismatchQuantityException("안전재고 수량은 0 이상이어야 합니다.");
    }
    int result = inventoryDAO.updateSafetyQuantity(storeId, productId, newSafetyQty);
    return result > 0;
  }

  // 현재재고 수량 변경
  public boolean updateCurrentQuantity(int storeId, int productId, int newCurrentQty) {
    validateInventoryKey(storeId, productId);
    validateNonNegativeQuantity(newCurrentQty, "현재재고 수량");

    int result = inventoryDAO.updateCurrentQuantity(storeId, productId, newCurrentQty);
    return result > 0;
  }

  private void validateInventoryKey(int storeId, int productId) {
    if (storeId <= 0) {
      throw new IllegalArgumentException("매장 ID는 필수입니다.");
    }

    if (productId <= 0) {
      throw new IllegalArgumentException("상품 ID는 필수입니다.");
    }
  }

  private void validateNonNegativeQuantity(int quantity, String fieldName) {
    if (quantity < 0) {
      throw new MismatchQuantityException(fieldName + "은 0 이상이어야 합니다.");
    }
  }
}
