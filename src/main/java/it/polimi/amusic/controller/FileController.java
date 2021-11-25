package it.polimi.amusic.controller;

import org.springframework.core.io.Resource;
import it.polimi.amusic.external.gcs.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/private/storage/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile multipartFile) {
        log.info("New request to /profile/pic/upload | File Name : {}", multipartFile.getOriginalFilename());
            return ResponseEntity.ok(fileService.uploadFile(multipartFile.getResource()));
    }

    @GetMapping("/private/storage/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        log.info("New request to /profile/pic/{}", fileName);
            return ResponseEntity.ok(fileService.downloadFile(fileName));
    }


}
