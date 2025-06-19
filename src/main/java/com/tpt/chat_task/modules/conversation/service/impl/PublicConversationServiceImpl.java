package com.tpt.chat_task.modules.conversation.service.impl;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.dto.request.CreatePublicConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdatePublicConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.response.ConversationMemberResponse;
import com.tpt.chat_task.modules.conversation.dto.response.PublicConversationDetailResponse;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_MEMBER_ROLE;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.repository.ConversationRepository;
import com.tpt.chat_task.modules.conversation.service.PublicConversationService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_ROLE;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.enums.WORKSPACE_USER_ROLE;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicConversationServiceImpl implements PublicConversationService {
    private final ConversationRepository conversationRepository;

    private final WorkspaceRepository workspaceRepository;

    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;

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
    public PublicConversationDetailResponse createNewPublicConversation(String workspaceId, CreatePublicConversationRequest request) throws NotFoundException {
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
                .build();

        conversation = this.conversationRepository.save(conversation);

        return PublicConversationDetailResponse.builder()
                .id(conversation.getId())
                .isPinned(conversation.isPinned())
                .type(conversation.getType().name())
                .name(conversation.getName())
                .build();
    }

    @Override
    public PublicConversationDetailResponse getPublicConversationDetail(String conversationId) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        return PublicConversationDetailResponse.builder()
                .id(conversation.getId())
                .isPinned(conversation.isPinned())
                .type(conversation.getType().name())
                .name(conversation.getName())
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> getListPublicConversations(String token, Integer page, Integer paging) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Conversation> conversationPage = this.conversationRepository.findConversationsByUserIdAndType(userId, CONVERSATION_TYPE.GROUP.name(), pageable);
        List<Conversation> conversations = conversationPage.getContent();

        List<PublicConversationDetailResponse> conversationDetailResponseList = conversations.stream().map(conversation -> {
            return PublicConversationDetailResponse.builder()
                    .id(conversation.getId())
                    .isPinned(conversation.isPinned())
                    .type(conversation.getType().name())
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
    public SuccessResponseWithMetadata<?> searchListPublicConversations(String token, String name, Integer page, Integer paging) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Conversation> conversationPage = this.conversationRepository.searchByUserIdAndName(userId, CONVERSATION_TYPE.GROUP.name(), CONVERSATION_TYPE.GROUP.name(),pageable);
        List<Conversation> conversations = conversationPage.getContent();

        List<PublicConversationDetailResponse> conversationDetailResponseList = conversations.stream().map(conversation -> {
            return PublicConversationDetailResponse.builder()
                    .id(conversation.getId())
                    .isPinned(conversation.isPinned())
                    .type(conversation.getType().name())
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
    public PublicConversationDetailResponse updatePublicConversation(String id, UpdatePublicConversationRequest request) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));

        conversation.setName(request.getName());
        this.conversationRepository.save(conversation);

        return PublicConversationDetailResponse.builder()
                .id(conversation.getId())
                .isPinned(conversation.isPinned())
                .type(conversation.getType().name())
                .name(conversation.getName())
                .build();
    }

    @Override
    public String deletePublicConversation(String id) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        this.conversationRepository.delete(conversation);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String togglePinnedConversation(String id) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        conversation.setPinned(!conversation.isPinned());
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String addMemberToPublicConversation(String id, String userId) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        conversation.getUsers().add(user);
        this.conversationRepository.save(conversation);

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String removeMemberFromPublicConversation(String id, String userId) throws NotFoundException {
        Conversation conversation = this.conversationRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        conversation.getUsers().remove(user);
        this.conversationRepository.save(conversation);

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<ConversationMemberResponse> getConversationMembers(String conversationId) throws NotFoundException {
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
