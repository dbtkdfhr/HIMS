package order.external;

import common.type.External_OrderStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExternalOrderReceiptDAO {
  public void insertExternalReceipt(Connection mariaConn, ExternalOrderReceiptDTO dto) throws SQLException {
    String sql = "INSERT INTO external_order_receipt "
        + "(supplier_id, supplier_product_id, internal_order_request_id, "
        + "request_store_name, request_quantity, approved_quantity, receipt_status) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement pstmt = mariaConn.prepareStatement(sql)) {
      pstmt.setLong(1, dto.getSupplierId());
      pstmt.setLong(2, dto.getSupplierProductId());
      pstmt.setLong(3, dto.getInternalOrderRequestId());
      pstmt.setString(4, dto.getRequestStoreName());
      pstmt.setLong(5, dto.getRequestQuantity());
      pstmt.setLong(6, dto.getApprovedQuantity());
      pstmt.setString(7, dto.getReceiptStatus());

      pstmt.executeUpdate();
    }
  }

  public List<ExternalOrderReceiptDTO> findAll(Connection mariaConn) throws SQLException {
    String sql = "SELECT * FROM external_order_receipt ORDER BY created_at DESC";
    List<ExternalOrderReceiptDTO> list = new ArrayList<>();

    try (PreparedStatement pstmt = mariaConn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        list.add(mapToExternalOrderReceiptDTO(rs));
      }
    }

    return list;
  }

  public ExternalOrderReceiptDTO findByInternalOrderRequestId(Connection mariaConn,
      long internalOrderRequestId) throws SQLException {
    String sql = "SELECT * FROM external_order_receipt WHERE internal_order_request_id = ?";

    try (PreparedStatement pstmt = mariaConn.prepareStatement(sql)) {
      pstmt.setLong(1, internalOrderRequestId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapToExternalOrderReceiptDTO(rs);
        }
      }
    }

    return null;
  }

  // 공급처에 들어온 모든 발주 접수 조회
  public List<ExternalOrderReceiptDTO> findAllByStatus(
      Connection mariaConn,
      External_OrderStatus status
  ) throws SQLException {
    String sql = "SELECT * FROM external_order_receipt "
        + "WHERE receipt_status = ? "
        + "ORDER BY created_at DESC";

    List<ExternalOrderReceiptDTO> list = new ArrayList<>();

    try (PreparedStatement pstmt = mariaConn.prepareStatement(sql)) {
      pstmt.setString(1, status.name());

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          list.add(mapToExternalOrderReceiptDTO(rs));
        }
      }
    }

    return list;
  }

  // 접수증 상태 업데이트
  public int updateStatus(Connection mariaConn, long internalRequestId, External_OrderStatus status) throws SQLException {
    String sql = "UPDATE external_order_receipt SET receipt_status = ? WHERE internal_order_request_id  = ? AND receipt_status = ?";
    try (PreparedStatement pstmt = mariaConn.prepareStatement(sql)) {
      pstmt.setString(1, status.name());
      pstmt.setLong(2, internalRequestId);
      pstmt.setString(3, External_OrderStatus.RECEIVED.name());
      return pstmt.executeUpdate();
    }
  }

  private ExternalOrderReceiptDTO mapToExternalOrderReceiptDTO(ResultSet rs) throws SQLException {
    ExternalOrderReceiptDTO dto = new ExternalOrderReceiptDTO();

    dto.setExternalOrderReceiptId(rs.getLong("external_order_receipt_id"));
    dto.setSupplierId(rs.getLong("supplier_id"));
    dto.setSupplierProductId(rs.getLong("supplier_product_id"));
    dto.setInternalOrderRequestId(rs.getLong("internal_order_request_id"));
    dto.setRequestStoreName(rs.getString("request_store_name"));
    dto.setRequestQuantity(rs.getInt("request_quantity"));
    dto.setApprovedQuantity(rs.getInt("approved_quantity"));
    dto.setReceiptStatus(rs.getString("receipt_status"));

    return dto;
  }
}
