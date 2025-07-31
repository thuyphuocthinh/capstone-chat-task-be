package com.tpt.chat_task.common.dto;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SuccessResponseWithMetadata<T> {
    @Builder.Default
    private String status = RESPONSE_STATUS.SUCCESS.toString();
    private T data;
    private Metadata metadata;
}
