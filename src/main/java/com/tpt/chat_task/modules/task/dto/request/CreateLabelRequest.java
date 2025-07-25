package com.tpt.chat_task.modules.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateLabelRequest {
    @NotBlank(message = "Label title cannot be blank")
    private String title;

    @NotBlank(message = "Label color cannot be blank")
    private String color;
}
