package com.integral.controller;


import lombok.RequiredArgsConstructor;
import com.integral.model.Work;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.integral.service.WorkService;

@RestController
@RequestMapping("/works")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkController {

    private final WorkService service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Work> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentName") String studentName){

        Work savedWork = service.uploadWork(file, studentName);
        return ResponseEntity.ok(savedWork);
    }


}
