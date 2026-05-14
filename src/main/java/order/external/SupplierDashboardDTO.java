package order.external;

import lombok.Data;

@Data
public class SupplierDashboardDTO {
  private long orderRequestId;
  private long productId;
  private int approvedQuantity;
  private int currentStock;
  private String externalStatus;
}
