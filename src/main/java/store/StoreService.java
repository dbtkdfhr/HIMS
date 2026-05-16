package store;

import java.sql.SQLException;
import java.util.List;

public class StoreService {
  private final StoreDAO storeDAO = new StoreDAO();

  public List<StoreDTO> findAllStores() throws SQLException {
    return storeDAO.findAll();
  }

  public List<StoreDTO> findStoresByBranchId(long branchId) throws SQLException {
    if (branchId <= 0) {
      throw new IllegalArgumentException("지점 ID는 필수입니다.");
    }

    return storeDAO.getStoresByBranchId(branchId);
  }

  public List<String> findAllStoreSummaries() throws SQLException {
    return storeDAO.findAllStoreSummaries();
  }

  public String findStoreNameById(long storeId) throws SQLException {
    return storeDAO.findStoreNameById(storeId);
  }

  public int registerStore(StoreDTO storeDTO) throws SQLException {
    if (storeDTO == null) {
      throw new IllegalArgumentException("매장 정보는 필수입니다.");
    }
    if (storeDTO.getBranchId() <= 0) {
      throw new IllegalArgumentException("지점 ID는 필수입니다.");
    }
    if (storeDTO.getBrandId() <= 0) {
      throw new IllegalArgumentException("브랜드 ID는 필수입니다.");
    }
    validateNotBlank(storeDTO.getStoreName(), "매장명");
    validateNotBlank(storeDTO.getOperationStatus(), "운영상태");

    return storeDAO.insertStore(storeDTO);
  }

  private void validateNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
    }
  }
}
