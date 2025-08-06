package com.tpt.chat_task.modules.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskCommentResponse {
    private String id;
    private String senderId;
    private String content;
    private LocalDateTime createdAt;
    private List<String> files;
    private List<String> mentions;
}
