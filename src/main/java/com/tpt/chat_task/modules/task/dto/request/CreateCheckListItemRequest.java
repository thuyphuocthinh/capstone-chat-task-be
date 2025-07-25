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
public class CreateCheckListItemRequest {
    @NotBlank(message = "Check List Item Title Cannot Be Blank")
    private String title;
}
