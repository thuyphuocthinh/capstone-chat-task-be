package com.tpt.chat_task.modules.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class TaskCommentReplyResponse extends TaskCommentResponse {
    private List<TaskCommentResponse> taskCommentResponses;
}
