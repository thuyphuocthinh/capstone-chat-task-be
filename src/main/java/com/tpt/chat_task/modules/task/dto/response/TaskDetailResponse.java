package com.tpt.chat_task.modules.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDetailResponse {
    private String id;
    private String title;
    private String description;
    private List<LabelDetailResponse> labels;
    private List<CheckListResponse> checklists;
    private List<WorkspaceMemberResponse> members;
    private List<TaskResourceResponse> files;
}
