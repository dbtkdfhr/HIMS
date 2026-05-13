package store;

import java.sql.SQLException;
import java.util.List;

public class StoreService {
  private final StoreDAO storeDAO = new StoreDAO();

  public List<String> findAllStoreSummaries() throws SQLException {
    return storeDAO.findAllStoreSummaries();
  }
}
