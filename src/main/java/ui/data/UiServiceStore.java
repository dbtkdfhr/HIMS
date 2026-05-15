package ui.data;

import auth.AuthService;
import auth.LoginRequestDTO;
import auth.LoginResponseDTO;
import branch.BranchDAO;
import branch.BranchDTO;
import brand.BrandDTO;
import brand.BrandService;
import category.CategoryDTO;
import category.CategoryService;
import common.type.OrderStatus;
import common.type.RoleType;
import employee.EmployeeDTO;
import employee.EmployeeService;
import exception.InputException;
import inventory.InventoryDTO;
import inventory.InventoryService;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import order.OrderManagementService;
import order.external.ExternalOrderReceiptDTO;
import order.external.ExternalOrderService;
import order.request.OrderRequestDTO;
import order.request.OrderRequestService;
import product.ProductDTO;
import product.ProductService;
import store.StoreDTO;
import store.StoreService;
import store.receipt.StoreReceiptDTO;
import store.receipt.StoreReceiptService;

public class UiServiceStore {

  private final AuthService authService = new AuthService();
  private final InventoryService inventoryService = new InventoryService();
  private final OrderRequestService orderRequestService = new OrderRequestService();
  private final OrderManagementService orderManagementService = new OrderManagementService();
  private final ExternalOrderService externalOrderService = new ExternalOrderService();
  private final StoreReceiptService storeReceiptService = new StoreReceiptService();
  private final StoreService storeService = new StoreService();
  private final ProductService productService = new ProductService();
  private final EmployeeService employeeService = new EmployeeService();
  private final BrandService brandService = new BrandService();
  private final CategoryService categoryService = new CategoryService();
  private final BranchDAO branchDAO = new BranchDAO();

  public EmployeeDTO authenticate(String loginId, String password) throws SQLException {
    LoginRequestDTO requestDTO = new LoginRequestDTO();
    requestDTO.setLoginId(loginId);
    requestDTO.setPassword(password);

    LoginResponseDTO responseDTO = authService.login(requestDTO);

    EmployeeDTO employeeDTO = new EmployeeDTO();
    employeeDTO.setEmployeeId(responseDTO.getEmployeeId());
    employeeDTO.setLoginId(responseDTO.getLoginId());
    employeeDTO.setEmployeeName(responseDTO.getEmployeeName());
    employeeDTO.setPhoneNumber(responseDTO.getPhoneNumber());
    employeeDTO.setRoleId(responseDTO.getRoleId());
    employeeDTO.setStoreId(responseDTO.getStoreId());
    employeeDTO.setBranchId(responseDTO.getBranchId());
    employeeDTO.setIsActive(responseDTO.getIsActive());
    return employeeDTO;
  }

  public String findRoleName(int roleId) {
    return RoleType.fromRoleId(roleId).getRoleName();
  }

  public String findStoreName(long storeId) throws SQLException {
    return storeService.findStoreNameById(storeId);
  }

  public String findProductName(long productId) throws SQLException {
    return productService.findProductNameById(productId);
  }

  public List<EmployeeDTO> employees() throws SQLException {
    return employeeService.findAllEmployees();
  }

  public List<StoreDTO> stores() throws SQLException {
    return storeService.findAllStores();
  }

  public List<ProductDTO> products() throws SQLException {
    return productService.findAllProducts();
  }

  public List<BranchDTO> branches() throws SQLException {
    return branchDAO.getBranches();
  }

  public List<InventoryDTO> inventories() throws SQLException {
    return inventoryService.getInventoryList(systemUser());
  }

  public List<InventoryDTO> findInventoriesByBranch(long branchId) throws SQLException {
    return inventoryService.getInventoryList(branchUser(branchId));
  }

  public List<InventoryDTO> findInventoriesByStore(long storeId) throws SQLException {
    return inventoryService.getInventoryList(storeUser(storeId));
  }

  public List<InventoryDTO> findLowStockByStore(long storeId) throws SQLException {
    return inventoryService.getLowStockList(storeUser(storeId));
  }

  public List<OrderRequestDTO> orders() throws SQLException {
    return orderRequestService.getAllOrderRequests();
  }

  public List<OrderRequestDTO> findOrdersByStatus(String status) throws SQLException {
    if (status == null) {
      return orderRequestService.getAllOrderRequests();
    }

    return orderRequestService.getOrderRequestsByStatus(OrderStatus.valueOf(status));
  }

  public List<OrderRequestDTO> findOrdersByStore(long storeId) throws SQLException {
    return orderRequestService.getMyOrderRequests(storeId);
  }

  public List<StoreReceiptDTO> findReceiptsByStore(long storeId) throws SQLException {
    return storeReceiptService.findReceiptHistoryByStoreId(storeId);
  }

  public OrderRequestDTO findOrder(long orderRequestId) throws SQLException {
    return orderRequestService.getOrderRequest(orderRequestId);
  }

  public OrderRequestDTO createOrderRequest(long storeId, long productId, long employeeId,
      int quantity, String reason) throws SQLException {
    OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
    orderRequestDTO.setStoreId(storeId);
    orderRequestDTO.setProductId(productId);
    orderRequestDTO.setRequestEmployeeId(employeeId);
    orderRequestDTO.setOrderQuantity(quantity);
    orderRequestDTO.setRequestReason(reason);

    orderRequestService.createOrderRequest(orderRequestDTO);
    List<OrderRequestDTO> orders = orderRequestService.getMyOrderRequests(storeId);
    if (orders.isEmpty()) {
      return orderRequestDTO;
    }

    return orders.get(0);
  }

  public void approveOrder(long orderId, long employeeId, int approvedQuantity) throws SQLException {
    orderManagementService.approveOrder(orderId, approvedQuantity, employeeId);
  }

  public void rejectOrder(long orderId, long employeeId, String reason) throws SQLException {
    orderManagementService.rejectOrder(orderId, reason, employeeId);
  }

  public void sendOrderToVendor(long orderId) throws SQLException {
    if ("-".equals(findExternalOrderStatus(orderId))) {
      throw new InputException("외부 발주처 접수 내역이 없습니다. 발주 승인 상태를 확인해 주세요.");
    }
  }

  public void approveExternalOrder(long orderId) throws SQLException {
    ExternalOrderReceiptDTO receiptDTO = requireExternalReceipt(orderId);
    externalOrderService.processShipping(
        orderId,
        receiptDTO.getSupplierProductId(),
        receiptDTO.getApprovedQuantity()
    );
  }

  public void rejectExternalOrder(long orderId, String reason) throws SQLException {
    externalOrderService.rejectShipping(orderId, reason);
  }

  public String findExternalOrderStatus(long orderId) throws SQLException {
    return externalOrderService.findExternalOrderStatus(orderId);
  }

  public String findExternalRejectReason(long orderId) throws SQLException {
    OrderRequestDTO order = orderRequestService.getOrderRequest(orderId);
    return order.getRejectReason() == null ? "" : order.getRejectReason();
  }

  public StoreReceiptDTO confirmReceipt(long orderId, long employeeId) throws SQLException {
    storeReceiptService.receiveReceipt(orderId, employeeId, "");
    return receiptByOrderId(orderId);
  }

  public StoreReceiptDTO markReceiptDifference(long orderId, long employeeId, int receivedQuantity,
      String reason) throws SQLException {
    storeReceiptService.partialReceiveReceipt(orderId, employeeId, receivedQuantity, reason);
    return receiptByOrderId(orderId);
  }

  public StoreReceiptDTO rejectReceipt(long orderId, long employeeId, String reason)
      throws SQLException {
    storeReceiptService.cancelReceipt(orderId, employeeId, reason);
    return receiptByOrderId(orderId);
  }

  public void processSale(long storeId, long productId, int quantity) throws SQLException {
    storeReceiptService.processSale(storeId, productId, quantity);
  }

  public EmployeeDTO createEmployee(String loginId, String name, RoleType roleType, Long branchId,
      Long storeId) throws SQLException {
    EmployeeDTO employeeDTO = new EmployeeDTO();
    employeeDTO.setLoginId(loginId);
    employeeDTO.setPassword("pass1234");
    employeeDTO.setEmployeeName(name);
    employeeDTO.setPhoneNumber("010-0000-0000");
    employeeDTO.setRoleId(roleType.getRoleId());
    employeeDTO.setStoreId(storeId);
    employeeDTO.setBranchId(branchId);
    employeeDTO.setIsActive("Y");
    employeeDTO.setCreatedAt(LocalDateTime.now());
    employeeDTO.setUpdatedAt(LocalDateTime.now());

    employeeService.registerEmployee(employeeDTO);
    return employeeDTO;
  }

  public void changeEmployeeRole(long employeeId, RoleType roleType) throws SQLException {
    EmployeeDTO employeeDTO = new EmployeeDTO();
    employeeDTO.setEmployeeId(employeeId);
    employeeDTO.setRoleId(roleType.getRoleId());
    employeeService.updateRoleAndStore(employeeDTO);
  }

  public void changeStoreManagerStore(long employeeId, long storeId) throws SQLException {
    EmployeeDTO employeeDTO = new EmployeeDTO();
    employeeDTO.setEmployeeId(employeeId);
    employeeDTO.setRoleId(RoleType.STORE_MANAGER.getRoleId());
    employeeDTO.setStoreId(storeId);
    employeeService.updateRoleAndStore(employeeDTO);
  }

  public void deactivateEmployee(long employeeId) throws SQLException {
    employeeService.disableEmployee(employeeId);
  }

  public void resetEmployeePassword(long employeeId, String newPassword) throws SQLException {
    employeeService.resetPassword(employeeId, newPassword);
  }

  public void createStore(StoreDTO storeDTO) throws SQLException {
    storeService.registerStore(storeDTO);
  }

  public void createProduct(ProductDTO productDTO) throws SQLException {
    productService.registerProduct(productDTO);
  }

  public List<BrandDTO> brands() throws SQLException {
    return brandService.findAllBrands();
  }

  public List<CategoryDTO> categories() throws SQLException {
    return categoryService.getAllCategories();
  }

  public Map<String, List<String[]>> masterRecords() throws SQLException {
    Map<String, List<String[]>> records = new LinkedHashMap<>();
    List<String[]> storeRows = new ArrayList<>();
    for (StoreDTO storeDTO : stores()) {
      storeRows.add(new String[]{
          String.valueOf(storeDTO.getStoreId()),
          storeDTO.getStoreName(),
          storeDTO.getStoreLocation(),
          storeDTO.getOperationStatus()
      });
    }
    records.put("입점매장", storeRows);

    List<String[]> productRows = new ArrayList<>();
    for (ProductDTO productDTO : products()) {
      productRows.add(new String[]{
          String.valueOf(productDTO.getProductId()),
          productDTO.getProductName(),
          String.valueOf(productDTO.getPrice()),
          productDTO.getProductStatus()
      });
    }
    records.put("상품", productRows);
    return records;
  }

  private StoreReceiptDTO receiptByOrderId(long orderId) throws SQLException {
    OrderRequestDTO order = orderRequestService.getOrderRequest(orderId);
    for (StoreReceiptDTO receiptDTO : storeReceiptService.findReceiptHistoryByStoreId(order.getStoreId())) {
      if (receiptDTO.getOrderRequestId() == orderId) {
        return receiptDTO;
      }
    }
    return new StoreReceiptDTO();
  }

  private ExternalOrderReceiptDTO requireExternalReceipt(long orderId) throws SQLException {
    ExternalOrderReceiptDTO receiptDTO = externalOrderService.getExternalOrderReceipt(orderId);
    if (receiptDTO == null) {
      throw new InputException("외부 발주처 접수 내역이 없습니다.");
    }

    return receiptDTO;
  }

  private Long findBranchIdByStoreId(Long storeId) throws SQLException {
    if (storeId == null) {
      return null;
    }

    for (StoreDTO storeDTO : stores()) {
      if (storeDTO.getStoreId() == storeId) {
        return storeDTO.getBranchId();
      }
    }

    return null;
  }

  private EmployeeDTO storeUser(long storeId) {
    EmployeeDTO user = new EmployeeDTO();
    user.setEmployeeId(0);
    user.setRoleId(RoleType.STORE_MANAGER.getRoleId());
    user.setStoreId(storeId);
    return user;
  }

  private EmployeeDTO branchUser(long branchId) {
    EmployeeDTO user = new EmployeeDTO();
    user.setEmployeeId(0);
    user.setRoleId(RoleType.BRANCH_MANAGER.getRoleId());
    user.setBranchId(branchId);
    return user;
  }

  private EmployeeDTO systemUser() {
    EmployeeDTO user = new EmployeeDTO();
    user.setEmployeeId(0);
    user.setRoleId(RoleType.SYSTEM_MANAGER.getRoleId());
    return user;
  }
}
