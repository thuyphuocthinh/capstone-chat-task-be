package com.tpt.chat_task.common.dto;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class ErrorResponse {
    private RESPONSE_STATUS status = RESPONSE_STATUS.ERROR;
    private String message;
}
