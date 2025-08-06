package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.CheckListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckListItemRepository extends JpaRepository<CheckListItem, String> {
    @Modifying
    @Query(value = """
        UPDATE checklist_items
        SET order_index = order_index - 1
        WHERE task_id = :task_id AND order_index BETWEEN :start and :end
    """, nativeQuery = true)
    void decrementOrderIndexesInRange(@Param("checkListId") String checkListId,
                                      @Param("start") int start,
                                      @Param("end") int end);

    @Modifying
    @Query(value = """
        UPDATE checklist_items
        SET order_index = order_index + 1
        WHERE task_id = :task_id AND order_index BETWEEN :start and :end
    """, nativeQuery = true)
    void incrementOrderIndexesInRange(@Param("checkListId") String checkListId,
                                      @Param("start") int start,
                                      @Param("end") int end);

}
