package common;

import auth.LoginResponseDTO;
import lombok.Getter;

public class Session {

  @Getter
  private static LoginResponseDTO loginUser;

  private Session() {
  }

  public static void login(LoginResponseDTO user) {
    loginUser = user;
  }

  public static boolean isLoggedIn() {
    return loginUser != null;
  }

  public static void logout() {
    loginUser = null;
  }
}