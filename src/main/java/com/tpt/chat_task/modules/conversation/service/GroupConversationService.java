package com.tpt.chat_task.modules.conversation.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.CreateGroupConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdateGroupConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.response.ConversationMemberResponse;
import com.tpt.chat_task.modules.conversation.dto.response.GroupConversationDetailResponse;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface GroupConversationService {
    public GroupConversationDetailResponse createNewGroupConversation(String workspaceId, CreateGroupConversationRequest request) throws NotFoundException;
    public GroupConversationDetailResponse getGroupConversationDetail(String workspaceId, String conversationId) throws NotFoundException;
    public SuccessResponseWithMetadata<?> getListGroupConversations(String workspaceId, String token, Integer page, Integer paging) throws NotFoundException;
    public SuccessResponseWithMetadata<?> searchListGroupConversations(String workspaceId, String token, String name, Integer page, Integer paging) throws NotFoundException;
    public GroupConversationDetailResponse updateGroupConversation(String workspaceId, String id, UpdateGroupConversationRequest request) throws NotFoundException;
    public String deleteGroupConversation(String workspaceId, String id) throws NotFoundException;
    public String togglePinnedConversation(String workspaceId, String id) throws NotFoundException;
    public String addMemberToGroupConversation(String workspaceId, String id, String userId) throws NotFoundException, BadRequestException;
    public String removeMemberFromGroupConversation(String workspaceId, String id, String userId) throws NotFoundException, BadRequestException;
    public List<ConversationMemberResponse> getConversationMembers(String workspaceId, String conversationId) throws NotFoundException;
}
