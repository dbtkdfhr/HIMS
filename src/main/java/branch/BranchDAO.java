package branch;

import static common.GetNullableVariable.getNullableLocalDateTime;

import common.DBConnection;
import common.type.DBType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

  /*SELECT*/
  // 전 지점 조회 -- 시스템 관리자가 함.
  public List<BranchDTO> getBranches() throws SQLException {

    String sql = "SELECT " +
        "branch_id, " +
        "branch_name, " +
        "address, " +
        "phone_number, " +
        "operation_status, " +
        "created_at, " +
        "updated_at " +
        "FROM BRANCH";

    try (
        Connection conn = DBConnection.getConnection(DBType.ORACLE);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()
    ) {
      List<BranchDTO> branches = new ArrayList<>();

      while (rs.next()) {
        BranchDTO branchDTO = new BranchDTO();

        branchDTO.setBranchId(rs.getLong("branch_id"));
        branchDTO.setBranchName(rs.getString("branch_name"));
        branchDTO.setAddress(rs.getString("address"));
        branchDTO.setPhoneNumber(rs.getString("phone_number"));
        branchDTO.setOperationStatus(rs.getString("operation_status"));
        branchDTO.setCreatedAt(getNullableLocalDateTime(rs, "created_at"));
        branchDTO.setUpdatedAt(getNullableLocalDateTime(rs, "updated_at"));

        branches.add(branchDTO);
      }

      return branches;
    }
  }
}
