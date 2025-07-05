package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.MessageSeen;
import com.tpt.chat_task.modules.conversation.entity.MessageUserId;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageSeenRepository extends JpaRepository<MessageSeen, MessageUserId> {

    @Query(value = """
        SELECT COUNT(*)
        FROM MessageSeen ms
        JOIN Message m ON ms.message_id = m.id
        JOIN Conversation c ON m.conversation_id = c.id
        WHERE ms.is_seen = false AND c.type = :type AND ms.user_id = :userId
    """, nativeQuery = true)
    int countUnreadPublicConversations(@Param("userId") String userId, @Param("type") CONVERSATION_TYPE type);

}
