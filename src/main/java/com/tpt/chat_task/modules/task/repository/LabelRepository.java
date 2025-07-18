package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabelRepository extends JpaRepository<Label, String> {

    @Query(value = """
        SELECT *
        FROM labels
        WHERE task_board_id = :taskBoardId
    """, nativeQuery = true)
    List<Label> findAllByTaskBoard(@Param("taskBoardId") String taskBoardId);
}
