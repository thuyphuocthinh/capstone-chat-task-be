package com.tpt.chat_task.common.dto;

import com.tpt.chat_task.common.constant.CenteredMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuccessResponseWithCenteredMetadata<T> {
    @Builder.Default
    private String status = RESPONSE_STATUS.SUCCESS.toString();
    private T data;
    private CenteredMetadata metadata;
}
