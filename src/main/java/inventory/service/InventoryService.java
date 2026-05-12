package inventory.service;

import inventory.dao.InventoryDAO;
import inventory.dto.InventoryDTO;
import java.util.List;

public class InventoryService {

  private final InventoryDAO inventoryDAO = new InventoryDAO();


  // [INV-01] 내 매장 전체 재고 목록 조회
  public List<InventoryDTO> getInventoryList(int storeId) {
    return inventoryDAO.searchInventory(storeId, null, null, null, false);
  }


}
