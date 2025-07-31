package com.tpt.chat_task.modules.user.dto.request;

import com.tpt.chat_task.modules.user.enums.USER_ROLE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class ChangeRoleRequest {
    private USER_ROLE role;
}
