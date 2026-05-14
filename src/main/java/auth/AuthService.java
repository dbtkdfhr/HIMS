package auth;

import common.type.RoleType;
import employee.EmployeeDAO;
import employee.EmployeeDTO;
import exception.DuplicateException;
import exception.NotFoundException;
import exception.NotReceptableException;
import java.sql.SQLException;

public class AuthService {
  private static final int MIN_PASSWORD_LENGTH = 4;

  private final EmployeeDAO employeeDAO;

  public AuthService(EmployeeDAO employeeDAO) {
    this.employeeDAO = employeeDAO;
  }

  public AuthService() {
    this(new EmployeeDAO());
  }

  public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws SQLException {
    LoginEmployeeDTO employee;

    try {
      employee = employeeDAO.getEmployeeByLoginId(loginRequestDTO.getLoginId());
    } catch (SQLException e) {
      throw e;
    }

    if (employee == null) {
      throw new NotFoundException("존재하지 않는 ID입니다.");
    }

    if (!employee.getPassword().equals(loginRequestDTO.getPassword())) {
      throw new DuplicateException("아이디 혹은 비밀번호가 일치하지 않습니다.");
    }

    if (!"Y".equals(employee.getIsActive())) {
      throw new NotReceptableException("시스템 관리자에게 문의하세요.");
    }

    RoleType roleType = RoleType.fromRoleId(employee.getRoleId());

    System.out.println(roleType);

    return new LoginResponseDTO(
        employee.getEmployeeId(),
        employee.getLoginId(),
        employee.getEmployeeName(),
        employee.getPhoneNumber(),
        employee.getRoleId(),
        employee.getRoleName(),
        roleType,
        employee.getStoreId(),
        employee.getBranchId(),
        employee.getIsActive()
    );
  }

  public void changePassword(PasswordChangeRequestDTO passwordChangeRequestDTO) throws SQLException {
    validatePasswordChangeRequest(passwordChangeRequestDTO);

    LoginEmployeeDTO loginEmployee = employeeDAO.getEmployeeByLoginId(
        passwordChangeRequestDTO.getLoginId()
    );

    if (loginEmployee == null) {
      throw new NotFoundException("존재하지 않는 사용자입니다.");
    }

    if (!loginEmployee.getPassword().equals(passwordChangeRequestDTO.getCurrentPassword())) {
      throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
    }

    if (loginEmployee.getPassword().equals(passwordChangeRequestDTO.getNewPassword())) {
      throw new IllegalArgumentException("기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
    }

    EmployeeDTO employee = new EmployeeDTO();
    employee.setEmployeeId(loginEmployee.getEmployeeId());
    employee.setPassword(passwordChangeRequestDTO.getNewPassword());

    int result = employeeDAO.updatePassword(employee);
    if (result == 0) {
      throw new NotFoundException("존재하지 않는 사용자입니다.");
    }
  }

  private void validatePasswordChangeRequest(PasswordChangeRequestDTO passwordChangeRequestDTO) {
    if (passwordChangeRequestDTO == null) {
      throw new IllegalArgumentException("비밀번호 변경 정보는 필수입니다.");
    }

    validateNotBlank(passwordChangeRequestDTO.getLoginId(), "로그인 ID");
    validateNotBlank(passwordChangeRequestDTO.getCurrentPassword(), "현재 비밀번호");
    validateNotBlank(passwordChangeRequestDTO.getNewPassword(), "새 비밀번호");
    validateNotBlank(passwordChangeRequestDTO.getConfirmPassword(), "새 비밀번호 확인");

    if (passwordChangeRequestDTO.getNewPassword().length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException("비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
    }

    if (!passwordChangeRequestDTO.getNewPassword()
        .equals(passwordChangeRequestDTO.getConfirmPassword())) {
      throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
    }
  }

  private void validateNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
    }
  }
}
