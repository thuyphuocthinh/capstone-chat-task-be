package com.tpt.chat_task.modules.resource.repository;

import com.tpt.chat_task.modules.resource.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {

}
