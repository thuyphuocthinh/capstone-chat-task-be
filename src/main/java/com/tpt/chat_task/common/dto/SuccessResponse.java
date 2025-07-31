package com.tpt.chat_task.common.dto;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SuccessResponse {
    @Builder.Default
    private String status = RESPONSE_STATUS.SUCCESS.toString();
    private Object data;
}
