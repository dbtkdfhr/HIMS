package auth.dto;

import role.type.RoleType;
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

  private Long employeeId;
  private String loginId;
  private String employeeName;

  private Long roleId;
  private String roleName;
  private RoleType roleType;
}
