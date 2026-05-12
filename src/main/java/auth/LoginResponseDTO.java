package auth;

import role.RoleType;
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

  private long roleId;
  private String roleName;
  private RoleType roleType;
}
