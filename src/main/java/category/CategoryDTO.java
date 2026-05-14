package category;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CategoryDTO {

  private long categoryId;
  private Long parentCategoryId;
  private String categoryName;
  private int categoryLevel;
  private String isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
