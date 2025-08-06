package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = """
        SELECT *
        FROM task_groups
        WHERE task_board_id = :taskBoardId
        ORDER BY order_index DESC
        LIMIT 1
    """, nativeQuery = true)
    TaskGroup findLastTaskGroupByTaskBoardId(@Param("taskBoardId") String taskBoardId);

    @Query(value = """
        SELECT COUNT(*)
        FROM task_groups
        WHERE task_board_id = :taskBoardId
    """, nativeQuery = true)
    int countTaskGroupsByTaskBoardId(@Param("taskBoardId") String taskBoardId);

    @Query(value = """
        SELECT *
        FROM task_groups
        WHERE task_board_id = :taskBoardId AND order_index = :orderIndex
    """, nativeQuery = true)
    TaskGroup findTaskGroupByTaskBoardIdAndOrderIndex(@Param("taskBoardId") String taskBoardId, @Param("orderIndex") int orderIndex);

    @Modifying
    @Query("""
        UPDATE TaskGroup tg
        SET tg.orderIndex = tg.orderIndex - 1
        WHERE tg.taskBoard.id = :taskBoardId
          AND tg.orderIndex BETWEEN :start AND :end
    """)
    void decrementOrderIndexesInRange(@Param("taskBoardId") String taskBoardId,
                                      @Param("start") int start,
                                      @Param("end") int end);

    @Modifying
    @Query("""
        UPDATE TaskGroup tg
        SET tg.orderIndex = tg.orderIndex + 1
        WHERE tg.taskBoard.id = :taskBoardId
          AND tg.orderIndex BETWEEN :start AND :end
    """)
    void incrementOrderIndexesInRange(@Param("taskBoardId") String taskBoardId,
                                      @Param("start") int start,
                                      @Param("end") int end);


}
