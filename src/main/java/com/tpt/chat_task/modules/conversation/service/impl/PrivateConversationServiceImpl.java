package com.tpt.chat_task.modules.conversation.service.impl;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.dto.request.CreatePrivateConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.response.ConversationMemberResponse;
import com.tpt.chat_task.modules.conversation.dto.response.GroupConversationDetailResponse;
import com.tpt.chat_task.modules.conversation.dto.response.PrivateConversationDetailResponse;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.entity.Message;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_MEMBER_ROLE;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.repository.ConversationRepository;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import com.tpt.chat_task.modules.conversation.service.PrivateConversationService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import com.tpt.chat_task.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateConversationServiceImpl implements PrivateConversationService {
    private final ConversationRepository conversationRepository;

    private final UserRepository userRepository;

    private final WorkspaceRepository workspaceRepository;

    private final JwtProvider jwtProvider;

    private final WorkspaceService workspaceService;

    private final RabbitTemplate rabbitTemplate;

    private final ChatService chatService;

    private List<ConversationMemberResponse> convertUsersListToConversationMemberResponseList(List<User> users) {
        return users.stream().map(u -> {
            return ConversationMemberResponse.builder()
                    .id(u.getId())
                    .roleInConversation(CONVERSATION_MEMBER_ROLE.MEMBER.toString())
                    .avatar(u.getAvatar())
                    .email(u.getEmail())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .build();
        }).toList();
    }

    @Override
    public PrivateConversationDetailResponse createPrivateConversation(String workspaceId, CreatePrivateConversationRequest request) throws NotFoundException, BadRequestException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(workspaceId));

        List<String> userIds = request.getUserIds();
        List<User> users = new ArrayList<>();
        String conversationName = "";
        for (String userId : userIds) {
            User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
            boolean checkUserExistInWorkspace = this.workspaceService.isMemberOfWorkspace(workspaceId, userId);
            if(!checkUserExistInWorkspace) {
                throw new BadRequestException(WorkspaceError.USER_NOT_IN_WORKSPACE);
            }
            users.add(user);
            conversationName = conversationName.concat(user.getFirstName() + " " + user.getLastName() + " - ");
        }
        conversationName = conversationName.substring(0, conversationName.lastIndexOf(" - "));
        Conversation privateConversation = Conversation.builder()
                .workspace(workspace)
                .users(users)
                .type(CONVERSATION_TYPE.PRIVATE)
                .name(conversationName)
                .build();
        privateConversation = conversationRepository.save(privateConversation);

        String conversationAddMemberExchange = RabbitMQSchema.CONVERSATION_ADD_MEMBER_EXCHANGE;
        String conversationAddMemberRoutingKey = RabbitMQSchema.CONVERSATION_ADD_MEMBER_ROUTING_KEY;
        this.rabbitTemplate.convertAndSend(
                conversationAddMemberExchange,
                conversationAddMemberRoutingKey,
                privateConversation.getUsers().stream().map(User::getId).toList()
        );

        return PrivateConversationDetailResponse.builder()
                .id(privateConversation.getId())
                .name(privateConversation.getName())
                .isPinned(privateConversation.isPinned())
                .members(this.convertUsersListToConversationMemberResponseList(users))
                .type(CONVERSATION_TYPE.PRIVATE.toString())
                .build();
    }

    @Override
    public String deletePrivateConversation(String workspaceId, String conversationId) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation privateConversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        this.conversationRepository.delete(privateConversation);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public PrivateConversationDetailResponse getPrivateConversationDetail(String workspaceId, String conversationId) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation privateConversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        return PrivateConversationDetailResponse.builder()
                .id(privateConversation.getId())
                .name(privateConversation.getName())
                .isPinned(privateConversation.isPinned())
                .type(CONVERSATION_TYPE.PRIVATE.toString())
                .members(convertUsersListToConversationMemberResponseList(privateConversation.getUsers()))
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> getListPrivateConversations(String workspaceId, String token, Integer page, Integer paging) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Conversation> conversationPage = this.conversationRepository.findConversationsByUserIdAndType(userId, CONVERSATION_TYPE.PRIVATE, pageable);
        List<Conversation> conversations = conversationPage.getContent();

        List<PrivateConversationDetailResponse> conversationDetailResponseList = conversations.stream().map(conversation -> {
            Message latestMessage = conversationRepository
                    .findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());
            int countUnread = this.conversationRepository.countUnreadByConversationId(conversation.getId(), userId);
            return PrivateConversationDetailResponse.builder()
                    .id(conversation.getId())
                    .isPinned(conversation.isPinned())
                    .name(conversation.getName())
                    .type(CONVERSATION_TYPE.PRIVATE.toString())
                    .members(convertUsersListToConversationMemberResponseList(conversation.getUsers()))
                    .message(latestMessage != null ? this.chatService.mapMessageToMessageResponse(latestMessage) : null)
                    .countUnread(countUnread)
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
    public String togglePinPrivateConversation(String workspaceId, String conversationId) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation privateConversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        privateConversation.setPinned(!privateConversation.isPinned());
        this.conversationRepository.save(privateConversation);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<ConversationMemberResponse> getMembersOfPrivateConversation(String workspaceId, String conversationId) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Conversation privateConversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        return this.convertUsersListToConversationMemberResponseList(privateConversation.getUsers());
    }
}
