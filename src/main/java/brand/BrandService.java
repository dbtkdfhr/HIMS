package brand;

import java.sql.SQLException;
import java.util.List;

public class BrandService {

  private final BrandDAO brandDAO;

  public BrandService() {
    this(new BrandDAO());
  }

  public BrandService(BrandDAO brandDAO) {
    this.brandDAO = brandDAO;
  }

  public List<BrandDTO> findAllBrands() throws SQLException {
    return brandDAO.findAll();
  }
}
