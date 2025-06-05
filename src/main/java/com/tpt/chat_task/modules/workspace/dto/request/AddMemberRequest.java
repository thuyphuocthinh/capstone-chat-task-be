package com.tpt.chat_task.modules.workspace.dto.request;

import com.tpt.chat_task.modules.workspace.enums.WORKSPACE_USER_ROLE;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class AddMemberRequest {
    @NotBlank(message = "User id cannot be blank")
    private String userId;

    @NotNull(message = "User role cannot be null")
    private WORKSPACE_USER_ROLE role;
}
