package com.tpt.chat_task.modules.workspace.dto.request;

import com.tpt.chat_task.modules.workspace.enums.WORKSPACE_USER_ROLE;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChangeRoleRequest {
    @NotNull(message = "Workspace role cannot be null")
    private WORKSPACE_USER_ROLE role;
}
