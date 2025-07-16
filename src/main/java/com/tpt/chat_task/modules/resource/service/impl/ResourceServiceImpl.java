package com.tpt.chat_task.modules.resource.service.impl;

import com.tpt.chat_task.infrastructure.storage.service.UploadService;
import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import com.tpt.chat_task.modules.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final UploadService uploadService;

    // id cua resource cung la id ma cloudinary tra ve
    @Override
    public Resource uploadSingleFile(MultipartFile file) throws IOException {
        Map<String, Object> cloudinaryResult = this.uploadService.uploadOneFile(file);
        String resourceType = (String) cloudinaryResult.get("resource_type");

        return Resource.builder()
                .id((String) cloudinaryResult.get("public_id"))
                .name((String) cloudinaryResult.get("original_filename"))
                .link((String) cloudinaryResult.get("secure_url"))
                .type(RESOURCE_TYPE.fromCloudinary(resourceType))
                .createdAt((LocalDateTime) cloudinaryResult.get("created_at"))
                .build();
    }


    @Override
    public List<Resource> uploadMultipleFiles(List<MultipartFile> files) throws IOException {
        List<Resource> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            Map<String, Object> cloudinaryResult = uploadService.uploadOneFile(file);
            String resourceType = (String) cloudinaryResult.get("resource_type");
            Resource response = Resource.builder()
                    .id((String) cloudinaryResult.get("public_id"))
                    .name((String) cloudinaryResult.get("original_filename"))
                    .link((String) cloudinaryResult.get("secure_url"))
                    .type(RESOURCE_TYPE.fromCloudinary(resourceType))
                    .createdAt(LocalDateTime.now())
                    .build();

            responses.add(response);
        }

        return responses;
    }

}
