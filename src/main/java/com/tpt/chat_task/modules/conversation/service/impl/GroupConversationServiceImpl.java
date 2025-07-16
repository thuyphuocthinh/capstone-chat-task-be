package com.tpt.chat_task.modules.conversation.service.impl;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.conversation.ConversationMemberRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.EXCHANGE_TYPE;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.dto.request.CreateGroupConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdateGroupConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.response.ConversationMemberResponse;
import com.tpt.chat_task.modules.conversation.dto.response.GroupConversationDetailResponse;
import com.tpt.chat_task.modules.conversation.dto.response.UnreadCountDTO;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.entity.Message;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_MEMBER_ROLE;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.repository.ConversationRepository;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import com.tpt.chat_task.modules.conversation.service.GroupConversationService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_ROLE;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.enums.WORKSPACE_USER_ROLE;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import com.tpt.chat_task.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupConversationServiceImpl implements GroupConversationService {
    private final ConversationRepository conversationRepository;

    private final WorkspaceRepository workspaceRepository;

    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;

    private final WorkspaceService workspaceService;

    private final RabbitTemplate rabbitTemplate;

    private final ChatService chatService;

    private WorkspaceMemberResponse findHost() {
        User host = this.userRepository.findByRole(USER_ROLE.ADMIN);
        if(host == null) {
            throw new NotFoundException(WorkspaceError.ADMIN_NOT_FOUND);
        }

        return WorkspaceMemberResponse.builder()
                .email(host.getEmail())
                .firstName(host.getFirstName())
                .lastName(host.getLastName())
                .roleInWorkspace(WORKSPACE_USER_ROLE.HOST.toString())
                .avatar(host.getAvatar())
                .id(host.getId())
                .build();
    }

    @Override
    public GroupConversationDetailResponse createNewGroupConversation(String workspaceId, CreateGroupConversationRequest request) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));

        User host = this.userRepository.findByRole(USER_ROLE.ADMIN);
        if (host == null) {
            throw new NotFoundException(WorkspaceError.ADMIN_NOT_FOUND);
        }

        List<User> users = new ArrayList<>();
        users.add(host);

        Conversation conversation = Conversation.builder()
                .workspace(workspace)
                .name(request.getName())
                .type(CONVERSATION_TYPE.GROUP)
                .isPinned(false)
                .users(users)
                .workspace(workspace)
                .build();

        try {
            conversation = conversationRepository.save(conversation);
        } catch (Exception e) {
            log.error("Failed to save conversation: {}", e.getMessage(), e);
            throw e;
        }

        String conversationAddMemberExchange = RabbitMQSchema.CONVERSATION_ADD_MEMBER_EXCHANGE;
        String conversationAddMemberRoutingKey = RabbitMQSchema.CONVERSATION_ADD_MEMBER_ROUTING_KEY;
        this.rabbitTemplate.convertAndSend(
                conversationAddMemberExchange,
                conversationAddMemberRoutingKey,
                RabbitMQRequest.builder()
                        .routingKey(conversationAddMemberRoutingKey)
                        .exchangeType(EXCHANGE_TYPE.DIRECT)
                        .payload(ConversationMemberRequest.builder()
                                .conversationId(conversation.getId())
                                .userId(host.getId())
                                .type(conversation.getType())
                                .build()
                        )
                        .userId(host.getId())
                        .build()
        );

        return GroupConversationDetailResponse.builder()
                .id(conversation.getId())
                .isPinned(conversation.isPinned())
                .type(conversation.getType().name())
                .name(conversation.getName())
                .build();
    }

    @Override
    public GroupConversationDetailResponse getGroupConversationDetail(String workspaceId, String conversationId) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        Message latestMessage = this.conversationRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId);

        return GroupConversationDetailResponse.builder()
                .id(conversation.getId())
                .isPinned(conversation.isPinned())
                .type(conversation.getType().name())
                .message(this.chatService.mapMessageToMessageResponse(latestMessage))
                .name(conversation.getName())
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> getListGroupConversations(String workspaceId, String token, Integer page, Integer paging) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Conversation> conversationPage = this.conversationRepository.findConversationsByUserIdAndType(userId, CONVERSATION_TYPE.GROUP, pageable);
        List<Conversation> conversations = conversationPage.getContent();

        List<String> conversationIds = conversations.stream().map(Conversation::getId).collect(Collectors.toList());
        List<Message> latestMessages = conversationRepository.findListOfLatestMessagesByConversationIds(conversationIds);
        List<UnreadCountDTO> unreadCounts = conversationRepository.countUnreadMessagesForConversations(conversationIds, userId);

        // Convert to map for O(1) lookup
        Map<String, Message> latestMessageMap = latestMessages.stream()
                .collect(Collectors.toMap(m -> m.getConversation().getId(), Function.identity()));

        Map<String, Integer> unreadCountMap = unreadCounts.stream()
                .collect(Collectors.toMap(UnreadCountDTO::getConversationId, UnreadCountDTO::getUnreadCount));


        List<GroupConversationDetailResponse> conversationDetailResponseList = conversations.stream().map(conversation -> {
            Message latestMessage = latestMessageMap.get(conversation.getId());
            Integer countUnread = unreadCountMap.getOrDefault(conversation.getId(), 0);
            return GroupConversationDetailResponse.builder()
                    .id(conversation.getId())
                    .isPinned(conversation.isPinned())
                    .type(conversation.getType().name())
                    .message(latestMessage != null ? this.chatService.mapMessageToMessageResponse(latestMessage) : null)
                    .countUnread(countUnread)
                    .name(conversation.getName())
                    .build();
        }).toList();

        Metadata metadata = Metadata.builder()
                .currentPage(conversationPage.getNumber() + 1)
                .totalPages(conversationPage.getTotalPages())
                .totalElements((int) conversationPage.getTotalElements())
                .pageSize(conversationPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(conversationDetailResponseList)
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> searchListGroupConversations(String workspaceId, String token, String name, Integer page, Integer paging) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Conversation> conversationPage = this.conversationRepository.searchByUserIdAndName(userId, name, CONVERSATION_TYPE.GROUP, pageable);
        List<Conversation> conversations = conversationPage.getContent();

        List<String> conversationIds = conversations.stream().map(Conversation::getId).collect(Collectors.toList());
        List<Message> latestMessages = conversationRepository.findListOfLatestMessagesByConversationIds(conversationIds);
        List<UnreadCountDTO> unreadCounts = conversationRepository.countUnreadMessagesForConversations(conversationIds, userId);

        // Convert to map for O(1) lookup
        Map<String, Message> latestMessageMap = latestMessages.stream()
                .collect(Collectors.toMap(m -> m.getConversation().getId(), Function.identity()));

        Map<String, Integer> unreadCountMap = unreadCounts.stream()
                .collect(Collectors.toMap(UnreadCountDTO::getConversationId, UnreadCountDTO::getUnreadCount));


        List<GroupConversationDetailResponse> conversationDetailResponseList = conversations.stream().map(conversation -> {
            Message latestMessage = latestMessageMap.get(conversation.getId());
            Integer countUnread = unreadCountMap.getOrDefault(conversation.getId(), 0);
            return GroupConversationDetailResponse.builder()
                    .id(conversation.getId())
                    .isPinned(conversation.isPinned())
                    .type(conversation.getType().name())
                    .message(latestMessage != null ? this.chatService.mapMessageToMessageResponse(latestMessage) : null)
                    .countUnread(countUnread)
                    .name(conversation.getName())
                    .build();
        }).toList();

        Metadata metadata = Metadata.builder()
                .currentPage(conversationPage.getNumber() + 1)
                .totalPages(conversationPage.getTotalPages())
                .totalElements((int) conversationPage.getTotalElements())
                .pageSize(conversationPage.getSize())
                .build();


        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(conversationDetailResponseList)
                .build();
    }

    @Override
    public GroupConversationDetailResponse updateGroupConversation(String workspaceId, String id, UpdateGroupConversationRequest request) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        conversation.setName(request.getName());
        this.conversationRepository.save(conversation);

        return GroupConversationDetailResponse.builder()
                .id(conversation.getId())
                .isPinned(conversation.isPinned())
                .type(conversation.getType().name())
                .name(conversation.getName())
                .build();
    }

    @Override
    public String deleteGroupConversation(String workspaceId, String id) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        this.conversationRepository.delete(conversation);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String togglePinnedConversation(String workspaceId, String id) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        conversation.setPinned(!conversation.isPinned());
        this.conversationRepository.save(conversation);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String addMemberToGroupConversation(String workspaceId, String id, String userId) throws NotFoundException, BadRequestException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        boolean checkUserExistInWorkspace = this.workspaceService.isMemberOfWorkspace(workspaceId, userId);
        if(!checkUserExistInWorkspace) {
            throw new BadRequestException(WorkspaceError.USER_NOT_IN_WORKSPACE);
        }
        if(this.conversationRepository.existsConversationByConversationIdAndUserId(id, userId)) {
            throw new BadRequestException(ConversationError.USER_ALREADY_IN_CONVERSATION);
        }
        conversation.getUsers().add(user);
        this.conversationRepository.save(conversation);
        String conversationAddMemberExchange = RabbitMQSchema.CONVERSATION_ADD_MEMBER_EXCHANGE;
        String conversationAddMemberRoutingKey = RabbitMQSchema.CONVERSATION_ADD_MEMBER_ROUTING_KEY;
        this.rabbitTemplate.convertAndSend(
                conversationAddMemberExchange,
                conversationAddMemberRoutingKey,
                RabbitMQRequest.builder()
                        .routingKey(conversationAddMemberRoutingKey)
                        .exchangeType(EXCHANGE_TYPE.DIRECT)
                        .payload(ConversationMemberRequest.builder().conversationId(id).userId(userId).type(conversation.getType()).build())
                        .userId(userId)
                        .build()
        );
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String removeMemberFromGroupConversation(String workspaceId, String id, String userId) throws NotFoundException, BadRequestException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        boolean checkUserExistInWorkspace = this.workspaceService.isMemberOfWorkspace(workspaceId, userId);
        if(!checkUserExistInWorkspace) {
            throw new BadRequestException(WorkspaceError.USER_NOT_IN_WORKSPACE);
        }
        if(!this.conversationRepository.existsConversationByConversationIdAndUserId(id, userId)) {
            throw new BadRequestException(ConversationError.USER_NOT_IN_CONVERSATION);
        }
        conversation.getUsers().remove(user);
        this.conversationRepository.save(conversation);
        String conversationDeleteMemberExchange = RabbitMQSchema.CONVERSATION_DELETE_MEMBER_EXCHANGE;
        String conversationDeleteMemberRoutingKey = RabbitMQSchema.CONVERSATION_DELETE_MEMBER_ROUTING_KEY;
        this.rabbitTemplate.convertAndSend(
                conversationDeleteMemberExchange,
                conversationDeleteMemberRoutingKey,
                RabbitMQRequest.builder()
                        .routingKey(conversationDeleteMemberRoutingKey)
                        .exchangeType(EXCHANGE_TYPE.DIRECT)
                        .payload(ConversationMemberRequest.builder().conversationId(id).userId(userId).type(conversation.getType()).build())
                        .userId(userId)
                        .build()
        );
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<ConversationMemberResponse> getConversationMembers(String workspaceId, String conversationId) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        List<User> users = conversation.getUsers();
        return users.stream().map(user -> {
            return ConversationMemberResponse.builder()
                    .id(user.getId())
                    .avatar(user.getAvatar())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roleInConversation(user.getRole().equals(USER_ROLE.ADMIN) ? CONVERSATION_MEMBER_ROLE.ADMIN.name() : CONVERSATION_MEMBER_ROLE.MEMBER.name())
                    .build();
        }).toList();
    }
}
