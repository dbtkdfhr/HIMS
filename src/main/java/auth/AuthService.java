package auth;

import common.type.RoleType;
import employee.EmployeeDAO;
import exception.DuplicateException;
import exception.NotFoundException;
import exception.NotReceptableException;
import java.sql.SQLException;

public class AuthService {

  private final EmployeeDAO employeeDAO;

  public AuthService() {
    employeeDAO = new EmployeeDAO();
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
        employee.getIsActive()
    );
  }
}
