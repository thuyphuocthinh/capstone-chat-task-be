package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.constant.LabelError;
import com.tpt.chat_task.modules.task.constant.TaskBoardError;
import com.tpt.chat_task.modules.task.dto.request.CreateLabelRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateLabelRequest;
import com.tpt.chat_task.modules.task.dto.response.LabelDetailResponse;
import com.tpt.chat_task.modules.task.entity.Label;
import com.tpt.chat_task.modules.task.entity.TaskBoard;
import com.tpt.chat_task.modules.task.repository.LabelRepository;
import com.tpt.chat_task.modules.task.repository.TaskBoardRepository;
import com.tpt.chat_task.modules.task.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;

    private final TaskBoardRepository taskBoardRepository;

    private LabelDetailResponse mapLabelToDetailResponse(Label label) {
        return LabelDetailResponse.builder()
                .id(label.getId())
                .title(label.getTitle())
                .color(label.getColor())
                .build();
    }

    @Override
    public LabelDetailResponse addNewLabel(String taskBoardId, CreateLabelRequest createLabelRequest) throws NotFoundException {
        TaskBoard taskBoard = taskBoardRepository.findById(taskBoardId).orElseThrow(()->new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));

        Label label = Label.builder()
                .title(createLabelRequest.getTitle())
                .color(createLabelRequest.getColor())
                .taskBoard(taskBoard)
                .build();

        label = this.labelRepository.save(label);

        return mapLabelToDetailResponse(label);
    }

    @Override
    public LabelDetailResponse updateLabel(String taskBoardId, String labelId, UpdateLabelRequest updateLabelRequest) throws NotFoundException {
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(()->new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        Label label = this.labelRepository.findById(labelId).orElseThrow(() -> new NotFoundException(LabelError.LABEL_NOT_FOUND));

        String newTitle = updateLabelRequest.getTitle();
        String newColor = updateLabelRequest.getColor();

        if(newTitle != null && newTitle != ""){
            label.setTitle(newTitle);
        }

        if(newColor != null && newColor != ""){
            label.setColor(newColor);
        }

        label = this.labelRepository.save(label);

        return mapLabelToDetailResponse(label);
    }

    @Override
    public String deleteLabel(String taskBoardId, String labelId) throws NotFoundException {
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(()->new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        this.labelRepository.findById(labelId).orElseThrow(() -> new NotFoundException(LabelError.LABEL_NOT_FOUND));
        this.labelRepository.deleteById(labelId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<LabelDetailResponse> getAllLabelsByTaskBoard(String taskBoardId) throws NotFoundException {
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(()->new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        List<Label> labels = this.labelRepository.findAllByTaskBoard(taskBoardId);
        return labels.stream().map(this::mapLabelToDetailResponse).collect(Collectors.toList());
    }
}
