package com.tpt.chat_task.modules.resource.repository;

import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    @Query(value = """
        SELECT r.*
        FROM resource r
        JOIN messages m ON r.message_id = m.id
        WHERE m.conversation_id = :conversationId
    """, nativeQuery = true)
    List<Resource> findByConversationId(@Param("conversationId") String conversationId);

    @Query(value = """
        SELECT r.*
        FROM resource r
        JOIN messages m ON r.message_id = m.id
        WHERE m.conversation_id = :conversationId AND r.type = :type
    """, nativeQuery = true)
    List<Resource> findByConversationIdAndType(@Param("conversationId") String conversationId, @Param("type")RESOURCE_TYPE type);
}
