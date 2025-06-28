package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id = ?1
    """, nativeQuery = true)
    List<Message> getRepliesByMessageId(String messageId);

    @Modifying
    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId
        ORDER BY created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListMessagesByConversationId(String conversationId, int limit);

    @Modifying
    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId AND created_at >= :createdAt
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListMessagesByConversationIdAndAboveTime(String conversationId, LocalDateTime time, int limit);

    @Modifying
    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId AND created_at <= :createdAt
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListMessagesByConversationIdAndBelowTime(String conversationId, LocalDateTime time, int limit);

    @Modifying
    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at >= :time AND conversation_id = :conversationId
    """, nativeQuery = true)
    Integer countAboveMessages(String conversationId, LocalDateTime time);

    @Modifying
    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at <= :time AND conversation_id = :conversationId
    """, nativeQuery = true)
    Integer countBelowMessages(String conversationId, LocalDateTime time);

    @Modifying
    @Query(value = """
        SELECT *
        FROM messaegs
        WHERE conversation_id = :conversationId
    """, nativeQuery = true)
    List<Message> getListPinnedMessagesByConversationId(String conversationId);

    @Modifying
    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id := messageId
        ORDER BY created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListRepliesMessageByMessageId(String messageId, Integer limit);

    @Modifying
    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id := messageId AND created_at >= :createdAt
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListRepliesMessageByMessageIdAndAboveTime(String messageId, LocalDateTime time, Integer limit);


    @Modifying
    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id := messageId AND created_at <= :createdAt
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListRepliesMessageByMessageIdAndBelowTime(String messageId, LocalDateTime time, Integer limit);


    @Modifying
    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at >= :time AND parent_id = :messageId
    """, nativeQuery = true)
    Integer countAboveMessagesReplies(String messageId, LocalDateTime time);

    @Modifying
    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at <= :time AND parent_id = :messageId
    """, nativeQuery = true)
    Integer countBelowMessagesReplies(String messageId, LocalDateTime time);

    Message findByParentId(String parentId);
}
