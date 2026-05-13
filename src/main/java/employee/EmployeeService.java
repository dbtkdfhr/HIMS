package employee;

import common.type.RoleType;
import java.sql.SQLException;
import java.util.List;

public class EmployeeService {
  private final EmployeeDAO employeeDAO = new EmployeeDAO();

  public List<String> findAllEmployeeSummaries() throws SQLException {
    return employeeDAO.findAllEmployeeSummaries();
  }

  public int registerEmployee(EmployeeDTO employee) throws SQLException {
    try {
      validateRequired(employee);

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

  private void validateRequired(EmployeeDTO employee) {
    if (employee == null) {
      throw new IllegalArgumentException("직원 정보는 필수입니다.");
    }

    validateNotBlank(employee.getLoginId(), "로그인 ID");
    validateNotBlank(employee.getPassword(), "비밀번호");
    validateNotBlank(employee.getEmployeeName(), "직원명");
    validateNotBlank(employee.getPhoneNumber(), "연락처");
  }

  private void validateStoreId(Long storeId) {
    if (storeId == null) {
      throw new IllegalArgumentException("입점매장담당자는 storeId가 필수입니다.");
    }
  }

  private void validateNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
    }
  }
}
