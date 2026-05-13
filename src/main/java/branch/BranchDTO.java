package branch;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BranchDTO {
  private long branchId;
  private String branchName;
  private String address;
  private String phoneNumber;
  private String operationStatus;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
