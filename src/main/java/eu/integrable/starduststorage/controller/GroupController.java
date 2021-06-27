package eu.integrable.starduststorage.controller;

import eu.integrable.starduststorage.model.GroupModel;
import eu.integrable.starduststorage.repository.FileModelRepository;
import eu.integrable.starduststorage.repository.GroupModelRepository;
import eu.integrable.starduststorage.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/storage/group")
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupModelRepository groupModelRepository;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("{groupId}")
    @Operation(summary = "Get group")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity getGroupInfo(@PathVariable String groupId,
                                       Authentication authentication) {

        // Get group model
        Optional<GroupModel> groupModel = groupModelRepository.findById(groupId);
        if (groupModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body("No group");
        }

        // Check access permissions
        if (!permissionService.isAccessPermitted(authentication, groupModel.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("No access");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(groupModel.get());
    }

    @PostMapping("{groupId}")
    @Operation(summary = "Set group")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity setGroupInfo(@PathVariable String groupId,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) String permission,
                                       @RequestParam(required = false) Long quota,
                                       Authentication authentication) {

        // Check if writer
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to update files");
        }

        // Get group model
        Optional<GroupModel> gm = groupModelRepository.findById(groupId);
        if (gm.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("The group exists");
        }

        // Get owner
        String owner = authentication.getPrincipal().toString();

        // Create Group Model
        GroupModel groupModel = GroupModel.builder()
                .id(groupId)
                .description(description)
                .owner(owner)
                .permission(permission)
                .quota(quota)
                .build();

        // Store in database
        groupModel = groupModelRepository.save(groupModel);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(groupModel);
    }


    @DeleteMapping("{groupId}")
    @Operation(summary = "Delete group")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity setGroupInfo(@PathVariable String groupId,
                                       Authentication authentication) {

        // Check if writer
        if (!authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_WRITER")))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Not allowed to update files");
        }

        // Get group model
        Optional<GroupModel> groupModel = groupModelRepository.findById(groupId);
        if (groupModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("The group does not exist");
        }

        // Delete
        groupModelRepository.delete(groupModel.get());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("Deleted");
    }
}
