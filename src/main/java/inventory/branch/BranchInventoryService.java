package inventory.branch;

import inventory.InventoryDTO;
import java.sql.SQLException;
import java.util.List;
import exception.MismatchQuantityException;

public class BranchInventoryService {

  private final BranchInventoryDAO branchInventoryDAO = new BranchInventoryDAO();

  // [BM-INV-01] 전국 모든 지점/매장 재고 목록 조회
  public List<InventoryDTO> getAllStoreInventoryList() throws SQLException {
    return getAllStoreInventoryList(null);
  }

  // [BM-INV-01] 특정 지점의 전체 매장 재고 목록 조회
  public List<InventoryDTO> getAllStoreInventoryList(Long branchId) throws SQLException {
    return branchInventoryDAO.searchAllStoreInventory(branchId, null, null, null, null, false);
  }

  // [BM-INV-02] 전국 단위 브랜드/매장/상품명 검색
  public List<InventoryDTO> searchAllStoreInventory(String brandName, String categoryName,
      String storeName, String productName) throws SQLException {
    return searchAllStoreInventory(null, brandName, categoryName, storeName, productName);
  }

  // [BM-INV-02] 특정 지점 기준 브랜드/매장/상품명 검색
  public List<InventoryDTO> searchAllStoreInventory(Long branchId, String brandName,
      String categoryName, String storeName, String productName) throws SQLException {
    return branchInventoryDAO.searchAllStoreInventory(branchId, brandName, categoryName, storeName,
        productName, false);
  }

  // [BM-INV-03] 전국 단위 재고 부족 상품 조회
  public List<InventoryDTO> getAllStoreLowStockList() throws SQLException {
    return getAllStoreLowStockList(null);
  }

  // [BM-INV-03] 특정 지점 내 재고 부족 상품 조회
  public List<InventoryDTO> getAllStoreLowStockList(Long branchId) throws SQLException {
    return branchInventoryDAO.searchAllStoreInventory(branchId, null, null, null, null, true);
  }

  // [BM-INV-03] 전국 단위 재고 부족 상품 검색 (조건 포함)
  public List<InventoryDTO> searchAllStoreLowStockInventory(String brandName, String categoryName,
      String storeName, String productName) throws SQLException {
    return searchAllStoreLowStockInventory(null, brandName, categoryName, storeName, productName);
  }

  // [BM-INV-03] 특정 지점 내 재고 부족 상품 검색 (조건 포함)
  public List<InventoryDTO> searchAllStoreLowStockInventory(Long branchId, String brandName,
      String categoryName, String storeName, String productName) throws SQLException {
    return branchInventoryDAO.searchAllStoreInventory(branchId, brandName, categoryName, storeName,
        productName, true);
  }

  // 현재재고 수량 변경
  public boolean updateCurrentQuantity(int storeId, int productId, int newCurrentQty)
      throws SQLException {
    validateInventoryKey(storeId, productId);
    validateNonNegativeQuantity(newCurrentQty, "현재재고 수량");

    int result = branchInventoryDAO.updateCurrentQuantity(storeId, productId, newCurrentQty);
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
