package com.tpt.chat_task.infrastructure.rabbitmq.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TaskMemberRequest {
    private String taskId;
    private String userId;
}
