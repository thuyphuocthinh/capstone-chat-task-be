package com.tpt.chat_task.modules.resource.service;

import com.tpt.chat_task.modules.resource.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ResourceService {
    public Resource uploadSingleFile(MultipartFile file) throws IOException;
    public List<Resource> uploadMultipleFiles(List<MultipartFile> files) throws IOException;
}
