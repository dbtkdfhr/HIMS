package inventory;

import common.type.RoleType;
import employee.EmployeeDTO;
import exception.InputException;
import exception.MismatchQuantityException;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {
  private final InventoryDAO inventoryDAO;

  public InventoryService() {
    this(new InventoryDAO());
  }

  public InventoryService(InventoryDAO inventoryDAO) {
    this.inventoryDAO = inventoryDAO;
  }

  /**
   * 사용자의 권한에 따른 재고 목록 조회
   */
  public List<InventoryDTO> getInventoryList(EmployeeDTO loginUser) throws SQLException {
    return searchInventory(loginUser, null, null, null, null, false);
  }

  /**
   * 사용자의 권한에 따른 재고 부족 상품 목록 조회
   */
  public List<InventoryDTO> getLowStockList(EmployeeDTO loginUser) throws SQLException {
    return searchInventory(loginUser, null, null, null, null, true);
  }

  /**
   * 사용자가 권한에 따른 재고 검색
   */
  public List<InventoryDTO> searchInventory(EmployeeDTO loginUser, String brandName,
      String categoryName, String storeName, String productName, boolean isLowStockOnly)
      throws SQLException {

    RoleType role = RoleType.fromRoleId(loginUser.getRoleId());

    switch (role) {
      case STORE_MANAGER:
        validateStoreId(loginUser.getStoreId());
        // 매장 관리자는 본인 매장 정보로만 조회 (storeName 필터 무시)
        return inventoryDAO.searchInventory(null, loginUser.getStoreId().intValue(), brandName,
            categoryName, null, productName, isLowStockOnly);

      case BRANCH_MANAGER:
        validateBranchId(loginUser.getBranchId());
        // 지점 관리자는 본인 지점 정보로 소속 매장들 조회
        return inventoryDAO.searchInventory(loginUser.getBranchId(), null, brandName, categoryName,
            storeName, productName, isLowStockOnly);

      case SYSTEM_MANAGER:
        // 시스템 관리자는 필터 없이 전국 조회 가능
        return inventoryDAO.searchInventory(null, null, brandName, categoryName, storeName,
            productName, isLowStockOnly);

      default:
        throw new IllegalStateException("해당 권한으로는 재고 조회를 할 수 없습니다: " + role.getRoleName());
    }
  }

  /**
   * 안전재고 수량 변경 (매장 관리자 전용)
        
   */
  public boolean updateSafetyQuantity(EmployeeDTO loginUser, int storeId, int productId,
      int newSafetyQty) throws SQLException {
    validateNonNegativeQuantity(newSafetyQty, "안전재고 수량");

    RoleType role = RoleType.fromRoleId(loginUser.getRoleId());
    if (role == RoleType.STORE_MANAGER && (loginUser.getStoreId() == null || loginUser.getStoreId() != storeId)) {
      throw new InputException("본인 매장의 안전재고만 수정할 수 있습니다.");
    }

    int result = inventoryDAO.updateSafetyQuantity(storeId, productId, newSafetyQty);
    return result > 0;
  }

  /**
   * 현재재고 수량 변경 (지점/시스템 관리자 권한)
   */
  public boolean updateCurrentQuantity(EmployeeDTO loginUser, int storeId, int productId,
      int newCurrentQty) throws SQLException {
    validateNonNegativeQuantity(newCurrentQty, "현재재고 수량");

    RoleType role = RoleType.fromRoleId(loginUser.getRoleId());
    // 일반 직원은 수정 불가
    if (role == RoleType.STAFF) {
      throw new InputException("재고 수량을 변경할 권한이 없습니다.");
    }

    int result = inventoryDAO.updateCurrentQuantity(storeId, productId, newCurrentQty);
    return result > 0;
  }

  private void validateStoreId(Long storeId) {
    if (storeId == null || storeId <= 0) {
      throw new IllegalArgumentException("매장 정보가 올바르지 않습니다.");
    }
  }

  private void validateBranchId(Long branchId) {
    if (branchId == null || branchId <= 0) {
      throw new IllegalArgumentException("지점 정보가 올바르지 않습니다.");
    }
  }

  private void validateNonNegativeQuantity(int quantity, String fieldName) {
    if (quantity < 0) {
      throw new MismatchQuantityException(fieldName + "은 0 이상이어야 합니다.");
    }
  }
}
