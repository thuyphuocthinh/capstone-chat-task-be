package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

}
