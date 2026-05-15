package employee;

import static common.GetNullableVariable.getNullableLocalDateTime;
import static common.GetNullableVariable.getNullableLong;
import static common.ResultSetUtils.hasColumn;
import static common.SetNullableVariable.setNullableLong;

import auth.LoginEmployeeDTO;
import common.DBConnection;
import common.type.DBType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class EmployeeDAO {
  /* SELECT */
  public String findEmployeeNameById(long employeeId) throws SQLException {
    String sql = "SELECT employee_name FROM EMPLOYEE WHERE employee_id = ?";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setLong(1, employeeId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("EMPLOYEE_NAME");
        }
      }
    }

    return "알 수 없음(" + employeeId + ")";
  }

  public List<String> findAllEmployeeSummaries() throws SQLException {
    List<String> list = new ArrayList<>();
    String sql =
        "SELECT " +
            "employee_id, " +
            "role_id, " +
            "store_id, " +
            "branch_id, " +
            "login_id, " +
            "employee_name, " +
            "phone_number, " +
            "is_active, " +
            "created_at, " +
            "updated_at " +
            "FROM EMPLOYEE " +
            "ORDER BY employee_id";

    try (Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        EmployeeDTO employee = mapEmployee(rs);
        list.add(employee.getEmployeeId() + ". " + employee.getEmployeeName()
            + " (" + employee.getLoginId() + ")");
      }
    }

    return list;
  }

  public boolean existsLoginId(String loginId) throws SQLException {
    String sql = "SELECT 1 FROM EMPLOYEE WHERE login_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setString(1, loginId);

      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  public String findPasswordByEmployeeId(long employeeId) throws SQLException {
    String sql = "SELECT password FROM EMPLOYEE WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setLong(1, employeeId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("PASSWORD");
        }
      }
    }

    return null;
  }

  public EmployeeDTO findEmployeeById(long employeeId) throws SQLException {
    String sql =
        "SELECT " +
            "employee_id, " +
            "role_id, " +
            "store_id, " +
            "branch_id, " +
            "login_id, " +
            "employee_name, " +
            "phone_number, " +
            "is_active, " +
            "created_at, " +
            "updated_at " +
            "FROM EMPLOYEE " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setLong(1, employeeId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapEmployee(rs);
        }
      }
    }

    return null;
  }

  // 전체 조회
  public List<EmployeeDTO> getEmployees() throws SQLException {
    List<EmployeeDTO> employees = new ArrayList<>();

    String sql =
        "SELECT " +
            "E.employee_id, " +
            "E.role_id, " +
            "E.store_id, " +
            "S.store_name, " +
            "E.branch_id, " +
            "B.branch_name, " +
            "E.login_id, " +
            "E.employee_name, " +
            "E.phone_number, " +
            "E.is_active, " +
            "E.created_at, " +
            "E.updated_at " +
            "FROM EMPLOYEE E " +
            "LEFT JOIN STORE S " +
            "ON E.store_id = S.store_id " +
            "LEFT JOIN BRANCH B " +
            "ON E.branch_id = B.branch_id " +
            "GROUP BY " +
            "E.role_id, " +
            "E.employee_id, " +
            "E.store_id, " +
            "S.store_name, " +
            "E.branch_id, " +
            "B.branch_name, " +
            "E.login_id, " +
            "E.employee_name, " +
            "E.phone_number, " +
            "E.is_active, " +
            "E.created_at, " +
            "E.updated_at " +
            "ORDER BY E.role_id, E.employee_id ";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()
    ) {

      while (rs.next()) {
        employees.add(mapEmployee(rs));
      }
    }

    return employees;
  }

  // 로그인 ID 기준 직원 조회
  public LoginEmployeeDTO getEmployeeByLoginId(String loginId) throws SQLException {
    String sql =
        "SELECT " +
            "e.employee_id, " +
            "e.role_id, " +
            "e.store_id, " +
            "e.branch_id, " +
            "e.login_id, " +
            "e.password, " +
            "e.employee_name, " +
            "e.phone_number, " +
            "e.is_active, " +
            "r.role_name " +
            "FROM EMPLOYEE e " +
            "JOIN ROLE r " +
            "ON e.role_id = r.role_id " +
            "WHERE e.login_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setString(1, loginId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          LoginEmployeeDTO employeeDTO = new LoginEmployeeDTO();

          employeeDTO.setEmployeeId(rs.getLong("EMPLOYEE_ID"));
          employeeDTO.setRoleId(rs.getInt("ROLE_ID"));
          employeeDTO.setRoleName(rs.getString("ROLE_NAME"));

          employeeDTO.setStoreId(getNullableLong(rs, "STORE_ID"));
          employeeDTO.setBranchId(getNullableLong(rs, "BRANCH_ID"));

          employeeDTO.setLoginId(rs.getString("LOGIN_ID"));
          employeeDTO.setPassword(rs.getString("PASSWORD"));
          employeeDTO.setEmployeeName(rs.getString("EMPLOYEE_NAME"));
          employeeDTO.setPhoneNumber(rs.getString("PHONE_NUMBER"));
          employeeDTO.setIsActive(rs.getString("IS_ACTIVE"));

          return employeeDTO;
        }
      }

    }

    return null;
  }

  /* INSERT */
  // 직원 추가
  public int insertEmployee(EmployeeDTO employee) throws SQLException {
    String sql =
        "INSERT INTO EMPLOYEE (" +
            "role_id, " +
            "store_id, " +
            "branch_id, " +
            "login_id, " +
            "password, " +
            "employee_name, " +
            "phone_number, " +
            "is_active" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setInt(1, employee.getRoleId());

      setNullableLong(pstmt, 2, employee.getStoreId());
      setNullableLong(pstmt, 3, employee.getBranchId());
      pstmt.setString(4, employee.getLoginId());
      pstmt.setString(5, employee.getPassword());
      pstmt.setString(6, employee.getEmployeeName());
      pstmt.setString(7, employee.getPhoneNumber());

      if (employee.getIsActive() == null) {
        pstmt.setString(8, "Y");
      } else {
        pstmt.setString(8, employee.getIsActive());
      }

      return pstmt.executeUpdate();

    }
  }

  /* UPDATE */
  // 직원 전체 정보 수정
  public int updateEmployee(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "employee_name = ?, " +
            "phone_number = ?, " +
            "role_id = ?, " +
            "store_id = ?, " +
            "branch_id = ?, " +
            "is_active = ?, " +
            "updated_at = SYSDATE " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setString(1, employee.getEmployeeName());
      pstmt.setString(2, employee.getPhoneNumber());
      pstmt.setInt(3, employee.getRoleId());

      setNullableLong(pstmt, 4, employee.getStoreId());
      setNullableLong(pstmt, 5, employee.getBranchId());
      pstmt.setString(6, employee.getIsActive());
      pstmt.setLong(7, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 직원명 수정
  public int updateEmployeeName(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "employee_name = ?, " +
            "updated_at = SYSDATE " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setString(1, employee.getEmployeeName());
      pstmt.setLong(2, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 연락처 수정
  public int updatePhoneNumber(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "phone_number = ?, " +
            "updated_at = SYSDATE " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setString(1, employee.getPhoneNumber());
      pstmt.setLong(2, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 권한 수정 및 소속 매장
  public int updateRoleAndStore(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "role_id = ?, " +
            "branch_id = ?, " +
            "store_id = ? " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, employee.getRoleId());
      setNullableLong(pstmt, 2, employee.getBranchId());
      setNullableLong(pstmt, 3, employee.getStoreId());
      pstmt.setLong(4, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 로그인 ID 수정
  public int updateLoginId(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "login_id = ? " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setString(1, employee.getLoginId());
      pstmt.setLong(2, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 비밀번호 수정
  public int updatePassword(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "password = ? " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setString(1, employee.getPassword());
      pstmt.setLong(2, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 직원 사용 중지 처리
  public int disableEmployee(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "is_active = 'N' " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setLong(1, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 직원 사용 재개 처리
  public int activateEmployee(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "is_active = 'Y' " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setLong(1, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  private EmployeeDTO mapEmployee(ResultSet rs) throws SQLException {
    EmployeeDTO employeeDTO = new EmployeeDTO();

    employeeDTO.setEmployeeId(rs.getLong("EMPLOYEE_ID"));
    employeeDTO.setRoleId(rs.getInt("ROLE_ID"));

    employeeDTO.setStoreId(getNullableLong(rs, "STORE_ID"));
    employeeDTO.setBranchId(getNullableLong(rs, "BRANCH_ID"));
    if (hasColumn(rs, "BRANCH_NAME")) {
      employeeDTO.setBranchName(rs.getString("BRANCH_NAME"));
    }

    employeeDTO.setLoginId(rs.getString("LOGIN_ID"));
    employeeDTO.setEmployeeName(rs.getString("EMPLOYEE_NAME"));
    employeeDTO.setPhoneNumber(rs.getString("PHONE_NUMBER"));
    employeeDTO.setIsActive(rs.getString("IS_ACTIVE"));

    employeeDTO.setCreatedAt(getNullableLocalDateTime(rs, "CREATED_AT"));
    employeeDTO.setUpdatedAt(getNullableLocalDateTime(rs, "UPDATED_AT"));

    return employeeDTO;
  }
}
