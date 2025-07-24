package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    @Query(value = """
        SELECT t.*
        FROM tasks t
        JOIN task_users tu ON t.id = tu.task_id
        WHERE t.task_group_id = :groupId AND tu.user_id = :userId
        """, countQuery = """
        SELECT COUNT(*)
        FROM tasks t
        JOIN task_users tu ON t.id = tu.task_id
        WHERE t.task_group_id = :groupId AND tu.user_id = :userId
    """, nativeQuery = true)
    Page<Task> findAllByUserIdAndGroupId(
            @Param("userId") String userId,
            @Param("groupId") String groupId,
            Pageable pageable
    );


    @Modifying
    @Query(value = """
        UPDATE tasks
        SET order_index = order_index - 1
        WHERE id = :taskId AND order_index BETWEEN :start and :end
    """, nativeQuery = true)
    void decrementOrderIndexesInRange(@Param("taskId") String taskId,
                                      @Param("start") int start,
                                      @Param("end") int end);

    @Modifying
    @Query(value = """
        UPDATE tasks
        SET order_index = order_index + 1
        WHERE id = :taskId AND order_index BETWEEN :start and :end
    """, nativeQuery = true)
    void incrementOrderIndexesInRange(@Param("taskId") String taskId,
                                      @Param("start") int start,
                                      @Param("end") int end);
}
