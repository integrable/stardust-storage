package eu.integrable.storagemanager.controller;

import eu.integrable.storagemanager.model.FileModel;
import eu.integrable.storagemanager.repository.FileModelRepository;
import eu.integrable.storagemanager.service.FileService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private FileModelRepository fileModelRepository;

    @GetMapping("{id}/description")
    public ResponseEntity<?> getFileDescription(@PathVariable String id) {

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isPresent()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File does not exist");
        }
    }

    @GetMapping("{id}")
    public ResponseEntity downloadFile(@PathVariable String id) {

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        try {
            Resource resource = fileService.loadFile(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileModel.get().getFilename()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileModel.get().getFilename() + "\"")
                    .body(resource);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body("Can not download file");
        }
    }

    @PostMapping("")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile multipartFile,
                                        @RequestParam(required = true) String filename,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false) String owner,
                                        @RequestParam(required = false) String permission) {

        String id = UUID.randomUUID().toString();

        String checksum = "";
        try {
            checksum = DigestUtils.sha256Hex(multipartFile.getInputStream());
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        FileModel fileModel = FileModel.builder()
                .id(id)
                .filename(filename)
                .description(description)
                .owner(owner)
                .permission(permission)
                .size(multipartFile.getSize())
                .checksum(checksum)
                .build();

        fileModelRepository.save(fileModel);

        try {
            fileService.saveFile(multipartFile, id);
        } catch (IOException ex) {
            fileModelRepository.delete(fileModel);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel);
    }
}
