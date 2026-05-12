package auth;

import common.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginResponseDTO {

  private long employeeId;
  private String loginId;
  private String employeeName;
  private String phoneNumber;

  private int roleId;
  private String roleName;
  private RoleType roleType;

  private Long storeId;
  private String isActive;
}
