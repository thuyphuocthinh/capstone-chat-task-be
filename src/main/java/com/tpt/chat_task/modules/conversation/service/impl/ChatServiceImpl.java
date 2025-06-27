package com.tpt.chat_task.modules.conversation.service.impl;

import com.tpt.chat_task.common.dto.SuccessResponseWithCenteredMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementContentRequest;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementRequest;
import com.tpt.chat_task.modules.conversation.dto.request.MessageElementSectionRequest;
import com.tpt.chat_task.modules.conversation.dto.request.MessageRequest;
import com.tpt.chat_task.modules.conversation.dto.response.*;
import com.tpt.chat_task.modules.conversation.entity.*;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import com.tpt.chat_task.modules.conversation.repository.*;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import com.tpt.chat_task.modules.conversation.service.IconService;
import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.resource.service.ResourceService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
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

    @Override
    @Transactional
    public MessageResponse addNewMessage(String token, String conversationId, MessageRequest request) throws NotFoundException, IOException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User sender = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        List<MessageElementRequest> elements = request.getElements();
        List<MultipartFile> files = request.getFiles();
        Message message = new Message();

        // 1. Neu co files => tao ham upload file => tao resource service
        if(files != null && !files.isEmpty()) {
            List<Resource> resources = resourceService.uploadMultipleFiles(files);
            message.setResources(resources);
        }

        // 2. Otherwise,
        /*
            *  -  Viet ham build TEXT_LIST (nho validation), tra ve MessageElement
            *  -  Viet ham build TEXT_SECTION (nho validation), tra ve MessageElement
        * */
        if(elements.isEmpty()){
            throw new BadRequestException(ConversationError.INVALID_REQUEST_CREATE_MESSAGE);
        }

        List<MessageElement> messageElements = this.buildMessageElements(elements);
        message.setMessageElements(messageElements);

        // 3. Thuc hien luu message
        message.setUser(sender);
        message = this.messageRepository.save(message);

        // 4. Viet ham map sang message response

        // 5.  Viet hàm get user id => bắn thông báo vào queue
        // 5.1 Nhắn tin vào channel thông thường thì member sẽ được nhận thông báo alert
        // 5.2 Những member được mention sẽ nhận thêm một thông báo quả chuông
        // 5.3 Nếu mention toàn channel thì các member sẽ nhận thông báo quả chuông
        // TODO: Create a notification exchange

        return this.mapMessageToMessageResponse(message);
    }

    private MessageResponse mapMessageToMessageResponse(Message message){
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

            }
        }

        return result;
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
                    parent.getElements().add(text);
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

        // TODO: ban realtime

        return this.mapMessageToMessageResponse(message);
    }

    @Override
    public String deleteMessage(String conversationId, String messageId) throws NotFoundException {
        this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(conversationId));
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        this.messageRepository.deleteById(message.getId());

        // TODO: ban realtime

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
    public SuccessResponseWithCenteredMetadata<?> getListOfMessages(String conversationId, Integer paging, boolean isAbove) throws NotFoundException {
        return null;
    }

    @Override
    public MessageResponse replyMessage(String token, String messageId, MessageRequest request) throws NotFoundException, IOException {
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));

        String userId = this.jwtProvider.getIdFromToken(token);
        User sender = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        List<MessageElementRequest> elements = request.getElements();
        List<MultipartFile> files = request.getFiles();
        Message replyMessage = new Message();

        // 1. Neu co files => tao ham upload file => tao resource service
        if(files != null && !files.isEmpty()) {
            List<Resource> resources = resourceService.uploadMultipleFiles(files);
            message.setResources(resources);
        }

        // 2. Otherwise,
        /*
         *  -  Viet ham build TEXT_LIST (nho validation), tra ve MessageElement
         *  -  Viet ham build TEXT_SECTION (nho validation), tra ve MessageElement
         * */
        if(elements.isEmpty()){
            throw new BadRequestException(ConversationError.INVALID_REQUEST_CREATE_MESSAGE);
        }

        List<MessageElement> messageElements = this.buildMessageElements(elements);
        message.setMessageElements(messageElements);

        // 3. Thuc hien luu message
        replyMessage.setUser(sender);
        replyMessage.setParentId(message.getId());
        replyMessage = this.messageRepository.save(message);

        // TODO: BAN REALTIME

        return this.mapMessageToMessageResponse(replyMessage);
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getListRepliesOfMessage(String messageId, Integer paging) throws NotFoundException {
        return null;
    }

    @Override
    public String toggleReactMessage(String messageId, String iconId) throws NotFoundException {
        Message message = this.messageRepository.findById(messageId).orElseThrow(() -> new NotFoundException(messageId));
        Icon icon = this.iconRepository.findById(iconId).orElseThrow(() -> new NotFoundException(ConversationError.ICON_NOT_FOUND));
        MessageReaction messageReaction = this.messageReactionRepository.getReactionByMessageIdAndIconId(message.getId(), icon.getId());

        if(messageReaction != null) {
            this.messageReactionRepository.delete(messageReaction);
        } else {
            // TODO: ban realtime
            messageReaction = MessageReaction.builder()
                    .message(message)
                    .icon(icon)
                    .build();
            this.messageReactionRepository.save(messageReaction);
        }

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public SuccessResponseWithCenteredMetadata<?> getPinnedMessages(String messageId) throws NotFoundException {
        return null;
    }
}
