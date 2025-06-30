package com.tpt.chat_task.modules.conversation.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.CreateEmojiRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdateEmojiRequest;
import com.tpt.chat_task.modules.conversation.dto.response.EmojiDetailResponse;
import com.tpt.chat_task.modules.conversation.dto.response.MessageReactResponse;

import java.util.List;

public interface IconService {
    public EmojiDetailResponse createEmoji(CreateEmojiRequest request);
    public EmojiDetailResponse updateEmoji(String id, UpdateEmojiRequest request);
    public String deleteEmoji(String emojiId);
    public List<EmojiDetailResponse> getAllEmojis();
    public List<EmojiDetailResponse> createEmojis(List<CreateEmojiRequest> requests);
    public List<MessageReactResponse> getReactionsByMessageId(String messageId) throws NotFoundException;
}
