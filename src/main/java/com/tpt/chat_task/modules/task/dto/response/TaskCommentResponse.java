package com.tpt.chat_task.modules.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskCommentResponse {
    private String id;
    private String senderId;
    private String content;
    private List<String> files;
    private List<String> mentions;
}
