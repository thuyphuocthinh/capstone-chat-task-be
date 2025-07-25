package com.tpt.chat_task.modules.task.dto.response;

import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TaskResourceResponse {
    private String id;
    private String name;
    private String link;
    private RESOURCE_TYPE type;
}
