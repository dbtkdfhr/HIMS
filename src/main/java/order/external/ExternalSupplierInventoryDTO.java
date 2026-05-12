package order.external;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ExternalSupplierInventoryDTO {
  private long supplierId;
  private long supplierProductId;
  private int quantity;
  private LocalDateTime createdAt;
}
