package com.tpt.chat_task.modules.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateTaskCommentRequest {
    @NotBlank(message = "Task comment cannot be blank")
    private String content;

    private List<String> mentions;

    private List<MultipartFile> files;
}
