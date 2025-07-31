package com.tpt.chat_task.common.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Metadata {
    private int currentPage;
    private int totalPages;
    private int totalElements;
    private int pageSize;
}
