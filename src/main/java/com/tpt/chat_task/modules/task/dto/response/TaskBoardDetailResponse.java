package com.tpt.chat_task.modules.task.dto.response;

import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TaskBoardDetailResponse {
    private String id;
    private String title;
    private String backgroundImageUrl;
    private List<WorkspaceMemberResponse> members;
    private List<TaskGroupDetailResponse> groups;
}
