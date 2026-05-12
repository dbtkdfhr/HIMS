package role;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RoleDTO {

  private int roleId;
  private String roleName;
  private String roleDescription;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
