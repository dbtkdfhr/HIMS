package employee;

import auth.LoginEmployeeDTO;
import common.DBConnection;
import common.type.DBType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
            "ORDER BY employee_id ";

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

          long storeId = rs.getLong("STORE_ID");
          if (rs.wasNull()) {
            employeeDTO.setStoreId(null);
          } else {
            employeeDTO.setStoreId(storeId);
          }

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

      if (employee.getStoreId() == null) {
        pstmt.setNull(2, java.sql.Types.NUMERIC);
      } else {
        pstmt.setLong(2, employee.getStoreId());
      }

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

      if (employee.getStoreId() == null) {
        pstmt.setNull(4, java.sql.Types.NUMERIC);
      } else {
        pstmt.setLong(4, employee.getStoreId());
      }

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
            "store_id = ?, " +
            "updated_at = SYSDATE " +
            "WHERE employee_id = ?";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      pstmt.setInt(1, employee.getRoleId());

      if (employee.getStoreId() == null) {
        pstmt.setNull(2, java.sql.Types.NUMERIC);
      } else {
        pstmt.setLong(2, employee.getStoreId());
      }

      pstmt.setLong(3, employee.getEmployeeId());

      return pstmt.executeUpdate();

    }
  }

  // 로그인 ID 수정
  public int updateLoginId(EmployeeDTO employee) throws SQLException {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "login_id = ?, " +
            "updated_at = SYSDATE " +
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
            "password = ?, " +
            "updated_at = SYSDATE " +
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
            "is_active = 'N', " +
            "updated_at = SYSDATE " +
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
            "is_active = 'Y', " +
            "updated_at = SYSDATE " +
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

    long storeId = rs.getLong("STORE_ID");
    if (rs.wasNull()) {
      employeeDTO.setStoreId(null);
    } else {
      employeeDTO.setStoreId(storeId);
    }

    long branchId = rs.getLong("BRANCH_ID");
    if (rs.wasNull()) {
      employeeDTO.setBranchId(null);
    } else {
      employeeDTO.setBranchId(branchId);
    }

    employeeDTO.setLoginId(rs.getString("LOGIN_ID"));
    employeeDTO.setEmployeeName(rs.getString("EMPLOYEE_NAME"));
    employeeDTO.setPhoneNumber(rs.getString("PHONE_NUMBER"));
    employeeDTO.setIsActive(rs.getString("IS_ACTIVE"));

    Timestamp createdAt = rs.getTimestamp("CREATED_AT");
    Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");

    employeeDTO.setCreatedAt(
        createdAt != null ? createdAt.toLocalDateTime() : null
    );

    employeeDTO.setUpdatedAt(
        updatedAt != null ? updatedAt.toLocalDateTime() : null
    );

    return employeeDTO;
  }

  private void setNullableLong(PreparedStatement pstmt, int parameterIndex, Long value)
      throws SQLException {
    if (value == null) {
      pstmt.setNull(parameterIndex, java.sql.Types.NUMERIC);
    } else {
      pstmt.setLong(parameterIndex, value);
    }
  }
}
