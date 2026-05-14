package category;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {
  private final CategoryDAO categoryDAO;

  public CategoryService() {
    this(new CategoryDAO());
  }

  public CategoryService(CategoryDAO categoryDAO) {
    this.categoryDAO = categoryDAO;
  }

  public List<CategoryDTO> getAllCategories() throws SQLException {
    return categoryDAO.getAllCategories();
  }

  public List<CategoryDTO> getCategoriesByLevel(int categoryLevel) throws SQLException {
    validateCategoryLevel(categoryLevel);

    return categoryDAO.getCategoriesByLevel(categoryLevel);
  }

  public List<CategoryDTO> getCategoriesByParentId(Long parentCategoryId) throws SQLException {
    validateParentCategoryId(parentCategoryId);

    return categoryDAO.getCategoriesByParentId(parentCategoryId);
  }

  private void validateCategoryLevel(int categoryLevel) {
    if (categoryLevel < 1) {
      throw new IllegalArgumentException("카테고리 레벨은 1 이상이어야 합니다.");
    }
  }

  private void validateParentCategoryId(Long parentCategoryId) {
    if (parentCategoryId != null && parentCategoryId <= 0) {
      throw new IllegalArgumentException("상위 카테고리 ID는 1 이상이어야 합니다.");
    }
  }
}
