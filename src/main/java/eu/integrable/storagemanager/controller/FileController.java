package eu.integrable.storagemanager.controller;

import eu.integrable.storagemanager.model.FileModel;
import eu.integrable.storagemanager.repository.FileModelRepository;
import eu.integrable.storagemanager.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    @Operation(summary = "Get file description")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity getFileDescription(@PathVariable String id) {

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isPresent()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File does not exist");
        }
    }

    @GetMapping("{id}")
    @Operation(summary = "Get file")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity downloadFile(@PathVariable String id) {

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        try {
            Resource resource = fileService.loadFile(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileModel.get().getMediaType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileModel.get().getFilename() + "\"")
                    .body(resource);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body("Can not download file");
        }
    }

    @PostMapping("")
    @Operation(summary = "Upload file")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam(required = true) String filename,
                                     @RequestParam(required = false) String description,
                                     @RequestParam(required = false) String owner,
                                     @RequestParam(required = false) String permission,
                                     @RequestParam(required = false) String mediatype,
                                     Authentication authentication) {

        // Check if allow to upload
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to upload files");
        }

        String id = UUID.randomUUID().toString();

        String checksum = "";
        try {
            checksum = DigestUtils.sha256Hex(file.getInputStream());
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        if (mediatype == null) {
            mediatype = "application/octet-stream";
        } else {
            try {
                MediaType.parseMediaType(mediatype);
            } catch (InvalidMediaTypeException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("Wrong media-type/media-type not supported");
            }
        }

        FileModel fileModel = FileModel.builder()
                .id(id)
                .filename(filename)
                .description(description)
                .owner(owner)
                .permission(permission)
                .size(file.getSize())
                .checksum(checksum)
                .mediaType(mediatype)
                .build();

        fileModelRepository.save(fileModel);

        try {
            fileService.saveFile(file, id);
        } catch (IOException ex) {
            fileModelRepository.delete(fileModel);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete file")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity deleteFile(@PathVariable String id,
                                     Authentication authentication) {

        // Check if allow to delete
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to delete files");
        }

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        fileModelRepository.delete(fileModel.get());

        try {
            fileService.deleteFile(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Deleted");

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body("Can not delete file");
        }
    }

    @PutMapping("{id}/description")
    @Operation(summary = "Update file description")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity updateDescription(@PathVariable String id,
                                            @RequestParam(required = false) String filename,
                                            @RequestParam(required = false) String description,
                                            @RequestParam(required = false) String owner,
                                            @RequestParam(required = false) String permission,
                                            @RequestParam(required = false) String mediatype,
                                            Authentication authentication) {

        // Check if allow to update
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to update descriptions");
        }

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        if (filename != null) fileModel.get().setFilename(filename);
        if (description != null) fileModel.get().setDescription(description);
        if (owner != null) fileModel.get().setOwner(owner);
        if (permission != null) fileModel.get().setPermission(permission);
        if (mediatype != null) {
            try {
                MediaType.parseMediaType(mediatype);
                fileModel.get().setMediaType(mediatype);
            } catch (InvalidMediaTypeException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("Wrong media-type/media-type not supported");
            }
        }

        fileModelRepository.save(fileModel.get());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update file")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity updateFile(@PathVariable String id,
                                     @RequestParam("file") MultipartFile multipartFile,
                                     @RequestParam(required = false) String filename,
                                     @RequestParam(required = false) String description,
                                     @RequestParam(required = false) String owner,
                                     @RequestParam(required = false) String permission,
                                     @RequestParam(required = false) String mediatype,
                                     Authentication authentication) {

        // Check if allow to update
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to update files");
        }

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        String checksum = "";
        try {
            checksum = DigestUtils.sha256Hex(multipartFile.getInputStream());
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        if (filename != null) fileModel.get().setFilename(filename);
        if (description != null) fileModel.get().setDescription(description);
        if (owner != null) fileModel.get().setOwner(owner);
        if (permission != null) fileModel.get().setPermission(permission);
        if (mediatype != null) {
            try {
                MediaType.parseMediaType(mediatype);
                fileModel.get().setMediaType(mediatype);
            } catch (InvalidMediaTypeException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("Wrong media-type/media-type not supported");
            }
        }
        fileModel.get().setChecksum(checksum);
        fileModel.get().setSize(multipartFile.getSize());

        fileModelRepository.save(fileModel.get());

        try {
            fileService.saveFile(multipartFile, id);
        } catch (IOException ex) {
            fileModelRepository.delete(fileModel.get());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel);
    }
}
