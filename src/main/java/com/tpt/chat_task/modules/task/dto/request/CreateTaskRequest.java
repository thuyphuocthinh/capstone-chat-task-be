package com.tpt.chat_task.modules.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Data
public class CreateTaskRequest {
    @NotBlank(message = "Task title cannot be blank")
    private String title;
}
