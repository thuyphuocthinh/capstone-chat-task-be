package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.MessageElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageElementRepository extends JpaRepository<MessageElement, String> {

    @Modifying
    @Query("DELETE FROM MessageElement e WHERE e.message.id = :messageId")
    void deleteByMessageId(@Param("messageId") String messageId);
}
