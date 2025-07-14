package com.tpt.chat_task.modules.queue.repository;

import com.tpt.chat_task.modules.queue.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueRepository extends JpaRepository<Queue, String> {
    boolean existsByQueueName(String queueName);

    @Query(value = """
        SELECT *
        FROM queues
        WHERE listener_id = :listenerId
    """, nativeQuery = true)
    List<Queue> findAllByListenerId(@Param("listenerId") String listenerId);
}
