package auth.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {

  private String loginId;
  private String password;
}
