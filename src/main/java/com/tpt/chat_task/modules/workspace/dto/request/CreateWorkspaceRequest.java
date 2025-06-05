package com.tpt.chat_task.modules.workspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateWorkspaceRequest {
    @NotBlank(message = "Workspace name cannot be blank")
    @Size(max = 255, message = "Workspace name max length is 255")
    private String name;
}
