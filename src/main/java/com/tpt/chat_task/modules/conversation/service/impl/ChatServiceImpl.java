package com.tpt.chat_task.modules.conversation.service.impl;

import com.tpt.chat_task.common.dto.SuccessResponseWithCenteredMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementRequest;
import com.tpt.chat_task.modules.conversation.dto.response.MessageElementResponse;
import com.tpt.chat_task.modules.conversation.dto.response.MessageResponse;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_STYLE;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import com.tpt.chat_task.modules.conversation.repository.ConversationRepository;
import com.tpt.chat_task.modules.conversation.repository.MessageElementRepository;
import com.tpt.chat_task.modules.conversation.repository.MessageRepository;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final MessageElementRepository messageElementRepository;

    private final MessageRepository messageRepository;

    private final ConversationRepository conversationRepository;

    @Override
    @Transactional
    public MessageResponse addNewMessage(String conversationId, MessageElementRequest request) throws NotFoundException {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        // request type chi co the la TEXT_LIST | TEXT_SECTION
        MESSAGE_ELEMENT_TYPE type = request.getType();

        // style: ORDERED | NUMERIC
        MESSAGE_ELEMENT_STYLE style = request.getStyle();

        // 1. Neu co files => tao ham upload file => tao resource service
        // 2. Otherwise,
        /*
        *  -  Viet ham build TEXT_LIST (nho validation), tra ve MessageElement
        *  -  Viet ham build TEXT_SECTION (nho validation), tra ve MessageElement
        * */
        // 3. Thuc hien luu message
        // 4. Viet ham map sang message response


        return null;
    }

    @Override
    public MessageResponse getMessageDetail(String conversationId, String messageId) throws NotFoundException {
        return null;
    }

    @Override
    public MessageResponse updateMessage(String conversationId, String messageId, MessageElementRequest request) throws NotFoundException {
        return null;
    }

    @Override
    public String deleteMessage(String conversationId, String messageId) throws NotFoundException {
        return "";
    }

    @Override
    public String togglePinMessage(String conversationId, String messageId) throws NotFoundException {
        return "";
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListOfMessages(String conversationId, Integer paging, boolean isAbove) throws NotFoundException {
        return null;
    }

    @Override
    public MessageResponse replyMessage(String messageId, MessageElementRequest request) throws NotFoundException {
        return null;
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessage(String messageId, Integer paging) throws NotFoundException {
        return null;
    }

    @Override
    public String toggleReactMessage(String messageId, String iconId) throws NotFoundException {
        return "";
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getPinnedMessages(String messageId) throws NotFoundException {
        return null;
    }
}
