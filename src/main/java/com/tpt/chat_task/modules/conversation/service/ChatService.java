package com.tpt.chat_task.modules.conversation.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithCenteredMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.MessageRequest;
import com.tpt.chat_task.modules.conversation.dto.response.MessageElementResponse;
import com.tpt.chat_task.modules.conversation.dto.response.MessageResourceResponse;
import com.tpt.chat_task.modules.conversation.dto.response.MessageResponse;
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
    public String toggleReactMessage(String messageId, String iconId) throws NotFoundException;
    public List<MessageResponse> getPinnedMessagesOfConversation(String conversationId) throws NotFoundException;
    // search message (hard)
    public List<MessageResourceResponse> getListResourcesOfConversation(String conversationId) throws NotFoundException;
    public List<MessageResourceResponse> getListResourcesOfConversationAndType(String conversationId, RESOURCE_TYPE type) throws NotFoundException;
}
