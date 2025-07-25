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
@Data
@Builder
public class UpdateTaskCommentRequest {
    @NotBlank(message = "Comment content cannot be blank")
    private String content;

    private List<String> resourceLinks;

    private List<MultipartFile> files;

    private List<String> mentions;
}
