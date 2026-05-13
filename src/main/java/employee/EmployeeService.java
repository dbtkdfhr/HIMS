package employee;

import common.type.RoleType;
import exception.DuplicateException;
import exception.NotFoundException;
import java.sql.SQLException;
import java.util.List;

public class EmployeeService {
  private static final int MIN_PASSWORD_LENGTH = 4;

  private final EmployeeDAO employeeDAO;

  public EmployeeService(EmployeeDAO employeeDAO) {
    this.employeeDAO = employeeDAO;
  }

  public List<String> findAllEmployeeSummaries() throws SQLException {
    return employeeDAO.findAllEmployeeSummaries();
  }

  public int registerEmployee(EmployeeDTO employee) throws SQLException {
    try {
      validateRequired(employee);
      validateDuplicateLoginId(employee.getLoginId());

      RoleType roleType = RoleType.fromRoleId(employee.getRoleId());

      if (roleType == RoleType.STORE_MANAGER) {
        validateStoreId(employee.getStoreId());
      } else {
        employee.setStoreId(null);
      }

      return employeeDAO.insertEmployee(employee);
    } catch (SQLException | RuntimeException e) {
      throw e;
    }
  }

  public int updatePassword(EmployeeDTO employee) throws SQLException {
    try {
      validateEmployeeId(employee);
      validateNotBlank(employee.getPassword(), "비밀번호");
      validatePasswordLength(employee.getPassword());
      validateDifferentPassword(employee.getEmployeeId(), employee.getPassword());

      return employeeDAO.updatePassword(employee);
    } catch (SQLException | RuntimeException e) {
      throw e;
    }
  }

  private void validateRequired(EmployeeDTO employee) {
    if (employee == null) {
      throw new IllegalArgumentException("직원 정보는 필수입니다.");
    }

    validateNotBlank(employee.getLoginId(), "로그인 ID");
    validateNotBlank(employee.getPassword(), "비밀번호");
    validateNotBlank(employee.getEmployeeName(), "직원명");
    validateNotBlank(employee.getPhoneNumber(), "연락처");
  }

  private void validateEmployeeId(EmployeeDTO employee) {
    if (employee == null) {
      throw new IllegalArgumentException("직원 정보는 필수입니다.");
    }

    if (employee.getEmployeeId() <= 0) {
      throw new IllegalArgumentException("직원 ID는 필수입니다.");
    }
  }

  private void validatePasswordLength(String password) {
    if (password.length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException("비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
    }
  }

  private void validateDifferentPassword(long employeeId, String newPassword) throws SQLException {
    String currentPassword = employeeDAO.findPasswordByEmployeeId(employeeId);

    if (currentPassword == null) {
      throw new NotFoundException("존재하지 않는 직원입니다.");
    }

    if (currentPassword.equals(newPassword)) {
      throw new IllegalArgumentException("기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
    }
  }

  private void validateStoreId(Long storeId) {
    if (storeId == null) {
      throw new IllegalArgumentException("입점매장담당자는 storeId가 필수입니다.");
    }
  }

  private void validateDuplicateLoginId(String loginId) throws SQLException {
    if (employeeDAO.existsLoginId(loginId)) {
      throw new DuplicateException("이미 사용 중인 로그인 ID입니다.");
    }
  }

  private void validateNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
    }
  }
}
