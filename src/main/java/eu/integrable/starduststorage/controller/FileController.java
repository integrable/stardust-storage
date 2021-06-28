package eu.integrable.starduststorage.controller;

import eu.integrable.starduststorage.model.FileModel;
import eu.integrable.starduststorage.model.GroupModel;
import eu.integrable.starduststorage.repository.FileModelRepository;
import eu.integrable.starduststorage.repository.GroupModelRepository;
import eu.integrable.starduststorage.service.FileService;
import eu.integrable.starduststorage.service.PermissionService;
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
@RequestMapping("/api/v1/storage/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private FileModelRepository fileModelRepository;

    @Autowired
    private GroupModelRepository groupModelRepository;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("{id}/description")
    @Operation(summary = "Get file description")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity getFileDescription(@PathVariable String id,
                                             Authentication authentication) {

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        // Check access permissions
        if (!permissionService.isAccessPermitted(authentication, fileModel.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("No access");
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel.get());
    }

    @GetMapping("{id}")
    @Operation(summary = "Get file")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity downloadFile(@PathVariable String id,
                                       Authentication authentication) {

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        // Check access permissions
        if (!permissionService.isAccessPermitted(authentication, fileModel.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("No access");
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
                                     @RequestParam(required = false, name = "group") String groupId,
                                     @RequestParam(required = false) String permission,
                                     @RequestParam(required = false) String mediatype,
                                     Authentication authentication) {

        // Check if writer
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

        // Get a group
        Optional<GroupModel> group = Optional.empty();
        if (groupId != null) {
            group = groupModelRepository.findById(groupId);
            if (group.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("Group does not exist");
            }
        }

        // Check quota
        if (group.isPresent()) {
            Long quota = group.get().getQuota();
            if (quota != null) {
                Long size = group.get().getSize() + file.getSize();
                if (size > quota) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Quota reached");
                }
            }
        }

        // Check if permissions are correct
        if (permission != null) {
            if (!permissionService.arePermissionsCorrect(authentication, permission)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("Wrong permissions format");
            }
        }

        // Get owner
        String owner = authentication.getPrincipal().toString();

        // Create File Model
        FileModel fileModel = FileModel.builder()
                .id(id)
                .filename(filename)
                .description(description)
                .owner(owner)
                .group(group.orElse(null))
                .permission(permission)
                .size(file.getSize())
                .checksum(checksum)
                .mediaType(mediatype)
                .build();

        // Store File Model in database
        fileModelRepository.save(fileModel);

        // Save file in media
        try {
            fileService.saveFile(file, id);
            group.ifPresent(groupModel -> {
                groupModel.increaseSize(file.getSize());
                groupModelRepository.save(groupModel);
            });
        } catch (IOException ex) {
            fileModelRepository.delete(fileModel);
            group.ifPresent(groupModel -> {
                groupModel.decreaseSize(file.getSize());
                groupModelRepository.save(groupModel);
            });
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileModel);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete file")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity deleteFile(@PathVariable String id,
                                     Authentication authentication) {

        // Check if writer
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to delete files");
        }

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        // Check access permissions
        if (!permissionService.isAccessPermitted(authentication, fileModel.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("No access");
        }

        fileModelRepository.delete(fileModel.get());

        try {
            // Decrease group size
            GroupModel groupModel = fileModel.get().getGroup();
            groupModel.decreaseSize(fileModel.get().getSize());
            groupModelRepository.save(groupModel);

            // Remove file
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
                                            @RequestParam(required = false, name = "group") String groupId,
                                            @RequestParam(required = false) String permission,
                                            @RequestParam(required = false) String mediatype,
                                            Authentication authentication) {

        // Check if writer
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to update descriptions");
        }

        Optional<FileModel> fileModel = fileModelRepository.findById(id);
        if (fileModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No file");
        }

        // Check access permissions
        if (!permissionService.isAccessPermitted(authentication, fileModel.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("No access");
        }

        // Check if permissions are correct
        if (!permissionService.arePermissionsCorrect(authentication, permission)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("Wrong permissions format");
        }

        if (filename != null) fileModel.get().setFilename(filename);
        if (description != null) fileModel.get().setDescription(description);
        if (groupId != null) {
            // Get a group
            Optional<GroupModel> group = groupModelRepository.findById(groupId);
            if (group.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("Group does not exist");
            }
            fileModel.get().setGroup(group.get());
        }
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

}
