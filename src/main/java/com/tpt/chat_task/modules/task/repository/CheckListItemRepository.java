package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.CheckListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckListItemRepository extends JpaRepository<CheckListItem, String> {
}
