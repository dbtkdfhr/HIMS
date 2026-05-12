package employee.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EmployeeDTO {

  private Long employeeId;
  private Long roleId;
  private Long storeId;
  private String loginId;
  private String password;
  private String employeeName;
  private String phoneNumber;
  private String isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
