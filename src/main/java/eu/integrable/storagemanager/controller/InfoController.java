package eu.integrable.storagemanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/info/")
public class InfoController {

    @Autowired
    private BuildProperties buildProperties;

    @GetMapping("getVersion")
    public ResponseEntity<String> getVersion() {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildProperties.getVersion());
    }
}
