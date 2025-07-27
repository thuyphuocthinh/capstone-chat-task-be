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
        WHERE parent_id = :messageId
    """, nativeQuery = true)
    List<Message> getRepliesByMessageId(@Param("messageId") String messageId);

    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId
        ORDER BY created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListMessagesByConversationId(@Param("conversationId") String conversationId, @Param("limit") int limit);

    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId AND created_at < :time
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListMessagesByConversationIdAndAboveTime(
            @Param("conversationId") String conversationId,
            @Param("time") LocalDateTime time,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId AND created_at > :time
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListMessagesByConversationIdAndBelowTime(
            @Param("conversationId") String conversationId,
            @Param("time") LocalDateTime time,
            @Param("limit") int limit
    );

    @Modifying
    @Query(value = """
        DELETE FROM message_resources WHERE message_id = :messageId
    """, nativeQuery = true)
    void deleteMessageResources(@Param("messageId") String messageId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.id = :id")
    void forceDelete(@Param("id") String id);


    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at < :time AND conversation_id = :conversationId
    """, nativeQuery = true)
    Integer countAboveMessages(@Param("conversationId") String conversationId, @Param("time") LocalDateTime time);

    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at > :time AND conversation_id = :conversationId
    """, nativeQuery = true)
    Integer countBelowMessages(@Param("conversationId") String conversationId, @Param("time") LocalDateTime time);

    @Query(value = """
        SELECT *
        FROM messages
        WHERE conversation_id = :conversationId AND is_pinned = true
    """, nativeQuery = true)
    List<Message> getListPinnedMessagesByConversationId(@Param("conversationId") String conversationId);

    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id = :messageId
        ORDER BY created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListRepliesMessageByMessageId(@Param("messageId") String messageId, @Param("limit") Integer limit);

    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id = :messageId AND created_at < :time
        ORDER BY created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListRepliesMessageByMessageIdAndAboveTime(
            @Param("messageId") String messageId,
            @Param("time") LocalDateTime time,
            @Param("limit") Integer limit
    );

    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id = :messageId AND created_at > :time
        ORDER BY created_at ASC
        LIMIT :limit
    """, nativeQuery = true)
    List<Message> getListRepliesMessageByMessageIdAndBelowTime(
            @Param("messageId") String messageId,
            @Param("time") LocalDateTime time,
            @Param("limit") Integer limit
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at < :time AND parent_id = :messageId
    """, nativeQuery = true)
    Integer countAboveMessagesReplies(@Param("messageId") String messageId, @Param("time") LocalDateTime time);

    @Query(value = """
        SELECT COUNT(*)
        FROM messages
        WHERE created_at > :time AND parent_id = :messageId
    """, nativeQuery = true)
    Integer countBelowMessagesReplies(@Param("messageId") String messageId, @Param("time") LocalDateTime time);

    @Query(value = """
        SELECT *
        FROM messages
        WHERE id = :parentId
    """, nativeQuery = true)
    Message findByParentId(@Param("parentId") String parentId);

    @Query(value = """
        SELECT m.*
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
            m.sender_id = :userId
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
            m.sender_id = :userId
            OR me.content = :userId
          )
        """, nativeQuery = true)
    Page<Message> findAllThreadMessages(@Param("userId") String userId, Pageable pageable);

    @Query(value = """
        SELECT COUNT(*)
        FROM messages m
        WHERE m.parent_id = :messageId
    """, nativeQuery = true)
    Integer countRepliesOfMessage(@Param("messageId") String messageId);
}
