package auth;

import common.Session;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthController {

  private final Scanner sc = new Scanner(System.in);
  private final AuthService authService = new AuthService();

  public void login() {
    System.out.println("로그인 ID : ");
    String loginId = sc.nextLine();

    System.out.println("비밀번호 : ");
    String password = sc.nextLine();

    LoginRequestDTO loginRequestDTO = new LoginRequestDTO(loginId, password);

    try{
      LoginResponseDTO loginUser = authService.login(loginRequestDTO);

      Session.login(loginUser);

      System.out.println();
      System.out.println(loginUser);
      System.out.println("권한 : " + loginUser.getRoleName());
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println(e.getMessage());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void logout() {
    if (!Session.isLoggedIn()){
      System.out.println("현재 로그인한 사용자가 존재하지 않습니다. ");
    }

    Session.logout();
    System.out.println("로그아웃 완료");
  }
}
