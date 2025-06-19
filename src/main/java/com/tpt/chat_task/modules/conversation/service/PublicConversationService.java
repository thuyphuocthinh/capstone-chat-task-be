package com.tpt.chat_task.modules.conversation.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.CreatePublicConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdatePublicConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.response.ConversationMemberResponse;
import com.tpt.chat_task.modules.conversation.dto.response.PublicConversationDetailResponse;

import java.util.List;

public interface PublicConversationService {
    public PublicConversationDetailResponse createNewPublicConversation(String workspaceId, CreatePublicConversationRequest request) throws NotFoundException;
    public PublicConversationDetailResponse getPublicConversationDetail(String conversationId) throws NotFoundException;
    public SuccessResponseWithMetadata<?> getListPublicConversations(String token, Integer page, Integer paging) throws NotFoundException;
    public SuccessResponseWithMetadata<?> searchListPublicConversations(String token, String name, Integer page, Integer paging) throws NotFoundException;
    public PublicConversationDetailResponse updatePublicConversation(String id, UpdatePublicConversationRequest request) throws NotFoundException;
    public String deletePublicConversation(String id) throws NotFoundException;
    public String togglePinnedConversation(String id) throws NotFoundException;
    public String addMemberToPublicConversation(String id, String userId) throws NotFoundException;
    public String removeMemberFromPublicConversation(String id, String userId) throws NotFoundException;
    public List<ConversationMemberResponse> getConversationMembers(String conversationId) throws NotFoundException;
}
