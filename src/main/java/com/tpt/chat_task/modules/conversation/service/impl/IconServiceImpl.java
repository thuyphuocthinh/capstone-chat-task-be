package com.tpt.chat_task.modules.conversation.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.dto.request.CreateEmojiRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdateEmojiRequest;
import com.tpt.chat_task.modules.conversation.dto.response.EmojiDetailResponse;
import com.tpt.chat_task.modules.conversation.entity.Icon;
import com.tpt.chat_task.modules.conversation.repository.IconRepository;
import com.tpt.chat_task.modules.conversation.service.IconService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IconServiceImpl implements IconService {
    private final IconRepository iconRepository;

    @Override
    public EmojiDetailResponse createEmoji(CreateEmojiRequest request) {
        Icon icon = Icon.builder()
                .name(request.getName())
                .type(request.getType())
                .build();

        icon = iconRepository.save(icon);

        return EmojiDetailResponse.builder()
                .id(icon.getId())
                .name(icon.getName())
                .type(icon.getType())
                .build();
    }

    @Override
    public EmojiDetailResponse updateEmoji(String id, UpdateEmojiRequest request) {
        Icon icon = iconRepository.findById(id).orElseThrow(() -> new NotFoundException(ConversationError.ICON_NOT_FOUND));

        if(request.getName() != null) {
            icon.setName(request.getName());
        }

        if(request.getType() != null) {
            icon.setType(request.getType());
        }

        icon = iconRepository.save(icon);

        return EmojiDetailResponse.builder()
                .id(icon.getId())
                .name(icon.getName())
                .type(icon.getType())
                .build();
    }

    @Override
    public String deleteEmoji(String emojiId) {
        Icon icon = iconRepository.findById(emojiId).orElseThrow(() -> new NotFoundException(ConversationError.ICON_NOT_FOUND));
        iconRepository.delete(icon);

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<EmojiDetailResponse> getAllEmojis() {
        List<Icon> icons = iconRepository.findAll();
        return icons.stream().map(icon -> EmojiDetailResponse.builder().id(icon.getId()).name(icon.getName()).type(icon.getType()).build()).toList();
    }

    @Override
    public List<EmojiDetailResponse> createEmojis(List<CreateEmojiRequest> requests) {
        List<EmojiDetailResponse> responses = new ArrayList<>();

        for (CreateEmojiRequest request : requests) {
            EmojiDetailResponse response = createEmoji(request);
            responses.add(response);
        }

        return responses;
    }
}
