package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.CheckList;
import com.tpt.chat_task.modules.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, String> {
    @Modifying
    @Query(value = """
        UPDATE checklists
        SET order_index = order_index - 1
        WHERE task_id = :task_id AND order_index BETWEEN :start and :end
    """, nativeQuery = true)
    void decrementOrderIndexesInRange(@Param("taskBoardId") String taskId,
                                      @Param("start") int start,
                                      @Param("end") int end);

    @Modifying
    @Query(value = """
        UPDATE checklists
        SET order_index = order_index + 1
        WHERE task_id = :task_id AND order_index BETWEEN :start and :end
    """, nativeQuery = true)
    void incrementOrderIndexesInRange(@Param("taskBoardId") String taskId,
                                      @Param("start") int start,
                                      @Param("end") int end);
}
