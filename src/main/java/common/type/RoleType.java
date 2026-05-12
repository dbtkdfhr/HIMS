package common.type;

import lombok.Getter;

@Getter
public enum RoleType {
  BRANCH_MANAGER(1, "지점관리자"), SUPPLIER_MANAGER(2, "발주처담당자"), STORE_MANAGER(3,
      "입점매장담당자"), SYSTEM_MANAGER(4, "시스템담당자"), STAFF(5, "일반직원");

  private final Integer roleId;
  private final String roleName;

  RoleType(int roleId, String roleName) {
    this.roleId = roleId;
    this.roleName = roleName;
  }

  public static RoleType fromRoleId(int roleId) {
    for (RoleType roleType : RoleType.values()) {
      if (roleType.roleId == roleId) {
        return roleType;
      }
    }

    throw new IllegalArgumentException("존재하지 않는 권한 ID" + roleId);
  }
}
