package com.tpt.chat_task.modules.conversation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MessageRequest {
    private List<MessageElementRequest> elements;

    private List<MultipartFile> files;
}
