package com.tpt.chat_task.modules.task.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateLabelRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateLabelRequest;
import com.tpt.chat_task.modules.task.dto.response.LabelDetailResponse;

import java.util.List;

public interface LabelService {
    public LabelDetailResponse addNewLabel(String taskBoardId, CreateLabelRequest createLabelRequest) throws NotFoundException;
    public LabelDetailResponse updateLabel(String taskBoardId, String labelId, UpdateLabelRequest updateLabelRequest) throws NotFoundException;
    public String deleteLabel(String taskBoardId, String labelId) throws NotFoundException;
    public List<LabelDetailResponse> getAllLabelsByTaskBoard(String taskBoardId) throws NotFoundException;
}
