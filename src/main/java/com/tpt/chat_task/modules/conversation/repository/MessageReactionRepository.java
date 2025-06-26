package com.tpt.chat_task.modules.conversation.repository;

import com.tpt.chat_task.modules.conversation.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {

    @Query(value = """
        SELECT * 
        FROM message_reaction AS mr
        WHERE mr.message_id = ?1
    """, nativeQuery = true)
    List<MessageReaction> getReactionsByMessageId(String messageId);

    @Query("SELECT mr FROM MessageReaction mr WHERE mr.message.id = :messageId AND mr.icon.id = :iconId")
    MessageReaction getReactionByMessageIdAndIconId(@Param("messageId") String messageId, @Param("iconId") String iconId);
}
