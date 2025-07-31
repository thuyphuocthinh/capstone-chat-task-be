package com.tpt.chat_task.common.dto;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import lombok.*;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
@Data
public class ErrorResponse {
    @Builder.Default
    private String status = RESPONSE_STATUS.ERROR.toString();
    private String message;
}
