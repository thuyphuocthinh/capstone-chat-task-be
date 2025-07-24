package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.constant.CheckListError;
import com.tpt.chat_task.modules.task.constant.CheckListItemError;
import com.tpt.chat_task.modules.task.constant.TaskError;
import com.tpt.chat_task.modules.task.dto.request.CreateCheckListItemRequest;
import com.tpt.chat_task.modules.task.dto.request.CreateCheckListRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateCheckListItemRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateCheckListRequest;
import com.tpt.chat_task.modules.task.dto.response.CheckListItemResponse;
import com.tpt.chat_task.modules.task.dto.response.CheckListResponse;
import com.tpt.chat_task.modules.task.entity.CheckList;
import com.tpt.chat_task.modules.task.entity.CheckListItem;
import com.tpt.chat_task.modules.task.entity.Task;
import com.tpt.chat_task.modules.task.repository.CheckListItemRepository;
import com.tpt.chat_task.modules.task.repository.CheckListRepository;
import com.tpt.chat_task.modules.task.repository.TaskRepository;
import com.tpt.chat_task.modules.task.service.CheckListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckListServiceImpl implements CheckListService {

    private final CheckListRepository checkListRepository;

    private final CheckListItemRepository checkListItemRepository;

    private final TaskRepository taskRepository;

    public CheckListResponse mapCheckListToResponse(CheckList checkList) {
        return CheckListResponse.builder()
                .id(checkList.getId())
                .title(checkList.getTitle())
                .items(
                        checkList.getChecklistItems() == null ? Collections.emptyList() :
                        checkList.getChecklistItems().stream().map(item -> mapCheckListItemToResponse(item)).toList()
                )
                .build();
    }

    public CheckListItemResponse mapCheckListItemToResponse(CheckListItem checkListItem) {
        return CheckListItemResponse.builder()
                .id(checkListItem.getId())
                .title(checkListItem.getTitle())
                .isDone(checkListItem.isDone())
                .build();
    }

    @Override
    public CheckListResponse addNewCheckList(String taskId, CreateCheckListRequest createCheckListRequest) throws NotFoundException {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));

        int totalCheckLists = task.getChecklists().size();
        CheckList checkList = CheckList.builder()
                .task(task)
                .title(createCheckListRequest.getTitle())
                .orderIndex(totalCheckLists + 1)
                .build();
        checkList = checkListRepository.save(checkList);

        return mapCheckListToResponse(checkList);
    }

    @Override
    public CheckListResponse updateNewCheckList(String taskId, String checkListId, UpdateCheckListRequest request) throws NotFoundException {
        this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        CheckList checkList = this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));

        String title = request.getTitle();

        if (title != null) {
            checkList.setTitle(title);
        }

        checkList = checkListRepository.save(checkList);

        return mapCheckListToResponse(checkList);
    }

    @Override
    public String deleteCheckList(String taskId, String checkListId) throws NotFoundException {
        this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));
        this.checkListRepository.deleteById(checkListId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public CheckListItemResponse addNewCheckListItem(String checkListId, CreateCheckListItemRequest createCheckListItemRequest) throws NotFoundException {
        CheckList checkList = this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));
        int totalItems = checkList.getChecklistItems().size();
        CheckListItem checkListItem = CheckListItem.builder()
                .title(createCheckListItemRequest.getTitle())
                .checklist(checkList)
                .orderIndex(totalItems++)
                .build();

        checkListItem = checkListItemRepository.save(checkListItem);
        return this.mapCheckListItemToResponse(checkListItem);
    }

    @Override
    public CheckListItemResponse updateCheckListItem(String checkListId, String checkListItemId, UpdateCheckListItemRequest request) throws NotFoundException {
        this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));
        CheckListItem item = this.checkListItemRepository.findById(checkListItemId).orElseThrow(() -> new NotFoundException(CheckListItemError.CHECK_LIST_ITEM_NOT_FOUND));
        String title = request.getTitle();
        if (title != null) {
            item.setTitle(title);
        }
        item = checkListItemRepository.save(item);
        return mapCheckListItemToResponse(item);
    }

    @Override
    public String deleteCheckListItem(String checkListId, String checkListItemId) throws NotFoundException {
        this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));
        this.checkListItemRepository.findById(checkListItemId).orElseThrow(() -> new NotFoundException(CheckListItemError.CHECK_LIST_ITEM_NOT_FOUND));
        this.checkListRepository.deleteById(checkListId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<CheckListItemResponse> getCheckListItemByCheckList(String checkListId) throws NotFoundException {
        CheckList checkList = this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));
        return checkList.getChecklistItems().stream().map(this::mapCheckListItemToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String changeCheckListPosition(String taskId, String checkListId, int newPosition) throws NotFoundException, BadRequestException {
        this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        CheckList checkList = this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));

        int totalItems = checkList.getChecklistItems().size();

        if (newPosition > totalItems || newPosition < 0) {
            throw new BadRequestException(CheckListError.INVALID_CHECK_LIST_POSITION);
        }

        if (newPosition == checkList.getOrderIndex()) {
            return RESPONSE_STATUS.SUCCESS.toString();
        }

        // 1 2 3 4 5 6 7 8 9
        int oldPosition = checkList.getOrderIndex();

        if(newPosition > oldPosition) {
            this.checkListRepository.decrementOrderIndexesInRange(taskId, oldPosition + 1, newPosition);
        } else {
            this.checkListRepository.incrementOrderIndexesInRange(taskId, newPosition, oldPosition - 1);
        }

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    @Transactional
    public String changeCheckListItemPosition(String checkListId, String checkListItemId, int newPosition) throws NotFoundException, BadRequestException {
        CheckList checkList = this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        CheckListItem item = this.checkListItemRepository.findById(checkListItemId).orElseThrow(() -> new NotFoundException(CheckListItemError.CHECK_LIST_ITEM_NOT_FOUND));
        int totalItems = checkList.getChecklistItems().size();
        if (newPosition > totalItems || newPosition < 0) {
            throw new BadRequestException(CheckListItemError.CHECK_LIST_ITEM_POSITION_INVALID);
        }
        if (newPosition == checkList.getOrderIndex()) {
            return RESPONSE_STATUS.SUCCESS.toString();
        }
        int oldPosition = checkList.getOrderIndex();
        if(newPosition > oldPosition) {
            this.checkListItemRepository.decrementOrderIndexesInRange(checkListId, oldPosition + 1, newPosition);
        } else {
            this.checkListItemRepository.incrementOrderIndexesInRange(checkListId, newPosition, oldPosition - 1);
        }
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String changeCheckListItemStatus(String checkListId, String checkListItemId) throws NotFoundException {
        this.checkListRepository.findById(checkListId).orElseThrow(() -> new NotFoundException(CheckListError.CHECK_LIST_NOT_FOUND));
        CheckListItem item = this.checkListItemRepository.findById(checkListItemId).orElseThrow(() -> new NotFoundException(CheckListItemError.CHECK_LIST_ITEM_NOT_FOUND));
        item.setDone(!item.isDone());
        checkListItemRepository.save(item);
        return RESPONSE_STATUS.SUCCESS.toString();
    }
}
