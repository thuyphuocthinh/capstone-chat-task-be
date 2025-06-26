package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {


    @Query(value = """
        SELECT *
        FROM messages
        WHERE parent_id = ?1
    """, nativeQuery = true)
    List<Message> getRepliesByMessageId(String messageId);
}
