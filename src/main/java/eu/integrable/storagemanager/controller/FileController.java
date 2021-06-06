package eu.integrable.storagemanager.controller;

import eu.integrable.storagemanager.model.FileModel;
import eu.integrable.storagemanager.repository.FileModelRepository;
import eu.integrable.storagemanager.service.FileService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/file/")
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

    @PostMapping("")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile multipartFile,
                                        @RequestBody FileModel fileDescription) {

        String id = UUID.randomUUID().toString();

        String checksum = "";
        try {
            checksum = DigestUtils.sha256Hex(multipartFile.getInputStream());
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        FileModel fileModel = FileModel.builder()
                .id(id)
                .filename(fileDescription.getFilename())
                .description(fileDescription.getDescription())
                .owner(fileDescription.getOwner())
                .permission(fileDescription.getPermission())
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

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModelRepository);
    }
}
