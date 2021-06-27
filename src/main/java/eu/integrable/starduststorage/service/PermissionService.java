package eu.integrable.starduststorage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.integrable.starduststorage.model.FileModel;
import eu.integrable.starduststorage.model.GroupModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PermissionService {

    public boolean isAccessPermitted(Authentication authentication, FileModel fileModel) {

        // Allow if permission not defined
        if (fileModel.getPermission() == null || fileModel.getPermission().isBlank()) {
            return true;
        }

        // Allow admin
        if (authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.equals(new SimpleGrantedAuthority("ROLE_ADMIN")))) {
            return true;
        }

        // Check the permission list
        try {
            String username = authentication.getPrincipal().toString();
            String permissionsJson = fileModel.getPermission();
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> permissions = (List<String>)objectMapper.readValue(permissionsJson, List.class);

            if (permissions.stream().anyMatch(permission -> permission.equals(username))) {
                return true;
            } else {
                return false;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isAccessPermitted(Authentication authentication, GroupModel groupModel) {
        // TODO
        return true;
    }

    public boolean arePermissionsCorrect(Authentication authentication, String permissionsJson) {

        try {
            String username = authentication.getPrincipal().toString();
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> permissions = (List<String>)objectMapper.readValue(permissionsJson, List.class);

            if (permissions.stream().anyMatch(permission -> permission.equals(username))) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
