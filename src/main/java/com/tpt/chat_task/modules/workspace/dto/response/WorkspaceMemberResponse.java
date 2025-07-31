package com.tpt.chat_task.modules.workspace.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspaceMemberResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private String roleInWorkspace;
}
