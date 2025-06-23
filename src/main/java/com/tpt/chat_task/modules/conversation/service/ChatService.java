package com.tpt.chat_task.modules.conversation.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithCenteredMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementRequest;
import com.tpt.chat_task.modules.conversation.dto.response.MessageElementResponse;
import com.tpt.chat_task.modules.conversation.dto.response.MessageResponse;

public interface ChatService {
    public MessageResponse addNewMessage(String conversationId, MessageElementRequest request) throws NotFoundException;
    public MessageResponse getMessageDetail(String conversationId, String messageId) throws NotFoundException;
    public MessageResponse updateMessage(String conversationId, String messageId, MessageElementRequest request) throws NotFoundException;
    public String deleteMessage(String conversationId, String messageId) throws NotFoundException;
    public String togglePinMessage(String conversationId, String messageId) throws NotFoundException;
    public SuccessResponseWithCenteredMetadata<?> getListOfMessages(String conversationId, Integer paging, boolean isAbove) throws NotFoundException;
    public MessageResponse replyMessage(String messageId, MessageElementRequest request) throws NotFoundException;
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessage(String messageId, Integer paging) throws NotFoundException;
    public String toggleReactMessage(String messageId, String iconId) throws NotFoundException;
    public SuccessResponseWithCenteredMetadata<?> getPinnedMessages(String messageId) throws NotFoundException;
    // search message (hard)
}
