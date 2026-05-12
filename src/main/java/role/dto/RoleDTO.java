package role.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RoleDTO {

  private Long roleId;
  private String roleName;
  private String roleDescription;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
