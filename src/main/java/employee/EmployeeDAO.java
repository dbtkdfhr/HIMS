package employee;

import common.DBConnection;
import common.DBType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
  public EmployeeDTO getEmployeeByLoginId(String loginId) {
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
        EmployeeDTO employeeDTO = new EmployeeDTO();

        employeeDTO.setEmployeeId(rs.getLong("EMPLOYEE_ID"));
        employeeDTO.setRoleId(rs.getInt("ROLE_ID"));
        employeeDTO.setRoleName(rs.getString("ROLE_NAME"));
        employeeDTO.setStoreId(rs.getLong("STORE_ID"));
        employeeDTO.setLoginId(rs.getString("LOGIN_ID"));
        employeeDTO.setPassword(rs.getString("PASSWORD"));
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

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      DBConnection.close(rs);
      DBConnection.close(pstmt);
      DBConnection.close(conn);
    }

    return null;
  }
}