package com.tpt.chat_task.modules.conversation.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.CreatePrivateConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.response.ConversationMemberResponse;
import com.tpt.chat_task.modules.conversation.dto.response.PrivateConversationDetailResponse;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface PrivateConversationService {
    public PrivateConversationDetailResponse createPrivateConversation(String workspaceId, CreatePrivateConversationRequest request) throws NotFoundException, BadRequestException;
    public String deletePrivateConversation(String workspaceId, String conversationId) throws NotFoundException;
    public PrivateConversationDetailResponse getPrivateConversationDetail(String workspaceId, String conversationId) throws NotFoundException;
    public SuccessResponseWithMetadata<?> getListPrivateConversations(String workspaceId, String token, Integer page, Integer paging) throws NotFoundException;
    public String togglePinPrivateConversation(String workspaceId, String conversationId) throws NotFoundException;
    public List<ConversationMemberResponse> getMembersOfPrivateConversation(String workspaceId, String conversationId) throws NotFoundException;
}
