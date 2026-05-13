package employee;

import java.sql.SQLException;
import java.util.List;

public class EmployeeService {
  private final EmployeeDAO employeeDAO = new EmployeeDAO();

  public List<String> findAllEmployeeSummaries() throws SQLException {
    return employeeDAO.findAllEmployeeSummaries();
  }
}
