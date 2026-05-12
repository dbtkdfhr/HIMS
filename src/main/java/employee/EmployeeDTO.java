package employee;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class EmployeeDTO {

  private long employeeId;
  private String loginId;
  private String password;
  private String employeeName;
  private String phoneNumber;

  private int roleId;
  private Long storeId;

  private String isActive;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
