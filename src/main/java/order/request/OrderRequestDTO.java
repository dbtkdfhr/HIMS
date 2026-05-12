package order.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderRequestDTO {

  private long orderRequestId;
  private long storeId;
  private long supplierIntegrationId;
  private long productId;
  private long requestEmployeeId;
  private Long approvalEmployeeId; // long과 Long은 not null, nullable 차이를 위해 분리
  private Long approvalRoleId;
  private String externalOrderId;
  private int orderQuantity;
  private Integer approvedQuantity;
  private String requestReason;
  private String rejectReason;
  private String orderStatus; // REQUESTED, APPROVED, REJECTED, SENT, RECEIVED, CANCELED
  private LocalDateTime requestedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;
  private LocalDateTime sentToSupplierAt;
}
