package eu.integrable.storagemanager.service;

import eu.integrable.storagemanager.property.StorageProperty;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Checksum;

@Service
public class FileService {

    @Autowired
    private StorageProperty storageProperty;

    private final Path fileStorageLocation = Paths.get(storageProperty.getDirectory()).toAbsolutePath().normalize();

    public void saveFile(MultipartFile file, String filename) throws IOException {

        Path filePath = fileStorageLocation.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public Resource loadFile(String filename) throws IOException {

        Path filePath = fileStorageLocation.resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        } else {
            throw new IOException("Resource does not exist in physical form");
        }
    }

    public void deleteFile(String filename) throws IOException {

        Path filePath = fileStorageLocation.resolve(filename);
        Files.delete(filePath);
    }

    public String calculateChecksum(String filename) throws IOException {

        Path filePath = fileStorageLocation.resolve(filename);
        String checksum = DigestUtils.sha256Hex(new FileInputStream(filePath.toFile()));
        return checksum;
    }

    public Long calculateSize(String filename) throws IOException {

        Path filePath = fileStorageLocation.resolve(filename);
        long size = Files.size(filePath);
        return size;
    }
}
