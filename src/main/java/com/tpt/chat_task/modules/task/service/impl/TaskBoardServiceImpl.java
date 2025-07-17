package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.constant.TaskBoardError;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskBoardDetailResponse;
import com.tpt.chat_task.modules.task.entity.TaskBoard;
import com.tpt.chat_task.modules.task.repository.TaskBoardRepository;
import com.tpt.chat_task.modules.task.service.TaskBoardService;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskBoardServiceImpl implements TaskBoardService {

    private final TaskBoardRepository taskBoardRepository;

    private final WorkspaceRepository workspaceRepository;

    @Override
    public TaskBoardDetailResponse createTaskBoard(String workspaceId, CreateTaskBoardRequest request) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = TaskBoard.builder()
                .title(request.getTitle())
                .backgroundUrl(request.getBackgroundImageUrl())
                .workspace(workspace)
                .build();
        taskBoard = this.taskBoardRepository.save(taskBoard);
        return TaskBoardDetailResponse.builder()
                .title(taskBoard.getTitle())
                .id(taskBoard.getId())
                .backgroundImageUrl(taskBoard.getBackgroundUrl())
                .build();
    }

    @Override
    public TaskBoardDetailResponse getTaskBoardDetail(String workspaceId, String taskBoardId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        return TaskBoardDetailResponse.builder()
                .title(taskBoard.getTitle())
                .id(taskBoard.getId())
                .backgroundImageUrl(taskBoard.getBackgroundUrl())
                .build();
    }

    @Override
    public TaskBoardDetailResponse updateTaskBoard(String workspaceId, String taskBoardId, UpdateTaskBoardRequest request) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));

        String title = taskBoard.getTitle();
        String backgroundImageUrl = taskBoard.getBackgroundUrl();

        if(title != null) {
            taskBoard.setTitle(title);
        }

        if(backgroundImageUrl != null) {
            taskBoard.setBackgroundUrl(backgroundImageUrl);
        }

        taskBoard = this.taskBoardRepository.save(taskBoard);

        return TaskBoardDetailResponse.builder()
                .title(taskBoard.getTitle())
                .id(taskBoard.getId())
                .backgroundImageUrl(taskBoard.getBackgroundUrl())
                .build();
    }

    @Override
    public String deleteTaskBoard(String workspaceId, String taskBoardId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        this.taskBoardRepository.deleteById(taskBoardId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<TaskBoardDetailResponse> getListTaskBoards(String workspaceId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        List<TaskBoard> taskBoards = this.taskBoardRepository.findAllByWorkspaceId(workspaceId);
        List<TaskBoardDetailResponse> taskBoardDetailResponses = taskBoards.stream().map(tb -> {
            return TaskBoardDetailResponse.builder()
                    .id(tb.getId())
                    .title(tb.getTitle())
                    .backgroundImageUrl(tb.getBackgroundUrl())
                    .build();
        }).toList();
        return taskBoardDetailResponses;
    }

    @Override
    public String togglePinTaskBoard(String workspaceId, String taskBoardId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        taskBoard.setPinned(!taskBoard.isPinned());
        return RESPONSE_STATUS.SUCCESS.toString();
    }
}
