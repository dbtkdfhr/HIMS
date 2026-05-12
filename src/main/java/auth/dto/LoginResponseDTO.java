package auth.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {

  private Long employeeId;
  private String loginId;
  private String employeeName;
  private Long roleId;
  private String roleName;
}
