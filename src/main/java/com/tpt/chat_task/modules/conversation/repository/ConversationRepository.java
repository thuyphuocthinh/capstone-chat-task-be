package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.dto.response.UnreadCountDTO;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.entity.Message;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    @Query("SELECT c FROM Conversation c JOIN c.users u WHERE u.id = :userId")
    List<Conversation> findAllByUserId(@Param("userId") String userId);

    @Query(value = """
        SELECT c.* 
        FROM conversations c
        JOIN conversation_users cu ON c.id = cu.conversation_id
        WHERE cu.user_id = :userId
        AND c.type = :type
    """, nativeQuery = true)
    List<Conversation> findRoomsByUserIdAndType(@Param("userId") String userId, @Param("type") String type);

    @Query("SELECT c FROM Conversation c JOIN c.users u WHERE u.id = :userId AND c.type = :type")
    Page<Conversation> findConversationsByUserIdAndType(@Param("userId") String userId, @Param("type") CONVERSATION_TYPE type, Pageable pageable);

    @Query("""
        SELECT c FROM Conversation c
        JOIN c.users u
        WHERE u.id = :userId
        AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND c.type = :type
    """)
    Page<Conversation> searchByUserIdAndName(
            @Param("userId") String userId,
            @Param("keyword") String keyword,
            @Param("type") CONVERSATION_TYPE type,
            Pageable pageable
    );

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Conversation c
        JOIN c.users u
        WHERE c.id = :conversationId AND u.id = :userId
    """)
    boolean existsConversationByConversationIdAndUserId(@Param("conversationId") String conversationId, @Param("userId") String userId);

    @Query(value = """
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId 
        ORDER BY created_at DESC 
        LIMIT 1
    """, nativeQuery = true)
    Message findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);

    @Query(value = """
        SELECT COUNT(*)
        FROM messages m
        JOIN message_seen ms ON ms.message_id = m.id
        WHERE ms.is_seen = FALSE AND ms.user_id = :userId AND m.conversation_id = :conversationId
    """, nativeQuery = true)
    int countUnreadByConversationId(@Param("conversationId") String conversationId, @Param("userId") String userId);

    @Query("""
        SELECT m.conversation.id AS conversationId, COUNT(m) AS unreadCount
        FROM MessageSeen ms
        JOIN ms.message m
        WHERE m.conversation.id IN :conversationIds AND ms.user.id = :userId AND ms.isSeen = false
        GROUP BY m.conversation.id
    """)
    List<UnreadCountDTO> countUnreadMessagesForConversations(@Param("conversationIds") List<String> conversationIds, @Param("userId") String userId);

    @Query(value = """
        SELECT m FROM messages m
        INNER JOIN (
                SELECT conversation_id, MAX(created_at) AS max_created_at
                FROM messages
                WHERE conversation_id IN (:conversationIds)
                GROUP BY conversation_id
        ) latest ON m.conversation_id = latest.conversation_id AND m.created_at = latest.max_created_at
    """, nativeQuery = true)
    List<Message> findListOfLatestMessagesByConversationIds(@Param("conversationIds") List<String> conversationIds);
}
