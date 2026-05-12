package auth;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class LoginEmployeeDTO {

  private long employeeId;
  private String loginId;
  private String password;
  private String employeeName;
  private String phoneNumber;

  private int roleId;
  private String roleName;
  private Long storeId;

  private String isActive;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
