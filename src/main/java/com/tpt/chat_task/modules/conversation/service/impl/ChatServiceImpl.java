package com.tpt.chat_task.modules.conversation.service.impl;

import com.tpt.chat_task.common.constant.CenteredMetadata;
import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithCenteredMetadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.EXCHANGE_TYPE;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.PUSH_NOTIFICATION_TYPE;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.PushNotificationAction;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementContentRequest;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementRequest;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementSectionRequest;
import com.tpt.chat_task.modules.conversation.dto.request.MessageRequest;
import com.tpt.chat_task.modules.conversation.dto.response.*;
import com.tpt.chat_task.modules.conversation.entity.*;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import com.tpt.chat_task.modules.conversation.repository.*;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import com.tpt.chat_task.modules.conversation.service.IconService;
import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import com.tpt.chat_task.modules.resource.repository.ResourceRepository;
import com.tpt.chat_task.modules.resource.service.ResourceService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final MessageElementRepository messageElementRepository;

    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    private final ConversationRepository conversationRepository;

    private final ResourceService resourceService;

    private final JwtProvider jwtProvider;

    private final IconService iconService;

    private final IconRepository iconRepository;

    private final MessageReactionRepository messageReactionRepository;

    private final RabbitTemplate rabbitTemplate;

    private final ExecutorService executorService;

    private final ResourceRepository resourceRepository;

    private final MessageSeenRepository messageSeenRepository;

    private final WorkspaceRepository workspaceRepository;

    @Override
    @Transactional
    public MessageResponse addNewMessage(String token, String conversationId, MessageRequest request) throws NotFoundException, IOException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User sender = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        List<MessageElementRequest> elements = request.getElements();
        List<MultipartFile> files = request.getFiles();
        Message message = buildAndSaveMessage(request, sender, files);
        MessageResponse response = this.mapMessageToMessageResponse(message);
        pushToQueueAsyncSendMessageAction(request, conversationId, response, PushNotificationAction.SEND_MESSAGE);
        return response;
    }

    private Message buildAndSaveMessage(MessageRequest request, User sender, List<MultipartFile> files) throws IOException {
        Message message = new Message();
        if (files != null && !files.isEmpty()) {
            List<Resource> resources = resourceService.uploadMultipleFiles(files);
            message.setResources(resources);
        }

        if (request.getElements().isEmpty()) {
            throw new BadRequestException(ConversationError.INVALID_REQUEST_CREATE_MESSAGE);
        }

        List<MessageElement> messageElements = this.buildMessageElements(request.getElements());
        message.setMessageElements(messageElements);
        message.setUser(sender);

        return messageRepository.save(message);
    }

    private void pushToQueueAsyncSendMessageAction(MessageRequest request, String conversationId, MessageResponse response, String action) {
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        String exchangeName = conversation.getType().compareTo(CONVERSATION_TYPE.PRIVATE) > 0 ? RabbitMQSchema.PRIVATE_CHAT_EXCHANGE : RabbitMQSchema.GROUP_CHAT_EXCHANGE;
        executorService.submit(() -> {
            if (checkMentionAll(request.getElements())) {
                rabbitTemplate.convertAndSend(
                        exchangeName,
                        RabbitMQSchema.getGroupChatRoutingKey(conversationId),
                        buildRabbitRequest(RabbitMQSchema.getGroupChatAllRoutingKey(conversationId), response, action)
                );
            } else {
                extractUserIds(request.getElements()).forEach(id -> {
                    rabbitTemplate.convertAndSend(
                            exchangeName,
                            RabbitMQSchema.getGroupChatRoutingKey(conversationId),
                            buildRabbitRequest(RabbitMQSchema.getGroupChatMentionRoutingKey(conversationId, id), response, action)
                    );
                });
            }
        });
    }

    private RabbitMQRequest buildRabbitRequest(String routingKey, MessageResponse response, String action) {
        return RabbitMQRequest.builder()
                .exchangeType(EXCHANGE_TYPE.TOPIC)
                .routingKey(routingKey)
                .payload(response)
                .pushNotificationAction(action)
                .pushNotificationType(PUSH_NOTIFICATION_TYPE.MESSAGE)
                .build();
    }

    @Override
    public MessageResponse mapMessageToMessageResponse(Message message){
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(message.getId());
        messageResponse.setSenderId(message.getUser().getId());
        messageResponse.setPinned(message.isPinned());
        messageResponse.setConversationId(message.getConversation().getId());
        messageResponse.setFiles(message.getResources().stream().map(resource -> {
            return MessageResourceResponse.builder()
                    .id(resource.getId())
                    .url(resource.getLink())
                    .name(resource.getName())
                    .resourceType(resource.getType())
                    .createdAt(resource.getCreatedAt())
                    .build();
        }).toList());
        messageResponse.setReactions(iconService.getReactionsByMessageId(message.getId()));
        // messageResponse.setUserReplyIds (if exist)
        if(message.getParentId() == null){
            messageResponse.setUserIds(new ArrayList<>());
        } else {
            messageResponse.setUserIds(this.getRepliesMessage(message.getId()).stream().map(m -> m.getUser().getId()).collect(Collectors.toList()));
        }
        messageResponse.setElements(this.mapMessageElementsToResponse(message.getMessageElements()));
        return messageResponse;
    }

    @Override
    public String markReadMessagesByConversation(String token, String conversationId) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        this.messageSeenRepository.markReadMessages(conversationId, userId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<MessageResponse> searchMessagesByConversationAndKeyword(String conversationId, String keyword) throws NotFoundException {
        List<Message> messages = this.messageRepository.searchMessageElementsByConversationIdAndKeyword(conversationId, keyword);
        return messages.stream().map(this::mapMessageToMessageResponse).toList();
    }

    @Override
    public SuccessResponseWithMetadata<?> getListThreadsOfWorkspace(String token, String workspaceId, Integer paging, Integer page) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Message> threadsPage = this.messageRepository.findAllThreadMessages(userId, pageable);
        List<Message> allThreadMessages = threadsPage.getContent();
        Map<String, List<Message>> threads = allThreadMessages.stream()
                .collect(Collectors.groupingBy(msg -> msg.getParentId() == null ? msg.getId() : msg.getParentId()));

        List<MessageResponse> messageResponses = new ArrayList<>();
        for (Map.Entry<String, List<Message>> entry : threads.entrySet()) {
            List<Message> messagesInThread = entry.getValue();
            for(Message message : messagesInThread) {
                messageResponses.add(this.mapMessageToMessageResponse(message));
            }
        }

        Metadata metadata = Metadata.builder()
                .currentPage(threadsPage.getNumber() + 1)
                .totalPages(threadsPage.getTotalPages())
                .totalElements((int) threadsPage.getTotalElements())
                .pageSize(threadsPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(messageResponses)
                .build();
    }

    private List<Message> getRepliesMessage(String messageId){
        this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(ConversationError.MESSAGE_NOT_FOUND));
        return this.messageRepository.getRepliesByMessageId(messageId);
    }

    private boolean checkMentionAll(List<MessageElementRequest> requests) {
        for (MessageElementRequest listElement : requests) {
            if (listElement.getElements() != null) {
                for (MessageElementSectionRequest section : listElement.getElements()) {
                    if (section.getElements() != null) {
                        for (MessageElementContentRequest content : section.getElements()) {
                            if (content.getType() == MESSAGE_ELEMENT_TYPE.TEXT &&
                                    "@channel".equalsIgnoreCase(content.getContent().trim())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private List<String> extractUserIds(List<MessageElementRequest> requests) {
        Set<String> userIds = new HashSet<>();
        for (MessageElementRequest listElement : requests) {
            if (listElement.getElements() != null) {
                for (MessageElementSectionRequest section : listElement.getElements()) {
                    if (section.getElements() != null) {
                        for (MessageElementContentRequest content : section.getElements()) {
                            if(content.getType() == MESSAGE_ELEMENT_TYPE.USER) {
                                userIds.add(content.getContent().trim());
                            }
                        }
                    }
                }
            }
        }
        return userIds.stream().sorted().collect(Collectors.toList());
    }

    private List<MessageElement> buildMessageElements(List<MessageElementRequest> requestElements) {
        List<MessageElement> result = new ArrayList<>();

        for (MessageElementRequest elementReq : requestElements) {
            if (elementReq.getType() == MESSAGE_ELEMENT_TYPE.TEXT_LIST) {
                String textListId = UUID.randomUUID().toString();

                // TEXT_LIST
                MessageElement listElement = new MessageElement();
                listElement.setId(textListId);
                listElement.setParentId(null);
                listElement.setType(elementReq.getType());
                listElement.setStyle(elementReq.getStyle());
                listElement.setIndent(elementReq.getIndent());
                result.add(listElement);

                for (MessageElementSectionRequest sectionReq : elementReq.getElements()) {
                    // TEXT_SECTION
                    String sectionId = UUID.randomUUID().toString();
                    MessageElement sectionElement = new MessageElement();
                    sectionElement.setId(sectionId);
                    sectionElement.setParentId(textListId);
                    sectionElement.setType(sectionReq.getType());
                    sectionElement.setIndent(0);
                    result.add(sectionElement);

                    for (MessageElementContentRequest contentReq : sectionReq.getElements()) {
                        String contentId = UUID.randomUUID().toString();

                        // TEXT / EMOJI / USER
                        MessageElement contentElement = new MessageElement();
                        contentElement.setId(contentId);
                        contentElement.setParentId(sectionId);
                        contentElement.setType(contentReq.getType());
                        contentElement.setContent(contentReq.getContent());
                        contentElement.setBold(contentReq.isBold());
                        contentElement.setItalic(contentReq.isItalic());
                        contentElement.setUnderline(contentReq.isUnderline());
                        result.add(contentElement);
                    }
                }
            } else if (elementReq.getType() == MESSAGE_ELEMENT_TYPE.TEXT_SECTION) {
                String sectionId = UUID.randomUUID().toString();

                // TEXT_SECTION trực tiếp
                MessageElement sectionElement = new MessageElement();
                sectionElement.setId(sectionId);
                sectionElement.setParentId(null);
                sectionElement.setType(elementReq.getType());
                sectionElement.setIndent(elementReq.getIndent());
                result.add(sectionElement);

                for (MessageElementSectionRequest sectionReq : elementReq.getElements()) {
                    // TEXT_SECTION (cha của TEXT/EMOJI/USER)
                    sectionElement.setId(sectionId);
                    sectionElement.setType(sectionReq.getType());
                    sectionElement.setIndent(0);
                    result.add(sectionElement);

                    for (MessageElementContentRequest contentReq : sectionReq.getElements()) {
                        String contentId = UUID.randomUUID().toString();

                        MessageElement contentElement = getMessageElement(contentReq, contentId, sectionId);
                        result.add(contentElement);
                    }
                }

            }
        }

        return result;
    }

    private static MessageElement getMessageElement(MessageElementContentRequest contentReq, String contentId, String sectionId) {
        MessageElement contentElement = new MessageElement();
        contentElement.setId(contentId);
        contentElement.setParentId(sectionId);
        contentElement.setType(contentReq.getType());
        contentElement.setContent(contentReq.getContent());
        contentElement.setBold(contentReq.isBold());
        contentElement.setItalic(contentReq.isItalic());
        contentElement.setUnderline(contentReq.isUnderline());
        return contentElement;
    }

    private List<MessageElementResponse> mapMessageElementsToResponse(List<MessageElement> messageElements) {
        // GET TEXT_LIST && // GET TEXT_SECTION WITHOUT PARENT_ID
        Map<String, MessageElementResponse> mapIdToResponse = new HashMap<>();
        List<MessageElementResponse> result = new ArrayList<>();

        for (MessageElement messageElement : messageElements) {
            if (messageElement.getType() == MESSAGE_ELEMENT_TYPE.TEXT_LIST) {
                MessageElementResponse textListResponse = new MessageElementResponse();
                textListResponse.setType(MESSAGE_ELEMENT_TYPE.TEXT_LIST);
                textListResponse.setStyle(messageElement.getStyle());
                textListResponse.setIndent(messageElement.getIndent());
                textListResponse.setElements(new ArrayList<>());
                mapIdToResponse.put(messageElement.getId(), textListResponse);
                result.add(textListResponse);
            }
            if (messageElement.getType() == MESSAGE_ELEMENT_TYPE.TEXT_SECTION && messageElement.getParentId() == null) {
                MessageElementResponse sectionResponse = new MessageElementResponse();
                sectionResponse.setType(MESSAGE_ELEMENT_TYPE.TEXT_SECTION);
                sectionResponse.setStyle(messageElement.getStyle());
                sectionResponse.setIndent(messageElement.getIndent());
                sectionResponse.setElements(new ArrayList<>());
                mapIdToResponse.put(messageElement.getId(), sectionResponse);
                result.add(sectionResponse);
            }
        }

        // GET TEXT SECTION WITH PARENT_ID
        for(MessageElement messageElement : messageElements) {
            if(messageElement.getType() == MESSAGE_ELEMENT_TYPE.TEXT_SECTION && messageElement.getParentId() != null) {
                String parentId = messageElement.getParentId();
                MessageElementResponse parentElement = mapIdToResponse.get(parentId);
                if(parentElement != null) {
                    MessageElementSectionResponse sectionResponse = new MessageElementSectionResponse();
                    sectionResponse.setType(MESSAGE_ELEMENT_TYPE.TEXT_SECTION);
                    List<MessageElementSectionResponse> sectionResponses = parentElement.getElements();
                    sectionResponses.add(sectionResponse);
                    mapIdToResponse.put(messageElement.getId(), sectionResponse);
                }
            }
        }

        // GET TEXT WITH PARENT ID
        for (MessageElement element : messageElements) {
            if (element.getType() == MESSAGE_ELEMENT_TYPE.TEXT && element.getParentId() != null) {
                MessageElementSectionResponse parent = (MessageElementSectionResponse) mapIdToResponse.get(element.getParentId());
                if (parent != null) {
                    MessageElementContentResponse text = new MessageElementContentResponse();
                    text.setType(MESSAGE_ELEMENT_TYPE.TEXT);
                    text.setContent(element.getContent());
                    text.setStyle(element.getStyle());
                    text.setIndent(element.getIndent());
                    parent.getContentElements().add(text);
                }
            }
        }

        return result;
    }

    @Override
    public MessageResponse getMessageDetail(String conversationId, String messageId) throws NotFoundException {
        this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(conversationId));
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        return this.mapMessageToMessageResponse(message);
    }

    @Override
    @Transactional
    public MessageResponse updateMessage(String conversationId, String messageId, MessageRequest request) throws NotFoundException, IOException {
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        this.messageElementRepository.deleteByMessageId(messageId);
        List<MessageElement> messageElements = this.buildMessageElements(request.getElements());
        message.setMessageElements(messageElements);
        this.messageRepository.save(message);
        MessageResponse response = this.mapMessageToMessageResponse(message);
        this.pushToQueueAsyncSendMessageAction(request, conversationId, response, PushNotificationAction.UPDATE_MESSAGE);
        return response;
    }

    @Override
    public String deleteMessage(String conversationId, String messageId) throws NotFoundException {
        this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(conversationId));
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        this.messageRepository.deleteById(message.getId());
        // TODO: ban realtime
        String exchangeName = message.getConversation().getType().compareTo(CONVERSATION_TYPE.PRIVATE) > 0 ? RabbitMQSchema.PRIVATE_CHAT_EXCHANGE : RabbitMQSchema.GROUP_CHAT_EXCHANGE;
        rabbitTemplate.convertAndSend(
                exchangeName,
                RabbitMQSchema.getGroupChatRoutingKey(conversationId),
                buildRabbitRequest(
                        RabbitMQSchema.getGroupChatAllRoutingKey(conversationId),
                        this.mapMessageToMessageResponse(message),
                        PushNotificationAction.DELETE_MESSAGE
                )
        );
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String togglePinMessage(String conversationId, String messageId) throws NotFoundException {
        this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(conversationId));
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        message.setPinned(!message.isPinned());
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListOfMessages(String conversationId, Integer paging) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(conversationId));
        List<Message> messageList = this.messageRepository.getListMessagesByConversationId(conversationId, paging);
        List<MessageResponse> messageResponses = messageList.stream().map(this::mapMessageToMessageResponse).toList();

        Integer countAbove = this.messageRepository.countAboveMessages(conversationId, messageList.get(messageList.size() - 1).getCreatedAt());
        Integer countBelow = this.messageRepository.countBelowMessages(conversationId, messageList.get(0).getCreatedAt());
        int countOther = countAbove + countBelow - paging;
        CenteredMetadata centeredMetadata = new CenteredMetadata();
        centeredMetadata.setCountOther(countOther);
        centeredMetadata.setCountAbove(countAbove);
        centeredMetadata.setCountBelow(countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(centeredMetadata)
                .build();
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListOfMessagesAboveOrBelow(String conversationId, String messageId, Integer paging, boolean isAbove) throws NotFoundException {
        this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(conversationId));
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));

        List<Message> messageList;
        if(isAbove) {
            messageList = this.messageRepository.getListMessagesByConversationIdAndBelowTime(conversationId, message.getCreatedAt(), paging);
        } else {
            messageList = this.messageRepository.getListMessagesByConversationIdAndBelowTime(conversationId, message.getCreatedAt(), paging);
        }

        List<MessageResponse> messageResponses = messageList.stream().map(this::mapMessageToMessageResponse).toList();

        Integer countAbove = this.messageRepository.countAboveMessages(conversationId, message.getCreatedAt());
        Integer countBelow = this.messageRepository.countBelowMessages(conversationId, message.getCreatedAt());
        int countOther = countAbove + countBelow - paging;
        CenteredMetadata centeredMetadata = new CenteredMetadata();
        centeredMetadata.setCountOther(countOther);
        centeredMetadata.setCountAbove(countAbove);
        centeredMetadata.setCountBelow(countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(centeredMetadata)
                .build();
    }

    @Override
    public MessageResponse replyMessage(String token, String messageId, MessageRequest request) throws NotFoundException, IOException {
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        String userId = this.jwtProvider.getIdFromToken(token);
        User sender = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        List<MessageElementRequest> elements = request.getElements();
        List<MultipartFile> files = request.getFiles();
        Message replyMessage = new Message();

        if(files != null && !files.isEmpty()) {
            List<Resource> resources = resourceService.uploadMultipleFiles(files);
            message.setResources(resources);
        }

        if(elements.isEmpty()){
            throw new BadRequestException(ConversationError.INVALID_REQUEST_CREATE_MESSAGE);
        }

        List<MessageElement> messageElements = this.buildMessageElements(elements);
        message.setMessageElements(messageElements);

        replyMessage.setUser(sender);
        replyMessage.setParentId(message.getId());
        if(!message.isThreadRoot()) {
            message.setThreadRoot(true);
            this.messageRepository.save(message);
        }
        replyMessage = this.messageRepository.save(message);

        MessageResponse response = this.mapMessageToMessageResponse(message);
        this.pushToQueueAsyncSendMessageAction(request, message.getConversation().getId(), response, PushNotificationAction.SEND_MESSAGE);

        return this.mapMessageToMessageResponse(replyMessage);
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessage(String messageId, Integer paging) throws NotFoundException {
        List<Message> messageList = this.messageRepository.getListRepliesMessageByMessageId(messageId, paging);
        List<MessageResponse> messageResponses = messageList.stream().map(this::mapMessageToMessageResponse).toList();

        Integer countAbove = this.messageRepository.countAboveMessagesReplies(messageId, messageList.get(messageList.size() - 1).getCreatedAt());
        Integer countBelow = this.messageRepository.countBelowMessagesReplies(messageId, messageList.get(0).getCreatedAt());
        int countOther = countAbove + countBelow - paging;
        CenteredMetadata centeredMetadata = new CenteredMetadata();
        centeredMetadata.setCountOther(countOther);
        centeredMetadata.setCountAbove(countAbove);
        centeredMetadata.setCountBelow(countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(centeredMetadata)
                .build();
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessageAboveOrBelow(String parentId, String messageId, Integer paging, boolean isAbove) throws NotFoundException {
        Message parentMessage = this.messageRepository.findByParentId(parentId);
        if(parentMessage == null){
            throw new NotFoundException(ConversationError.MESSAGE_NOT_FOUND);
        }
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(ConversationError.MESSAGE_NOT_FOUND));

        List<Message> messageList;

        if(isAbove) {
            messageList = this.messageRepository.getListRepliesMessageByMessageIdAndAboveTime(messageId, message.getCreatedAt(), paging);
        } else {
            messageList = this.messageRepository.getListRepliesMessageByMessageIdAndAboveTime(messageId, message.getCreatedAt(), paging);
        }

        List<MessageResponse> messageResponses = messageList.stream().map(this::mapMessageToMessageResponse).toList();

        Integer countAbove = this.messageRepository.countAboveMessagesReplies(messageId, messageList.get(messageList.size() - 1).getCreatedAt());
        Integer countBelow = this.messageRepository.countBelowMessagesReplies(messageId, messageList.get(0).getCreatedAt());
        int countOther = countAbove + countBelow - paging;
        CenteredMetadata centeredMetadata = new CenteredMetadata();
        centeredMetadata.setCountOther(countOther);
        centeredMetadata.setCountAbove(countAbove);
        centeredMetadata.setCountBelow(countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(centeredMetadata)
                .build();
    }

    @Override
    public String toggleReactMessage(String messageId, String iconId) throws NotFoundException {
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        Icon icon = this.iconRepository.findById(iconId).orElseThrow(() -> new NotFoundException(ConversationError.ICON_NOT_FOUND));
        MessageReaction messageReaction = this.messageReactionRepository.getReactionByMessageIdAndIconId(message.getId(), icon.getId());
        String exchangeName = message.getConversation().getType().compareTo(CONVERSATION_TYPE.PRIVATE) > 0 ? RabbitMQSchema.PRIVATE_CHAT_EXCHANGE : RabbitMQSchema.GROUP_CHAT_EXCHANGE;
        if(messageReaction != null) {
            this.messageReactionRepository.delete(messageReaction);
            rabbitTemplate.convertAndSend(
                    exchangeName,
                    RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()),
                    buildRabbitRequest(RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()), this.mapMessageToMessageResponse(message), PushNotificationAction.UNREACT_MESSAGE)
            );
            // push queue to remove notification for sender of the message you unreact to
        } else {
            rabbitTemplate.convertAndSend(
                    exchangeName,
                    RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()),
                    buildRabbitRequest(RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()), this.mapMessageToMessageResponse(message), PushNotificationAction.REACT_MESSAGE)
            );
            // push queue to save notification for sender of the message you react to
            messageReaction = MessageReaction.builder()
                    .message(message)
                    .icon(icon)
                    .build();
            this.messageReactionRepository.save(messageReaction);
        }

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<MessageResponse> getPinnedMessagesOfConversation(String conversationId) throws NotFoundException {
        this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        List<Message> messageList = this.messageRepository.getListPinnedMessagesByConversationId(conversationId);
        return messageList.stream().map(this::mapMessageToMessageResponse).toList();
    }

    @Override
    public List<MessageResourceResponse> getListResourcesOfConversation(String conversationId) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        List<Resource> responses = this.resourceRepository.findByConversationId(conversationId);
        return responses.stream().map(r -> {
            return MessageResourceResponse.builder()
                    .resourceType(r.getType())
                    .id(r.getId())
                    .name(r.getName())
                    .url(r.getLink())
                    .build();
        }).toList();
    }

    @Override
    public List<MessageResourceResponse> getListResourcesOfConversationAndType(String conversationId, RESOURCE_TYPE type) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        List<Resource> responses = this.resourceRepository.findByConversationIdAndType(conversationId, type);
        return responses.stream().map(r -> {
            return MessageResourceResponse.builder()
                    .resourceType(r.getType())
                    .id(r.getId())
                    .name(r.getName())
                    .url(r.getLink())
                    .build();
        }).toList();
    }
}
