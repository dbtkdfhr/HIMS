package product;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

  private final ProductDAO productDAO;

  public ProductService() {
    this(new ProductDAO());
  }

  public ProductService(ProductDAO productDAO) {
    this.productDAO = productDAO;
  }

  public List<ProductDTO> findAllProducts() throws SQLException {
    return productDAO.findAll();
  }

  public String findProductNameById(long productId) throws SQLException {
    return productDAO.findProductNameById(productId);
  }

  public int registerProduct(ProductDTO productDTO) throws SQLException {
    if (productDTO == null) {
      throw new IllegalArgumentException("상품 정보는 필수입니다.");
    }
    if (productDTO.getBrandId() <= 0) {
      throw new IllegalArgumentException("브랜드 ID는 필수입니다.");
    }
    if (productDTO.getCategoryId() <= 0) {
      throw new IllegalArgumentException("카테고리 ID는 필수입니다.");
    }
    if (productDTO.getPrice() < 0) {
      throw new IllegalArgumentException("판매가는 0 이상이어야 합니다.");
    }
    validateNotBlank(productDTO.getProductName(), "상품명");
    validateNotBlank(productDTO.getProductStatus(), "상품상태");

    return productDAO.insertProduct(productDTO);
  }

  private void validateNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
    }
  }
}
