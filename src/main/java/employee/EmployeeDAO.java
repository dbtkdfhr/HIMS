package employee;

import auth.LoginEmployeeDTO;
import common.DBConnection;
import common.DBType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

  // 전체 조회
  public List<EmployeeDTO> getEmployees() {
    List<EmployeeDTO> employees = new ArrayList<>();

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = DBConnection.getConnection(DBType.ORACLE);

      String sql =
          "SELECT " +
              "EMPLOYEE_ID, " +
              "ROLE_ID, " +
              "STORE_ID, " +
              "LOGIN_ID, " +
              "EMPLOYEE_NAME, " +
              "PHONE_NUMBER, " +
              "IS_ACTIVE, " +
              "CREATED_AT, " +
              "UPDATED_AT " +
              "FROM EMPLOYEE";

      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setEmployeeId(rs.getLong("EMPLOYEE_ID"));
        employeeDTO.setRoleId(rs.getInt("ROLE_ID"));
        employeeDTO.setStoreId(rs.getLong("STORE_ID"));
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

        employees.add(employeeDTO);
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      DBConnection.close(rs);
      DBConnection.close(pstmt);
      DBConnection.close(conn);
    }

    return employees;
  }

  // 로그인 ID 기준 직원 조회
  public LoginEmployeeDTO getEmployeeByLoginId(String loginId) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = DBConnection.getConnection(DBType.ORACLE);

      String sql =
          "SELECT " +
              "e.EMPLOYEE_ID, " +
              "e.ROLE_ID, " +
              "e.STORE_ID, " +
              "e.LOGIN_ID, " +
              "e.PASSWORD, " +
              "e.EMPLOYEE_NAME, " +
              "e.PHONE_NUMBER, " +
              "e.IS_ACTIVE, " +
              "e.CREATED_AT, " +
              "e.UPDATED_AT, " +
              "r.ROLE_NAME " +
              "FROM EMPLOYEE e " +
              "JOIN ROLE r " +
              "ON e.ROLE_ID = r.ROLE_ID " +
              "WHERE e.LOGIN_ID = ?";

      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, loginId);

      rs = pstmt.executeQuery();

      if (rs.next()) {
        LoginEmployeeDTO employeeDTO = new LoginEmployeeDTO();

        employeeDTO.setEmployeeId(rs.getLong("EMPLOYEE_ID"));
        employeeDTO.setRoleId(rs.getInt("ROLE_ID"));
        employeeDTO.setRoleName(rs.getString("ROLE_NAME"));
        employeeDTO.setStoreId(rs.getLong("STORE_ID"));
        employeeDTO.setLoginId(rs.getString("LOGIN_ID"));
        employeeDTO.setPassword(rs.getString("PASSWORD"));
        employeeDTO.setEmployeeName(rs.getString("EMPLOYEE_NAME"));
        employeeDTO.setPhoneNumber(rs.getString("PHONE_NUMBER"));
        employeeDTO.setIsActive(rs.getString("IS_ACTIVE"));

        return employeeDTO;
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      DBConnection.close(rs);
      DBConnection.close(pstmt);
      DBConnection.close(conn);
    }

    return null;
  }

  // EMPLOYEE_ID로 권한 조회


  /* INSERT */
  // 직원 추가
  public int insertEmployee(EmployeeDTO employee) {
    String sql =
        "INSERT INTO EMPLOYEE (" +
            "role_id, " +
            "store_id, " +
            "login_id, " +
            "password, " +
            "employee_name, " +
            "phone_number, " +
            "is_active" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";

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

      pstmt.setString(3, employee.getLoginId());
      pstmt.setString(4, employee.getPassword());
      pstmt.setString(5, employee.getEmployeeName());
      pstmt.setString(6, employee.getPhoneNumber());

      if (employee.getIsActive() == null) {
        pstmt.setString(7, "Y");
      } else {
        pstmt.setString(7, employee.getIsActive());
      }

      return pstmt.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("직원 등록 중 오류가 발생했습니다.", e);
    }
  }

  /* UPDATE */
  // 직원 전체 정보 수정
  public int updateEmployee(EmployeeDTO employee) {
    String sql =
        "UPDATE EMPLOYEE SET " +
            "employee_name = ?, " +
            "phone_number = ?, " +
            "role_id = ?, " +
            "store_id = ?, " +
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

      pstmt.setString(5, employee.getIsActive());
      pstmt.setLong(6, employee.getEmployeeId());

      return pstmt.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("직원 정보 수정 중 오류가 발생했습니다.", e);
    }
  }

  // 직원명 수정
  public int updateEmployeeName(EmployeeDTO employee) {
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

    } catch (SQLException e) {
      throw new RuntimeException("직원명 수정 중 오류가 발생했습니다.", e);
    }
  }

  // 연락처 수정
  public int updatePhoneNumber(EmployeeDTO employee) {
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

    } catch (SQLException e) {
      throw new RuntimeException("직원 연락처 수정 중 오류가 발생했습니다.", e);
    }
  }

  // 권한 수정 및 소속 매장
  public int updateRoleAndStore(EmployeeDTO employee) {
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

    } catch (SQLException e) {
      throw new RuntimeException("직원 권한 및 소속 매장 수정 중 오류가 발생했습니다.", e);
    }
  }

  // 로그인 ID 수정
  public int updateLoginId(EmployeeDTO employee) {
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

    } catch (SQLException e) {
      throw new RuntimeException("직원 로그인 ID 수정 중 오류가 발생했습니다.", e);
    }
  }

  // 비밀번호 수정
  public int updatePassword(EmployeeDTO employee) {
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

    } catch (SQLException e) {
      throw new RuntimeException("직원 비밀번호 수정 중 오류가 발생했습니다.", e);
    }
  }

  // 직원 사용 중지 처리
  public int disableEmployee(EmployeeDTO employee) {
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

    } catch (SQLException e) {
      throw new RuntimeException("직원 사용 중지 처리 중 오류가 발생했습니다.", e);
    }
  }

  // 직원 사용 재개 처리
  public int activateEmployee(EmployeeDTO employee) {
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

    } catch (SQLException e) {
      throw new RuntimeException("직원 사용 재개 처리 중 오류가 발생했습니다.", e);
    }
  }
}
