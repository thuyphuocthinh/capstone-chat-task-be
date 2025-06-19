package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.Conversation;
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
    Page<Conversation> findConversationsByUserIdAndType(@Param("userId") String userId, @Param("type") String type, Pageable pageable);

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
            @Param("type") String type,
            Pageable pageable
    );
}
