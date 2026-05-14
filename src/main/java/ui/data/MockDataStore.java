package ui.data;

import common.type.ProductStatus;
import common.type.RoleType;
import employee.EmployeeDTO;
import exception.DisableUserException;
import exception.InputException;
import exception.MismatchQuantityException;
import exception.NotFoundException;
import exception.NotReceptableException;
import inventory.InventoryDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import order.request.OrderRequestDTO;
import product.ProductDTO;
import store.StoreDTO;
import store.receipt.StoreReceiptDTO;

public class MockDataStore {

  private final List<EmployeeDTO> employees = new ArrayList<>();
  private final List<StoreDTO> stores = new ArrayList<>();
  private final List<ProductDTO> products = new ArrayList<>();
  private final List<InventoryDTO> inventories = new ArrayList<>();
  private final List<OrderRequestDTO> orders = new ArrayList<>();
  private final List<StoreReceiptDTO> receipts = new ArrayList<>();
  private final Map<Long, String> externalOrderStatuses = new LinkedHashMap<>();
  private final Map<Long, String> externalRejectReasons = new LinkedHashMap<>();
  private final Map<String, List<String[]>> masterRecords = new LinkedHashMap<>();
  private long nextOrderId = 1004;
  private long nextReceiptId = 5002;
  private long nextEmployeeId = 9006;

  public MockDataStore() {
    seed();
  }

  public List<EmployeeDTO> employees() {
    return employees;
  }

  public List<StoreDTO> stores() {
    return stores;
  }

  public List<ProductDTO> products() {
    return products;
  }

  public List<InventoryDTO> inventories() {
    return inventories;
  }

  public List<OrderRequestDTO> orders() {
    return orders;
  }

  public List<StoreReceiptDTO> receipts() {
    return receipts;
  }

  public Map<String, List<String[]>> masterRecords() {
    return masterRecords;
  }

  public EmployeeDTO findUserByRole(RoleType roleType) {
    for (EmployeeDTO employee : employees) {
      if (employee.getRoleId() == roleType.getRoleId() && "Y".equals(employee.getIsActive())) {
        return employee;
      }
    }
    return employees.get(0);
  }

  public EmployeeDTO authenticate(String loginId, String password, RoleType roleType) {
    for (EmployeeDTO employee : employees) {
      if (loginId.equals(employee.getLoginId()) && password.equals(employee.getPassword())
          && employee.getRoleId() == roleType.getRoleId()) {
        if (employee.getIsActive().equals("N")) {
          throw new DisableUserException("사용 중지된 계정입니다.");
        }
        return employee;
      }
    }
    throw new InputException("로그인 정보 또는 선택한 역할이 일치하지 않습니다.");
  }

  public String findRoleName(int roleId) {
    return RoleType.fromRoleId(roleId).getRoleName();
  }

  public String findStoreName(long storeId) {
    for (StoreDTO store : stores) {
      if (store.getStoreId() == storeId) {
        return store.getStoreName();
      }
    }
    return "-";
  }

  public String findProductName(long productId) {
    for (ProductDTO product : products) {
      if (product.getProductId() == productId) {
        return product.getProductName();
      }
    }
    return "-";
  }

  public InventoryDTO findInventory(long storeId, long productId) {
    for (InventoryDTO inventory : inventories) {
      if (inventory.getStoreId() == storeId && inventory.getProductId() == productId) {
        return inventory;
      }
    }
    return null;
  }

  public List<InventoryDTO> findInventoriesByStore(long storeId) {
    List<InventoryDTO> result = new ArrayList<>();
    for (InventoryDTO inventory : inventories) {
      if (inventory.getStoreId() == storeId) {
        result.add(inventory);
      }
    }
    return result;
  }

  public List<InventoryDTO> findLowStockByStore(long storeId) {
    List<InventoryDTO> result = new ArrayList<>();
    for (InventoryDTO inventory : findInventoriesByStore(storeId)) {
      if (inventory.getCurrentQuantity() <= inventory.getSafetyQuantity()) {
        result.add(inventory);
      }
    }
    return result;
  }

  public List<OrderRequestDTO> findOrdersByStatus(String status) {
    List<OrderRequestDTO> result = new ArrayList<>();
    for (OrderRequestDTO order : orders) {
      if (status == null || status.equals(order.getOrderStatus())) {
        result.add(order);
      }
    }
    return result;
  }

  public List<OrderRequestDTO> findOrdersByStore(long storeId) {
    List<OrderRequestDTO> result = new ArrayList<>();
    for (OrderRequestDTO order : orders) {
      if (order.getStoreId() == storeId) {
        result.add(order);
      }
    }
    return result;
  }

  public List<StoreReceiptDTO> findReceiptsByStore(long storeId) {
    List<StoreReceiptDTO> result = new ArrayList<>();
    for (StoreReceiptDTO receipt : receipts) {
      OrderRequestDTO order = findOrder(receipt.getOrderRequestId());
      if (order != null && order.getStoreId() == storeId) {
        result.add(receipt);
      }
    }
    return result;
  }

  public OrderRequestDTO findOrder(long orderRequestId) {
    for (OrderRequestDTO order : orders) {
      if (order.getOrderRequestId() == orderRequestId) {
        return order;
      }
    }
    return null;
  }

  public OrderRequestDTO createOrderRequest(long storeId, long productId, long employeeId,
      int quantity, String reason) {
    OrderRequestDTO order = new OrderRequestDTO();
    order.setOrderRequestId(nextOrderId++);
    order.setStoreId(storeId);
    order.setSupplierIntegrationId(301);
    order.setProductId(productId);
    order.setRequestEmployeeId(employeeId);
    order.setApprovalRoleId((long) RoleType.SUPPLIER_MANAGER.getRoleId());
    order.setOrderQuantity(quantity);
    order.setRequestReason(reason);
    order.setOrderStatus("REQUESTED");
    order.setRequestedAt(LocalDateTime.now());
    orders.add(order);
    return order;
  }

  public void approveOrder(long orderId, long employeeId, int approvedQuantity) {
    OrderRequestDTO order = requireOrder(orderId);
    requireStatus(order, "REQUESTED");
    if (approvedQuantity > order.getOrderQuantity()) {
      throw new MismatchQuantityException("승인수량은 요청수량을 초과할 수 없습니다.");
    }
    order.setApprovalEmployeeId(employeeId);
    order.setApprovedQuantity(approvedQuantity);
    order.setOrderStatus("APPROVED");
    order.setApprovedAt(LocalDateTime.now());
  }

  public void rejectOrder(long orderId, long employeeId, String reason) {
    OrderRequestDTO order = requireOrder(orderId);
    requireStatus(order, "REQUESTED");
    order.setApprovalEmployeeId(employeeId);
    order.setRejectReason(reason);
    order.setOrderStatus("REJECTED_BY_VENDOR");
    order.setRejectedAt(LocalDateTime.now());
  }

  public void sendOrderToVendor(long orderId) {
    OrderRequestDTO order = requireOrder(orderId);
    requireStatus(order, "APPROVED");
    order.setOrderStatus("SENT_TO_VENDOR");
    order.setExternalOrderId("EXT-" + orderId);
    order.setSentToSupplierAt(LocalDateTime.now());
    externalOrderStatuses.put(orderId, "RECEIVED");
  }

  public void shipOrder(long orderId) {
    OrderRequestDTO order = requireOrder(orderId);
    requireStatus(order, "SENT_TO_VENDOR");
    requireExternalStatus(orderId, "APPROVED");
    order.setOrderStatus("SHIPPED");
  }

  public void approveExternalOrder(long orderId) {
    requireOrder(orderId);
    requireExternalStatus(orderId, "RECEIVED");
    externalOrderStatuses.put(orderId, "APPROVED");
    externalRejectReasons.remove(orderId);
  }

  public void rejectExternalOrder(long orderId, String reason) {
    requireOrder(orderId);
    requireExternalStatus(orderId, "RECEIVED");
    externalOrderStatuses.put(orderId, "REJECTED");
    externalRejectReasons.put(orderId, reason);
  }

  public String findExternalOrderStatus(long orderId) {
    return externalOrderStatuses.getOrDefault(orderId, "-");
  }

  public String findExternalRejectReason(long orderId) {
    return externalRejectReasons.getOrDefault(orderId, "");
  }

  public StoreReceiptDTO confirmReceipt(long orderId, long employeeId) {
    OrderRequestDTO order = requireOrder(orderId);
    requireStatus(order, "SHIPPED");
    int quantity = approvedQuantity(order);
    StoreReceiptDTO receipt = createReceipt(orderId, employeeId, quantity, 0, "", "CONFIRMED");
    InventoryDTO inventory = findInventory(order.getStoreId(), order.getProductId());
    if (inventory != null) {
      inventory.setCurrentQuantity(inventory.getCurrentQuantity() + quantity);
      inventory.setUpdatedAt(LocalDateTime.now());
      inventory.setLowStock(inventory.getCurrentQuantity() <= inventory.getSafetyQuantity());
    }
    order.setOrderStatus("COMPLETED");
    return receipt;
  }

  public StoreReceiptDTO markReceiptDifference(long orderId, long employeeId, int receivedQuantity,
      String reason) {
    OrderRequestDTO order = requireOrder(orderId);
    requireStatus(order, "SHIPPED");
    int approvedQuantity = approvedQuantity(order);
    if (receivedQuantity >= approvedQuantity) {
      throw new MismatchQuantityException("수량 차이 입고는 승인수량보다 적은 수량만 입력할 수 있습니다.");
    }
    int difference = approvedQuantity - receivedQuantity;
    StoreReceiptDTO receipt = createReceipt(orderId, employeeId, receivedQuantity, difference,
        reason, "DIFFERENCE");
    order.setOrderStatus("RETURNED_BY_STORE");
    return receipt;
  }

  public StoreReceiptDTO rejectReceipt(long orderId, long employeeId, String reason) {
    OrderRequestDTO order = requireOrder(orderId);
    requireStatus(order, "SHIPPED");
    StoreReceiptDTO receipt = createReceipt(orderId, employeeId, 0, approvedQuantity(order), reason,
        "CANCELED");
    order.setOrderStatus("RETURNED_BY_STORE");
    return receipt;
  }

  public void processSale(long storeId, long productId, int quantity) {
    InventoryDTO inventory = findInventory(storeId, productId);
    if (inventory == null) {
      throw new NotFoundException("재고 정보를 찾을 수 없습니다.");
    }
    if (quantity <= 0) {
      throw new MismatchQuantityException("판매 수량은 1개 이상이어야 합니다.");
    }
    if (inventory.getCurrentQuantity() < quantity) {
      throw new MismatchQuantityException("판매 가능한 재고가 부족합니다.");
    }
    inventory.setCurrentQuantity(inventory.getCurrentQuantity() - quantity);
    inventory.setUpdatedAt(LocalDateTime.now());
    inventory.setLowStock(inventory.getCurrentQuantity() <= inventory.getSafetyQuantity());
  }

  public EmployeeDTO createEmployee(String loginId, String name, RoleType roleType, Long storeId) {
    Long branchId = findBranchIdByStoreId(storeId);
    EmployeeDTO employee = new EmployeeDTO(nextEmployeeId++, loginId, "1234", name, "010-0000-0000",
        roleType.getRoleId(), storeId, branchId, "Y", LocalDateTime.now(), LocalDateTime.now());
    employees.add(employee);
    return employee;
  }

  public void deactivateEmployee(long employeeId) {
    for (EmployeeDTO employee : employees) {
      if (employee.getEmployeeId() == employeeId) {
        employee.setIsActive("N");
        employee.setUpdatedAt(LocalDateTime.now());
        return;
      }
    }
    throw new NotFoundException("직원을 찾을 수 없습니다.");
  }

  public void addMasterRecord(String type, String... values) {
    masterRecords.computeIfAbsent(type, key -> new ArrayList<>()).add(values);
  }

  private StoreReceiptDTO createReceipt(long orderId, long employeeId, int receivedQuantity,
      int differenceQuantity, String reason, String status) {
    StoreReceiptDTO receipt = new StoreReceiptDTO();
    receipt.setStoreReceiptId(nextReceiptId++);
    receipt.setOrderRequestId(orderId);
    receipt.setConfirmEmployeeId(employeeId);
    receipt.setReceivedQuantity(receivedQuantity);
    receipt.setDifferenceQuantity(differenceQuantity);
    receipt.setDifferenceReason(reason);
    receipt.setReceiptStatus(status);
    receipt.setCreatedAt(LocalDateTime.now());
    receipt.setUpdatedAt(LocalDateTime.now());
    receipts.add(receipt);
    return receipt;
  }

  private OrderRequestDTO requireOrder(long orderId) {
    OrderRequestDTO order = findOrder(orderId);
    if (order == null) {
      throw new NotFoundException("발주 요청을 찾을 수 없습니다.");
    }
    return order;
  }

  private void requireStatus(OrderRequestDTO order, String status) {
    if (!status.equals(order.getOrderStatus())) {
      throw new NotReceptableException("현재 상태에서는 처리할 수 없습니다.");
    }
  }

  private void requireExternalStatus(long orderId, String status) {
    if (!status.equals(findExternalOrderStatus(orderId))) {
      throw new NotReceptableException("외부 발주처 상태가 처리 가능한 상태가 아닙니다.");
    }
  }

  private int approvedQuantity(OrderRequestDTO order) {
    if (order.getApprovedQuantity() == null) {
      return order.getOrderQuantity();
    }
    return order.getApprovedQuantity();
  }

  private void seed() {
    LocalDateTime now = LocalDateTime.now();
    employees.add(new EmployeeDTO(9001, "branch", "1234", "최우진", "010-1111-1111",
        RoleType.BRANCH_MANAGER.getRoleId(), null, 1L, "Y", now, now));
    employees.add(new EmployeeDTO(9002, "vendor", "1234", "Annie", "010-2222-2222",
        RoleType.SUPPLIER_MANAGER.getRoleId(), null, 1L, "Y", now, now));
    employees.add(new EmployeeDTO(9003, "store", "1234", "유상록", "010-3333-3333",
        RoleType.STORE_MANAGER.getRoleId(), 101L, 1L, "Y", now, now));
    employees.add(new EmployeeDTO(9004, "system", "1234", "시스템관리자", "010-4444-4444",
        RoleType.SYSTEM_MANAGER.getRoleId(), null, null, "Y", now, now));
    employees.add(new EmployeeDTO(9005, "staff", "1234", "일반직원", "010-5555-5555",
        RoleType.STAFF.getRoleId(), 101L, 1L, "Y", now, now));

    stores.add(store(101, 1, 10, "더한섬하우스 무역센터점", "3F", "서관 3층", "운영중"));
    stores.add(store(102, 1, 11, "타임 압구정본점", "2F", "본관 2층", "운영중"));

    products.add(product(1001, 10, 1, "울 블렌드 재킷", 329000, "가을/겨울", "ON_SALE"));
    products.add(product(1002, 10, 1, "코튼 셔츠", 159000, "상시", "ON_SALE"));
    products.add(product(1003, 11, 1, "시즌 원피스", 259000, "봄/여름", "ON_SALE"));

    inventories.add(inventory(101, 1001, 3, 5, "울 블렌드 재킷", 329000, "가을/겨울", "한섬", "의류"));
    inventories.add(inventory(101, 1002, 18, 8, "코튼 셔츠", 159000, "상시", "한섬", "의류"));
    inventories.add(inventory(102, 1003, 7, 10, "시즌 원피스", 259000, "봄/여름", "타임", "의류"));

    OrderRequestDTO requested = new OrderRequestDTO();
    requested.setOrderRequestId(1001);
    requested.setStoreId(101);
    requested.setSupplierIntegrationId(301);
    requested.setProductId(1001);
    requested.setRequestEmployeeId(9003);
    requested.setOrderQuantity(12);
    requested.setRequestReason("안전재고 이하");
    requested.setOrderStatus("REQUESTED");
    requested.setRequestedAt(now);
    orders.add(requested);

    OrderRequestDTO shipped = new OrderRequestDTO();
    shipped.setOrderRequestId(1002);
    shipped.setStoreId(101);
    shipped.setSupplierIntegrationId(301);
    shipped.setProductId(1002);
    shipped.setRequestEmployeeId(9003);
    shipped.setApprovalEmployeeId(9002L);
    shipped.setOrderQuantity(10);
    shipped.setApprovedQuantity(10);
    shipped.setRequestReason("판매량 증가");
    shipped.setOrderStatus("SHIPPED");
    shipped.setRequestedAt(now.minusDays(2));
    shipped.setApprovedAt(now.minusDays(1));
    shipped.setSentToSupplierAt(now.minusHours(10));
    shipped.setExternalOrderId("EXT-1002");
    orders.add(shipped);
    externalOrderStatuses.put(shipped.getOrderRequestId(), "APPROVED");
  }

  private StoreDTO store(long storeId, long branchId, long brandId, String name, String floor,
      String location, String status) {
    StoreDTO store = new StoreDTO();
    store.setStoreId(storeId);
    store.setBranchId(branchId);
    store.setBrandId(brandId);
    store.setStoreName(name);
    store.setFloorInfo(floor);
    store.setStoreLocation(location);
    store.setOperationStatus(status);
    store.setCreatedAt(LocalDateTime.now());
    store.setUpdatedAt(LocalDateTime.now());
    return store;
  }

  private Long findBranchIdByStoreId(Long storeId) {
    if (storeId == null) {
      return null;
    }

    for (StoreDTO store : stores) {
      if (store.getStoreId() == storeId) {
        return store.getBranchId();
      }
    }

    return null;
  }

  private ProductDTO product(long productId, long brandId, long categoryId, String name, int price,
      String season, String status) {
    ProductDTO product = new ProductDTO();
    product.setProductId(productId);
    product.setBrandId(brandId);
    product.setCategoryId(categoryId);
    product.setProductName(name);
    product.setPrice(price);
    product.setSeasonType(season);
    product.setProductStatus(status);
    product.setCreatedAt(LocalDateTime.now());
    product.setUpdatedAt(LocalDateTime.now());
    return product;
  }

  private InventoryDTO inventory(long storeId, long productId, int currentQuantity,
      int safetyQuantity, String productName, int price, String season, String brand,
      String category) {
    InventoryDTO inventory = new InventoryDTO();
    inventory.setStoreId(storeId);
    inventory.setProductId(productId);
    inventory.setCurrentQuantity(currentQuantity);
    inventory.setSafetyQuantity(safetyQuantity);
    inventory.setUpdatedAt(LocalDateTime.now());
    inventory.setProductName(productName);
    inventory.setPrice(price);
    inventory.setSeasonType(season);
    inventory.setProductStatus(ProductStatus.ON_SALE);
    inventory.setBrandName(brand);
    inventory.setCategoryName(category);
    inventory.setLowStock(currentQuantity <= safetyQuantity);
    return inventory;
  }
}
