package com.tpt.chat_task.modules.workspace.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpt.chat_task.modules.user.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspaceResponse {
    private String id;
    private String name;
    private List<WorkspaceMemberResponse> members = new ArrayList<>();
    private WorkspaceMemberResponse host;
}
