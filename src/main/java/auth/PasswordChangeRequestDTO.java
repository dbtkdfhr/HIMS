package auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"currentPassword", "newPassword", "confirmPassword"})
public class PasswordChangeRequestDTO {

  private String loginId;
  private String currentPassword;
  private String newPassword;
  private String confirmPassword;

}