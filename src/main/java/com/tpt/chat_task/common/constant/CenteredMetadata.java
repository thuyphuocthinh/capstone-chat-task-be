package com.tpt.chat_task.common.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CenteredMetadata {
    private int countOther;
    private int countAbove;
    private int countBelow;
}
