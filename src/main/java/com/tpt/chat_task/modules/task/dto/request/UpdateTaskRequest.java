package com.tpt.chat_task.modules.task.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
}
