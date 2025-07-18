package com.tpt.chat_task.modules.task.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateCheckListItemRequest;
import com.tpt.chat_task.modules.task.dto.request.CreateCheckListRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateCheckListItemRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateCheckListRequest;
import com.tpt.chat_task.modules.task.dto.response.CheckListItemResponse;
import com.tpt.chat_task.modules.task.dto.response.CheckListResponse;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface CheckListService {
    public CheckListResponse addNewCheckList(String taskId, CreateCheckListRequest createCheckListRequest) throws NotFoundException;
    public CheckListResponse updateNewCheckList(String taskId, String checkListId, UpdateCheckListRequest request) throws NotFoundException;
    public String deleteCheckList(String taskId, String checkListId) throws NotFoundException;
    public CheckListItemResponse addNewCheckListItem(String checkListId, CreateCheckListItemRequest createCheckListItemRequest) throws NotFoundException;
    public CheckListItemResponse updateCheckListItem(String checkListId, String checkListItemId, UpdateCheckListItemRequest request) throws NotFoundException;
    public String deleteCheckListItem(String checkListId, String checkListItemId) throws NotFoundException;
    public List<CheckListItemResponse> getCheckListItemByCheckList(String checkListId) throws NotFoundException;
    public String changeCheckListPosition(String taskId, String checkListId, int newPosition) throws NotFoundException, BadRequestException;
    public String changeCheckListItemPosition(String checkListId, String checkListItemId, int newPosition) throws NotFoundException, BadRequestException;
    public String changeCheckListItemStatus(String checkListId, String checkListItemId) throws NotFoundException;
}

