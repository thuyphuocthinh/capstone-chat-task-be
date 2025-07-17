package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, String> {
    @Query(value = """
        SELECT *
        FROM task_groups
        WHERE task_board_id = :taskBoardId
        ORDER BY order_index
    """, nativeQuery = true)
    List<TaskGroup> findAllByTaskBoard(@Param("taskBoardId") String taskBoardId);
}
