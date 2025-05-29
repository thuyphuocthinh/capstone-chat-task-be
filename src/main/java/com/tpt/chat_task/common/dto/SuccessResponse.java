package com.tpt.chat_task.common.dto;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;

public class SuccessResponse {
    private RESPONSE_STATUS status = RESPONSE_STATUS.SUCCESS;
    private Object data;
}
