package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.dto.response.SearchMessageProjection;
import com.tpt.chat_task.modules.conversation.dto.response.SearchMessageResponse;
import com.tpt.chat_task.modules.conversation.entity.Message;
import com.tpt.chat_task.modules.conversation.entity.MessageElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageElementRepository extends JpaRepository<MessageElement, String> {
    @Modifying
    @Query("DELETE FROM MessageElement e WHERE e.message.id = :messageId")
    void deleteByMessageId(@Param("messageId") String messageId);

    @Query(value = """
        SELECT 
          me.id AS id, 
          m.conversation_id AS conversation_id,
          ts_headline('simple', me.content, plainto_tsquery('simple', :keyword)) AS highlight,
          me.content AS content, 
          me.indent AS indent,
          me.type AS type, 
          me.is_bold AS is_bold, 
          me.is_italic AS is_italic, 
          me.is_underline AS is_underline, 
          me.created_at AS created_at
        FROM message_elements me
        JOIN messages m ON m.id = me.message_id
        WHERE m.conversation_id = :conversationId 
          AND me.content_tsv @@ plainto_tsquery('simple', :keyword)
    """, nativeQuery = true)
    List<SearchMessageProjection> ftsSearchMessagesByConversationId(
            @Param("conversationId") String conversationId,
            @Param("keyword") String keyword
    );
}
