package com.tpt.chat_task.modules.queue.repository;

import com.tpt.chat_task.modules.queue.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
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

    Queue findByQueueName(String queueName);

    @Modifying
    @Query(value = """
        DELETE FROM queues
        WHERE queue_name = :queueName
    """, nativeQuery = true)
    void deleteAllByQueueName(@Param("queueName") String queueName);


    @Modifying
    @Query(value = """
        DELETE FROM queues
        WHERE queue_name = :queueName
            AND listener_id = :listenerId
            AND exchange_name = :exchange
            AND routing_key = :routingKey
    """, nativeQuery = true)
    void deleteByQueueNameListenerRoutingKeyExchange(
            @Param("queueName") String queueName,
            @Param("listenerId") String listenerId,
            @Param("exchange") String exchange,
            @Param("routingKey") String routingKey
    );

    @Query(value = """
        SELECT COUNT(*) > 0
        FROM queues
        WHERE queue_name = :queueName AND exchange_name = :exchangeName AND routing_key = :routingKey AND listener_id = :listenerId
    """, nativeQuery = true)
    boolean existsByQueueNameAndExchangeNameAndRoutingKey(
            @Param("queueName") String queueName,
            @Param("exchangeName") String exchangeName,
            @Param("routingKey") String routingKey,
            @Param("listenerId") String listenerId
    );
}
