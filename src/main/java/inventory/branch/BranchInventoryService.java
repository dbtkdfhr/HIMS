package inventory.branch;

import inventory.InventoryDTO;
import java.util.List;

public class BranchInventoryService {

  private final BranchInventoryDAO branchInventoryDAO = new BranchInventoryDAO();

  // [BM-INV-01] 지점 관리자 전체 입점매장 재고 목록 조회
  public List<InventoryDTO> getAllStoreInventoryList() {
    return branchInventoryDAO.searchAllStoreInventory(null, null, null, null, null, false);
  }

  // [BM-INV-02] 지점/브랜드/카테고리/매장/상품명 기준 검색
  public List<InventoryDTO> searchAllStoreInventory(String branchName, String brandName,
      String categoryName, String storeName, String productName) {
    return branchInventoryDAO.searchAllStoreInventory(
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
    return branchInventoryDAO.searchAllStoreInventory(null, null, null, null, null, true);
  }

  // [BM-INV-03] 검색 조건을 포함한 재고 부족 상품 조회
  public List<InventoryDTO> searchAllStoreLowStockInventory(String branchName, String brandName,
      String categoryName, String storeName, String productName) {
    return branchInventoryDAO.searchAllStoreInventory(
        branchName,
        brandName,
        categoryName,
        storeName,
        productName,
        true
    );
  }
}
