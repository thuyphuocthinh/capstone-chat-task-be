package com.tpt.chat_task.modules.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CheckListResponse {
    private String id;
    private String title;
    private List<CheckListItemResponse> items;
}
