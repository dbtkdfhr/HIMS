package order.approval;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderApprovalDTO {
  private long orderApprovalId;
  private long approvalEmployeeId;
  private long orderRequestId;
  private int approvedQuantity;
  private String approvalStatus;
  private String approvalComment;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
