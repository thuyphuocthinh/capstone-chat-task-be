package com.tpt.chat_task.modules.conversation.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithCenteredMetadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.MessageRequest;
import com.tpt.chat_task.modules.conversation.dto.response.MessageElementResponse;
import com.tpt.chat_task.modules.conversation.dto.response.MessageResourceResponse;
import com.tpt.chat_task.modules.conversation.dto.response.MessageResponse;
import com.tpt.chat_task.modules.conversation.entity.Message;
import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;

import java.io.IOException;
import java.util.List;

public interface ChatService {
    public MessageResponse addNewMessage(String token, String conversationId, MessageRequest request) throws NotFoundException, IOException;
    public MessageResponse getMessageDetail(String conversationId, String messageId) throws NotFoundException;
    public MessageResponse updateMessage(String conversationId, String messageId, MessageRequest request) throws NotFoundException, IOException;
    public String deleteMessage(String conversationId, String messageId) throws NotFoundException;
    public String togglePinMessage(String conversationId, String messageId) throws NotFoundException;
    public SuccessResponseWithCenteredMetadata<?> getListOfMessages(String conversationId, Integer paging) throws NotFoundException;
    public SuccessResponseWithCenteredMetadata<?> getListOfMessagesAboveOrBelow(String conversationId, String messageId, Integer paging, boolean isAbove) throws NotFoundException;
    public MessageResponse replyMessage(String token, String messageId, MessageRequest request) throws NotFoundException, IOException;
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessage(String messageId, Integer paging) throws NotFoundException;
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessageAboveOrBelow(String parentId, String messageId, Integer paging, boolean isAbove) throws NotFoundException;
    public String toggleReactMessage(String token, String messageId, String iconId) throws NotFoundException;
    public List<MessageResponse> getPinnedMessagesOfConversation(String conversationId) throws NotFoundException;
    public List<MessageResourceResponse> getListResourcesOfConversation(String conversationId) throws NotFoundException;
    public List<MessageResourceResponse> getListResourcesOfConversationAndType(String conversationId, RESOURCE_TYPE type) throws NotFoundException;
    public MessageResponse mapMessageToMessageResponse(Message message);
    public String markReadMessagesByConversation(String token, String conversationI) throws NotFoundException;
    public List<MessageResponse> searchMessagesByConversationAndKeyword(String conversationId, String keyword) throws NotFoundException;
    public SuccessResponseWithMetadata<?> getListThreadsOfWorkspace(String token, String workspaceId, Integer paging, Integer page) throws NotFoundException;
}
