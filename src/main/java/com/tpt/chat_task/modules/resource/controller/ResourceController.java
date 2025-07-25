package com.tpt.chat_task.modules.resource.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.modules.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping("/upload-single")
    public ResponseEntity<?> uploadFile(
            @RequestPart MultipartFile file
    ) throws IOException {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.resourceService.uploadSingleFile(file))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestPart List<MultipartFile> files
    ) throws IOException {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.resourceService.uploadMultipleFiles(files))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
