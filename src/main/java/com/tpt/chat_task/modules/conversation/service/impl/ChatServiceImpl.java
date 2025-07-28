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
import com.tpt.chat_task.modules.notification.constant.NotificationConstant;
import com.tpt.chat_task.modules.notification.constant.NotificationError;
import com.tpt.chat_task.modules.notification.entity.Notification;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.repository.NotificationRepository;
import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import com.tpt.chat_task.modules.resource.repository.ResourceRepository;
import com.tpt.chat_task.modules.resource.service.ResourceService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Stream;


/*
    Theo thứ tự trên xuống dưới
* 1. Tối ưu query, index, entity => dễ làm, tác động lớn
  2. Tách logic nặng qua thread riêng / async => tránh block
  3. Caching kết quả / dữ liệu => giảm tải DB
  4. Dùng load balancer khi scale nhiều user
  5. Tối ưu infra: CDN, pub/sub, thread pool, queue
* */

@Service
@RequiredArgsConstructor
@Slf4j
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

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public MessageResponse addNewMessage(String token, String conversationId, MessageRequest request) throws NotFoundException, IOException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User sender = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        List<MultipartFile> files = request.getFiles();
        Message message = buildAndSaveMessage(request, sender, files, conversation);
        MessageResponse response = this.mapMessageToMessageResponse(message);
        pushToQueueAsyncSendMessageAction(request, conversationId, response, PushNotificationAction.SEND_MESSAGE);
        return response;
    }

    private List<MessageSeen> buildMessageSeen(Message message, User sender, List<User> userList) {
        return Stream.concat(
                Stream.of(MessageSeen.builder()
                        .id(new MessageUserId(sender.getId(), message.getId()))
                        .user(sender)
                        .message(message)
                        .isSeen(true)
                        .build()
                ),
                userList.stream()
                        .filter(user -> !user.getId().equals(sender.getId()))
                        .map(user -> MessageSeen.builder()
                                .id(new MessageUserId(user.getId(), message.getId()))
                                .user(user)
                                .message(message)
                                .isSeen(false)
                                .build())
        ).toList();
    }

    private Message buildAndSaveMessage(MessageRequest request, User sender, List<MultipartFile> files, Conversation conversation) throws IOException {
        Message message = new Message();
        if (files != null && !files.isEmpty()) {
            List<Resource> resources = resourceService.uploadMultipleFiles(files);
            message.setResources(resources);
        }

        if (request.getElements().isEmpty()) {
            throw new BadRequestException(ConversationError.INVALID_REQUEST_CREATE_MESSAGE);
        }

        message.setConversation(conversation);
        message.setUser(sender);

        List<MessageSeen> messageSeens = this.buildMessageSeen(message, sender, conversation.getUsers());
        message.setMessageSeen(messageSeens);

        List<MessageElement> messageElements = this.buildMessageElements(request.getElements(), message);
        message.setMessageElements(messageElements);

        return messageRepository.save(message);
    }

    private void pushToQueueAsyncSendMessageAction(MessageRequest request, String conversationId, MessageResponse response, String action) {
        Conversation conversation = this.conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        String exchangeName = conversation.getType() == CONVERSATION_TYPE.PRIVATE
                ? RabbitMQSchema.PRIVATE_CHAT_EXCHANGE
                : RabbitMQSchema.GROUP_CHAT_EXCHANGE;

        executorService.submit(() -> {
            try {
                if (checkMentionAll(request.getElements())) {
                    try {
                        String routingKey = conversation.getType() == CONVERSATION_TYPE.PRIVATE ?
                                RabbitMQSchema.getPrivateChatRoutingKey(conversationId) :
                                RabbitMQSchema.getGroupChatRoutingKey(conversationId);
                        RabbitMQRequest payload = buildRabbitRequest(conversationId, response, action, PUSH_NOTIFICATION_TYPE.MESSAGE);
                        rabbitTemplate.convertAndSend(exchangeName, routingKey, payload);
                        log.info("Sent '@all' message to [{}] with routingKey [{}]", exchangeName, routingKey);
                        List<String> allUserIds = this.extractUserIdsFromConversation(conversation);
                        this.pushToNotificationQueueAndSend(allUserIds, response, NOTIFICATION_TYPE.MENTION);
                    } catch (Exception e) {
                        log.error("Error sending '@all' message to RabbitMQ", e);
                    }

                } else if (!this.extractUserIds(request.getElements()).isEmpty()) {
                    List<String> userIds = extractUserIds(request.getElements());
                    for (String id : userIds) {
                        try {
                            String routingKey = conversation.getType() == CONVERSATION_TYPE.PRIVATE ?
                                    RabbitMQSchema.getPrivateChatMentionRoutingKey(conversationId, id) :
                                    RabbitMQSchema.getGroupChatMentionRoutingKey(conversationId, id);
                            RabbitMQRequest payload = buildRabbitRequest(routingKey, response, action, PUSH_NOTIFICATION_TYPE.MESSAGE);
                            rabbitTemplate.convertAndSend(exchangeName, routingKey, payload);
                            log.info("Sent mention message to [{}] with routingKey [{}] (userId: {})", exchangeName, routingKey, id);
                        } catch (Exception e) {
                            log.error("Error sending mention message for userId [{}]", id, e);
                        }
                    }
                    this.pushToNotificationQueueAndSend(userIds, response, NOTIFICATION_TYPE.MENTION);
                } else {
                    try {
                        String routingKey = conversation.getType() == CONVERSATION_TYPE.PRIVATE ?
                                RabbitMQSchema.getPrivateChatRoutingKey(conversationId) :
                                RabbitMQSchema.getGroupChatRoutingKey(conversationId);
                        Object payload = buildRabbitRequest(routingKey, response, action, PUSH_NOTIFICATION_TYPE.MESSAGE);
                        log.info("Sending message to conversation: {}", conversationId);
                        rabbitTemplate.convertAndSend(exchangeName, routingKey, payload);
                        log.info("Sent message to exchange [{}] with routingKey [{}]", exchangeName, routingKey);
                    } catch (Exception e) {
                        log.error("Error sending default message to RabbitMQ", e);
                    }
                }
            } catch (Exception e) {
                log.error("Error in pushToQueueAsyncSendMessageAction logic", e);
            }
        });
    }

    private void pushToNotificationQueueAndSend(List<String> allUserIds, MessageResponse response, NOTIFICATION_TYPE notificationType) {
        executorService.submit(() -> {
            try {
                RabbitMQRequest payload = buildRabbitRequest(RabbitMQSchema.NOTIFICATION_ROUTING_KEY, response, null, null);
                for (String userId : allUserIds) {
                    payload.setUserId(userId);
                    payload.setPushNotificationType(PUSH_NOTIFICATION_TYPE.NOTIFICATION);
                    payload.setPushNotificationAction(PushNotificationAction.NEW_NOTIFICATION);
                    payload.setNotificationTitle(NotificationConstant.NOTIFICATION_TITLE);
                    payload.setNotificationType(notificationType);
                    rabbitTemplate.convertAndSend(RabbitMQSchema.NOTIFICATION_EXCHANGE, RabbitMQSchema.NOTIFICATION_ROUTING_KEY, payload);
                }
                log.info("Sent message to notification queue");
            } catch (Exception e) {
                log.error("Error in pushToNotificationQueueAndSend logic", e);
            }
        });
    }

    private RabbitMQRequest buildRabbitRequest(String routingKey, MessageResponse response, String action, PUSH_NOTIFICATION_TYPE pushNotificationType) {
        return RabbitMQRequest.builder()
                .exchangeType(EXCHANGE_TYPE.TOPIC)
                .routingKey(routingKey)
                .payload(response)
                .pushNotificationAction(action)
                .pushNotificationType(pushNotificationType)
                .build();
    }

    @Override
    public MessageResponse mapMessageToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getUser().getId());
        response.setPinned(message.isPinned());
        response.setConversationId(message.getConversation().getId());

        // count replies
        Integer countRepliesOfMessage = this.messageRepository.countRepliesOfMessage(message.getId());
        response.setCountReplies(countRepliesOfMessage);

        // get user reply ids
        if(countRepliesOfMessage > 0) {
            List<String> userIds = this.messageRepository.getUserRepliesId(message.getId());
            response.setUserReplyIds(userIds);
        }

        // Set files
        List<Resource> resources = message.getResources();
        response.setFiles(resources != null && !resources.isEmpty()
                        ? resources.stream().map(resource -> MessageResourceResponse.builder()
                        .id(resource.getId())
                        .url(resource.getLink())
                        .name(resource.getName())
                        .resourceType(resource.getType())
                        .createdAt(resource.getCreatedAt())
                        .build()
                ).toList()
                        : Collections.emptyList()
        );

        // Set reactions
        List<MessageReactResponse> reactions = iconService.getReactionsByMessageId(message.getId());
        response.setReactions(reactions != null ? reactions : Collections.emptyList());

        // Set elements
        List<MessageElement> elements = message.getMessageElements();
        response.setElements(elements != null ? this.mapMessageElementsToResponse(elements) : Collections.emptyList());
        response.setRead(false);
        response.setCreatedAt(message.getCreatedAt());

        return response;
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

    private List<String> extractUserIdsFromConversation(Conversation conversation) {
        return conversation.getUsers().stream().map(User::getId).collect(Collectors.toList());
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

    private List<MessageElement> buildMessageElements(List<MessageElementRequest> requestElements, Message message) {
        List<MessageElement> result = new ArrayList<>();
        int orderIndex = 0;

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
                listElement.setMessage(message);
                listElement.setOrderIndex(orderIndex++);
                result.add(listElement);

                for (MessageElementSectionRequest sectionReq : elementReq.getElements()) {
                    // TEXT_SECTION
                    String sectionId = UUID.randomUUID().toString();
                    MessageElement sectionElement = new MessageElement();
                    sectionElement.setId(sectionId);
                    sectionElement.setParentId(textListId);
                    sectionElement.setType(sectionReq.getType());
                    sectionElement.setIndent(0);
                    sectionElement.setMessage(message);
                    sectionElement.setOrderIndex(orderIndex++);
                    result.add(sectionElement);

                    for (MessageElementContentRequest contentReq : sectionReq.getElements()) {
                        String contentId = UUID.randomUUID().toString();
                        // TEXT / EMOJI / USER
                        MessageElement contentElement = new MessageElement();
                        contentElement.setId(contentId);
                        contentElement.setParentId(sectionId);
                        contentElement.setType(contentReq.getType());
                        contentElement.setContent(contentReq.getContent());
                        log.info("Content text-list direct: {}", contentReq.getContent());
                        contentElement.setBold(contentReq.isBold());
                        contentElement.setItalic(contentReq.isItalic());
                        contentElement.setUnderline(contentReq.isUnderline());
                        contentElement.setMessage(message);
                        contentElement.setOrderIndex(orderIndex++);
                        result.add(contentElement);
                    }
                }
            } else if (elementReq.getType() == MESSAGE_ELEMENT_TYPE.TEXT_SECTION) {
                // TEXT_SECTION trực tiếp
                String sectionId = UUID.randomUUID().toString();
                MessageElement sectionElement = new MessageElement();
                sectionElement.setId(sectionId);
                sectionElement.setParentId(null);
                sectionElement.setType(elementReq.getType());
                sectionElement.setIndent(elementReq.getIndent());
                sectionElement.setMessage(message);
                sectionElement.setOrderIndex(orderIndex++);
                result.add(sectionElement);

                for (MessageElementSectionRequest sectionReq : elementReq.getElements()) {
                    // TEXT_SECTION (cha của TEXT/EMOJI/USER)
                    String contentId = UUID.randomUUID().toString();
                    MessageElementContentRequest contentReq = new MessageElementContentRequest();
                    contentReq.setContent(sectionReq.getContent());
                    log.info("Content text-section direct: {}", contentReq.getContent());
                    contentReq.setType(MESSAGE_ELEMENT_TYPE.TEXT);
                    contentReq.setBold(contentReq.isBold());
                    contentReq.setItalic(contentReq.isItalic());
                    contentReq.setUnderline(contentReq.isUnderline());
                    MessageElement contentElement = getMessageElement(contentReq, contentId, sectionId);
                    contentElement.setMessage(message);
                    contentElement.setOrderIndex(orderIndex++);
                    result.add(contentElement);
                }
            }
        }

        for (MessageElement el : result) {
            log.info("Saving: id={}, orderIndex={}, content={}, type={}", el.getId(), el.getOrderIndex(), el.getContent(), el.getType());
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
        messageElements.sort(Comparator.comparingInt(MessageElement::getOrderIndex));

        Map<String, MessageElementResponse> mapIdToResponse = new LinkedHashMap<>();
        List<MessageElementResponse> result = new ArrayList<>();

        for (MessageElement element : messageElements) {
            MESSAGE_ELEMENT_TYPE type = element.getType();
            String parentId = element.getParentId();

            switch (type) {
                case TEXT_LIST -> {
                    MessageElementResponse textList = new MessageElementResponse();
                    textList.setType(MESSAGE_ELEMENT_TYPE.TEXT_LIST);
                    textList.setStyle(element.getStyle());
                    textList.setIndent(element.getIndent());
                    textList.setElements(new ArrayList<>());
                    mapIdToResponse.put(element.getId(), textList);
                    result.add(textList);
                }

                case TEXT_SECTION -> {
                    MessageElementSectionResponse section = new MessageElementSectionResponse();
                    section.setType(MESSAGE_ELEMENT_TYPE.TEXT_SECTION);
                    section.setStyle(element.getStyle());
                    section.setIndent(element.getIndent());
                    section.setContentElements(new ArrayList<>());

                    if (parentId == null) {
                        mapIdToResponse.put(element.getId(), section);
                        result.add(section);
                    } else {
                        MessageElementResponse parent = mapIdToResponse.get(parentId);
                        if (parent != null && parent.getElements() != null) {
                            parent.getElements().add(section);
                        }
                        mapIdToResponse.put(element.getId(), section);
                    }
                }

                case TEXT, EMOJI, USER -> {
                    MessageElementContentResponse content = getMessageElementContentResponse(element);
                    MessageElementResponse parent = mapIdToResponse.get(parentId);
                    if (parent instanceof MessageElementSectionResponse section && section.getContentElements() != null) {
                        section.getContentElements().add(content);
                    }
                }
            }
        }

        return result;
    }

    private static MessageElementContentResponse getMessageElementContentResponse(MessageElement element) {
        MessageElementContentResponse text = new MessageElementContentResponse();
        text.setType(MESSAGE_ELEMENT_TYPE.TEXT);
        text.setContent(element.getContent());
        text.setStyle(element.getStyle());
        text.setIndent(element.getIndent());
        text.setBold(element.isBold());
        text.setItalic(element.isItalic());
        text.setUnderline(element.isUnderline());
        return text;
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
        Message message = this.messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(messageId));
        List<MessageElement> messageElements = this.buildMessageElements(request.getElements(), message);
        message.getMessageElements().clear();             // Hibernate sẽ xóa cũ
        message.getMessageElements().addAll(messageElements); // Thêm mới
        this.messageRepository.save(message);
        MessageResponse response = this.mapMessageToMessageResponse(message);
        this.pushToQueueAsyncSendMessageAction(request, conversationId, response, PushNotificationAction.UPDATE_MESSAGE);
        return response;
    }

    @Transactional
    @Override
    public String deleteMessage(String conversationId, String messageId) throws NotFoundException {
        log.info("Deleting message: {}", messageId);

        this.conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        Message message = this.messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ConversationError.MESSAGE_NOT_FOUND));

        MessageResponse response = this.mapMessageToMessageResponse(message);
        CONVERSATION_TYPE conversationType = message.getConversation().getType();

        this.messageRepository.forceDelete(messageId);
        log.info("After delete: {}", messageRepository.existsById(messageId));

        String exchangeName = conversationType.compareTo(CONVERSATION_TYPE.PRIVATE) > 0
                ? RabbitMQSchema.PRIVATE_CHAT_EXCHANGE
                : RabbitMQSchema.GROUP_CHAT_EXCHANGE;
        rabbitTemplate.convertAndSend(
                exchangeName,
                RabbitMQSchema.getGroupChatRoutingKey(conversationId),
                buildRabbitRequest(
                        RabbitMQSchema.getGroupChatAllRoutingKey(conversationId),
                        response,
                        PushNotificationAction.DELETE_MESSAGE,
                        PUSH_NOTIFICATION_TYPE.MESSAGE
                )
        );
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    @Transactional
    public String togglePinMessage(String conversationId, String messageId) throws NotFoundException {
        this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(ConversationError.MESSAGE_NOT_FOUND));
        message.setPinned(!message.isPinned());
        this.messageRepository.save(message);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListOfMessages(String conversationId, Integer paging) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        List<Message> messageList = this.messageRepository.getListMessagesByConversationId(conversationId, paging);
        List<MessageResponse> messageResponses = messageList.stream()
                .map(this::mapMessageToMessageResponse)
                .toList();

        Integer countAbove = 0;
        Integer countBelow = 0;

        if (!messageList.isEmpty()) {
            countAbove = this.messageRepository.countAboveMessages(conversationId, messageList.get(messageList.size() - 1).getCreatedAt());
            countBelow = this.messageRepository.countBelowMessages(conversationId, messageList.get(0).getCreatedAt());
        }

        CenteredMetadata centeredMetadata = buildCenteredMetadata(countAbove, countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(centeredMetadata)
                .build();
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListOfMessagesAboveOrBelow(String conversationId, String messageId, Integer paging, boolean isAbove) throws NotFoundException {
        this.conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(conversationId));

        Message message = this.messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(messageId));

        List<Message> messageList;
        if (isAbove) {
            messageList = this.messageRepository.getListMessagesByConversationIdAndAboveTime(conversationId, message.getCreatedAt(), paging);
        } else {
            messageList = this.messageRepository.getListMessagesByConversationIdAndBelowTime(conversationId, message.getCreatedAt(), paging);
        }

        List<MessageResponse> messageResponses = messageList.stream()
                .map(this::mapMessageToMessageResponse)
                .toList();

        Integer countAbove = this.messageRepository.countAboveMessages(conversationId, message.getCreatedAt());
        Integer countBelow = this.messageRepository.countBelowMessages(conversationId, message.getCreatedAt());

        CenteredMetadata centeredMetadata = buildCenteredMetadata(countAbove, countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(centeredMetadata)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse replyMessage(String token, String messageId, MessageRequest request) throws NotFoundException, IOException {
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(ConversationError.MESSAGE_NOT_FOUND));
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
        List<MessageElement> messageElements = this.buildMessageElements(elements, replyMessage);
        replyMessage.setMessageElements(messageElements);
        replyMessage.setUser(sender);
        replyMessage.setParentId(message.getId());
        replyMessage.setConversation(message.getConversation());
        if(!message.isThreadRoot()) {
            message.setThreadRoot(true);
            this.messageRepository.save(message);
        }
        replyMessage = this.messageRepository.save(replyMessage);
        MessageResponse response = this.mapMessageToMessageResponse(message);
        this.pushToQueueAsyncSendMessageAction(request, message.getConversation().getId(), response, PushNotificationAction.SEND_MESSAGE);
        return this.mapMessageToMessageResponse(replyMessage);
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessage(String messageId, Integer paging) throws NotFoundException {
        List<Message> messageList = this.messageRepository.getListRepliesMessageByMessageId(messageId, paging);
        List<MessageResponse> messageResponses = messageList.stream()
                .map(this::mapMessageToMessageResponse)
                .toList();

        int countAbove = 0;
        int countBelow = 0;

        if (!messageList.isEmpty()) {
            countAbove = this.messageRepository.countAboveMessagesReplies(
                    messageId, messageList.get(messageList.size() - 1).getCreatedAt()
            );
            countBelow = this.messageRepository.countBelowMessagesReplies(
                    messageId, messageList.get(0).getCreatedAt()
            );
        }


        CenteredMetadata centeredMetadata = buildCenteredMetadata(countAbove, countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(centeredMetadata)
                .build();
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessageAboveOrBelow(
            String parentId,
            String messageId,
            Integer paging,
            boolean isAbove
    ) throws NotFoundException {
        Message centerMessage = this.messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ConversationError.MESSAGE_NOT_FOUND));

        List<Message> messageList = isAbove
                ? this.messageRepository.getListRepliesMessageByMessageIdAndAboveTime(
                parentId,
                centerMessage.getCreatedAt(),
                paging
        )
                : this.messageRepository.getListRepliesMessageByMessageIdAndBelowTime(
                parentId,
                centerMessage.getCreatedAt(),
                paging
        );

        if (messageList.isEmpty()) {
            return SuccessResponseWithCenteredMetadata.builder()
                    .data(Collections.emptyList())
                    .status(RESPONSE_STATUS.SUCCESS.toString())
                    .metadata(new CenteredMetadata(0, 0, 0))
                    .build();
        }

        List<MessageResponse> messageResponses = messageList.stream()
                .map(this::mapMessageToMessageResponse)
                .toList();

        int countAbove = this.messageRepository.countAboveMessagesReplies(
                parentId, centerMessage.getCreatedAt());
        int countBelow = this.messageRepository.countBelowMessagesReplies(
                parentId, centerMessage.getCreatedAt());

        CenteredMetadata metadata = buildCenteredMetadata(countAbove, countBelow);

        return SuccessResponseWithCenteredMetadata.builder()
                .data(messageResponses)
                .status(RESPONSE_STATUS.SUCCESS.toString())
                .metadata(metadata)
                .build();
    }

    private CenteredMetadata buildCenteredMetadata(int countAbove, int countBelow) {
        CenteredMetadata metadata = new CenteredMetadata();
        metadata.setCountAbove(countAbove);
        metadata.setCountBelow(countBelow);
        metadata.setCountOther(countAbove + countBelow);
        return metadata;
    }

    @Override
    @Transactional
    public String toggleReactMessage(String token, String messageId, String iconId) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(ConversationError.MESSAGE_NOT_FOUND));
        Icon icon = this.iconRepository.findById(iconId).orElseThrow(() -> new NotFoundException(ConversationError.ICON_NOT_FOUND));
        MessageReaction messageReaction = this.messageReactionRepository.getReactionByMessageIdAndIconId(message.getId(), icon.getId());
        String exchangeName = message.getConversation().getType().compareTo(CONVERSATION_TYPE.PRIVATE) > 0 ? RabbitMQSchema.PRIVATE_CHAT_EXCHANGE : RabbitMQSchema.GROUP_CHAT_EXCHANGE;
        if(messageReaction != null) {
            this.messageReactionRepository.delete(messageReaction);
            // push realtime
            rabbitTemplate.convertAndSend(
                    exchangeName,
                    RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()),
                    buildRabbitRequest(RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()), this.mapMessageToMessageResponse(message), PushNotificationAction.UNREACT_MESSAGE, PUSH_NOTIFICATION_TYPE.MESSAGE)
            );
            // delete notification in db
            Notification notification = this.notificationRepository.findReactionNotification(
                    NOTIFICATION_TYPE.REACTION,
                    messageId,
                    message.getUser().getId()
            ).orElseThrow(() -> new NotFoundException(NotificationError.NOTIFICATION_NOT_FOUND));
            rabbitTemplate.convertAndSend(
                    RabbitMQSchema.NOTIFICATION_DELETE_EXCHANGE,
                    RabbitMQSchema.NOTIFICATION_DELETE_ROUTING_KEY,
                    notification.getId()
            );
        } else {
            MessageUserIconId id = new MessageUserIconId();
            id.setMessageId(message.getId());
            id.setUserId(user.getId());
            id.setIconId(icon.getId());
            messageReaction = MessageReaction.builder()
                    .id(id)
                    .message(message)
                    .icon(icon)
                    .user(user)
                    .build();
            this.messageReactionRepository.save(messageReaction);
            // push realtime
            rabbitTemplate.convertAndSend(
                    exchangeName,
                    RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()),
                    buildRabbitRequest(RabbitMQSchema.getGroupChatAllRoutingKey(message.getConversation().getId()), this.mapMessageToMessageResponse(message), PushNotificationAction.REACT_MESSAGE, PUSH_NOTIFICATION_TYPE.MESSAGE)
            );
            // push to notification queue to save db
            List<String> allUserIds = this.extractUserIdsFromConversation(message.getConversation());
            this.pushToNotificationQueueAndSend(List.of(message.getUser().getId()), this.mapMessageToMessageResponse(message), NOTIFICATION_TYPE.REACTION);
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
