package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.constant.TaskBoardError;
import com.tpt.chat_task.modules.task.constant.TaskGroupError;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskGroupRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskGroupRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskGroupDetailResponse;
import com.tpt.chat_task.modules.task.entity.TaskBoard;
import com.tpt.chat_task.modules.task.entity.TaskGroup;
import com.tpt.chat_task.modules.task.repository.TaskBoardRepository;
import com.tpt.chat_task.modules.task.repository.TaskGroupRepository;
import com.tpt.chat_task.modules.task.service.TaskGroupService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskGroupServiceImpl implements TaskGroupService {
    private final TaskGroupRepository taskGroupRepository;

    private final TaskBoardRepository taskBoardRepository;

    @Override
    public TaskGroupDetailResponse addNewTaskGroup(String taskBoardId, CreateTaskGroupRequest createTaskGroupRequest) throws NotFoundException {
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));

        TaskGroup taskGroup = new TaskGroup();
        TaskGroup latestTaskGroupByTaskBoard = this.taskGroupRepository.findLastTaskGroupByTaskBoardId(taskBoardId);
        if(latestTaskGroupByTaskBoard != null) {
            taskGroup.setOrderIndex(latestTaskGroupByTaskBoard.getOrderIndex() + 1);
        } else {
            taskGroup.setOrderIndex(1);
        }

        taskGroup.setTaskBoard(taskBoard);
        taskGroup.setTitle(createTaskGroupRequest.getTitle());
        taskGroup = this.taskGroupRepository.save(taskGroup);

        return TaskGroupDetailResponse.builder()
                .title(taskGroup.getTitle())
                .id(taskGroup.getId())
                .build();
    }

    @Override
    public TaskGroupDetailResponse updateTaskGroup(String taskBoardId, String taskGroupId, UpdateTaskGroupRequest updateTaskGroupRequest) throws NotFoundException {
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        TaskGroup taskGroup = this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));

        taskGroup.setTitle(updateTaskGroupRequest.getTitle());
        taskGroup = this.taskGroupRepository.save(taskGroup);

        return TaskGroupDetailResponse.builder()
                .title(taskGroup.getTitle())
                .id(taskGroup.getId())
                .build();
    }

    @Override
    public String deleteTaskGroup(String taskBoardId, String taskGroupId) throws NotFoundException {
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        this.taskGroupRepository.deleteById(taskGroupId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public TaskGroupDetailResponse getTaskGroupDetail(String taskBoardId, String taskGroupId) throws NotFoundException {
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        TaskGroup taskGroup = this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        return TaskGroupDetailResponse.builder()
                .title(taskGroup.getTitle())
                .id(taskGroup.getId())
                .build();
    }

    @Override
    @Transactional
    public String changePosition(String taskBoardId, String taskGroupId, int newPosition) throws NotFoundException, BadRequestException {
        this.taskBoardRepository.findById(taskBoardId)
                .orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));

        TaskGroup taskGroup = this.taskGroupRepository.findById(taskGroupId)
                .orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));

        int totalGroups = this.taskGroupRepository.countTaskGroupsByTaskBoardId(taskBoardId);

        if (newPosition < 1 || newPosition > totalGroups) {
            throw new BadRequestException(TaskGroupError.TASK_GROUP_POSITION_INVALID);
        }

        int oldPosition = taskGroup.getOrderIndex();
        if (newPosition == oldPosition) {
            return RESPONSE_STATUS.SUCCESS.toString();
        }

        if (newPosition > oldPosition) {
            // Dời các taskGroup có orderIndex từ (oldPosition+1) đến newPosition về phía trước
            this.taskGroupRepository.decrementOrderIndexesInRange(taskBoardId, oldPosition + 1, newPosition);
        } else {
            // Dời các taskGroup có orderIndex từ newPosition đến (oldPosition-1) về phía sau
            this.taskGroupRepository.incrementOrderIndexesInRange(taskBoardId, newPosition, oldPosition - 1);
        }

        taskGroup.setOrderIndex(newPosition);
        this.taskGroupRepository.save(taskGroup);

        return RESPONSE_STATUS.SUCCESS.toString();
    }
}
