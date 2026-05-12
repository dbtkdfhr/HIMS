package product;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductDTO {

  private long productId;
  private long brandId;
  private long categoryId;
  private String productName;
  private int price;
  private String seasonType;
  private String productStatus;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
