package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
        SELECT me.*
        FROM message_elements me
        JOIN messages m ON me.message_id = m.id
        JOIN conversations c ON m.conversation_id = c.id
        WHERE c.id = :conversationId
        AND to_tsvector('simple', me.content) @@ plainto_tsquery('simple', :keyword)
    """, nativeQuery = true)
    List<Message> searchMessageElementsByConversationIdAndKeyword(
            @Param("conversationId") String conversationId,
            @Param("keyword") String keyword
    );

    @Query(value = """
        SELECT DISTINCT m.*
        FROM messages m
        JOIN message_elements me ON me.message_id = m.id
        WHERE 
            (m.is_thread_root = true OR m.parent_id IN (
                SELECT id FROM messages WHERE is_thread_root = true
            ))
          AND (
            m.user_id = :userId
            OR me.content = :userId
          )
        ORDER BY m.conversation_id, m.created_at
        """,
                countQuery = """
        SELECT COUNT(DISTINCT m.id)
        FROM messages m
        JOIN message_elements me ON me.message_id = m.id
        WHERE 
            (m.is_thread_root = true OR m.parent_id IN (
                SELECT id FROM messages WHERE is_thread_root = true
            ))
          AND (
            m.user_id = :userId
            OR me.content = :userId
          )
        """, nativeQuery = true)
    Page<Message> findAllThreadMessages(@Param("userId") String userId, Pageable pageable);

}
