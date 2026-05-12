package product;

import java.sql.Date;
import lombok.Data;

@Data
public class ProductDTO {

  private int productId;
  private int brandId;
  private int categoryId;
  private String productName;
  private int price;
  private String seasonType;
  private String productStatus;
  private Date createdAt;
  private Date updatedAt;
}
