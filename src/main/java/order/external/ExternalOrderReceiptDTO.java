package order.external;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ExternalOrderReceiptDTO {
  private long externalOrderReceiptId;      // MariaDB PK
  private long internalOrderRequestId; // Oracle의 발주 요청 ID
  private String receiptStatus;        // RECEIVED, APPROVED, REJECTED
  private LocalDateTime createdAt;

  private long supplierId;
  private long supplierProductId;
  private String requestStoreName;
  private int requestQuantity;
  private int approvedQuantity;
}