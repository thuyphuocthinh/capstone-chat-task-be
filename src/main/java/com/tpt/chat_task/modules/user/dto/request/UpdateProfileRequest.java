package com.tpt.chat_task.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UpdateProfileRequest {
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 255, message = "First name max length is 255")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 255, message = "Last name max length is 255")
    private String lastName;
}
