package store.sales;

import common.DBConnection;
import common.type.DBType;
import exception.MismatchQuantityException;
import inventory.InventoryDTO;
import inventory.InventoryService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import product.ProductDAO;
import store.StoreDAO;
import store.inventory.StoreInventoryDAO;

public class StoreSalesService {
  private final StoreInventoryDAO storeInventoryDAO = new StoreInventoryDAO();
  private final InventoryService inventoryService = new InventoryService();
  private final StoreDAO storeDAO = new StoreDAO();
  private final ProductDAO productDAO = new ProductDAO();

  public List<String> findSaleTargetSummaries(int storeId) {
    List<String> result = new ArrayList<>();
    List<InventoryDTO> inventories = inventoryService.getInventoryList(storeId);

    for (InventoryDTO inventoryDTO : inventories) {
      String text = "";
      text += "상품ID " + inventoryDTO.getProductId();
      text += " | " + inventoryDTO.getProductName();
      text += " | 현재수량 " + inventoryDTO.getCurrentQuantity();
      text += " | 안전재고 " + inventoryDTO.getSafetyQuantity();
      text += " | 브랜드 " + inventoryDTO.getBrandName();
      result.add(text);
    }

    return result;
  }

  public String getSaleState(long storeId, long productId, int quantity) throws SQLException {
    String text = "";
    text += "매장: " + storeDAO.findStoreNameById(storeId) + "\n";
    text += "상품: " + productDAO.findProductNameById(productId) + "\n";
    text += "판매수량: " + quantity + "\n";
    text += "현재수량: " + nullable(storeInventoryDAO.findCurrentQuantity(storeId, productId));

    return text;
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

  private String nullable(Object value) {
    if (value == null) {
      return "-";
    }

    return String.valueOf(value);
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
