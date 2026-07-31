package com.recruitment.service;

import com.recruitment.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, String prefix) {
        try {
            Files.createDirectories(uploadDir);
            String storedName = prefix + "-" + System.currentTimeMillis() + "-" + sanitize(file.getOriginalFilename());
            Path target = uploadDir.resolve(storedName).normalize();
            file.transferTo(target);
            return storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    public Resource loadAsResource(String storedName) {
        Path file = uploadDir.resolve(storedName).normalize();
        if (!Files.exists(file)) {
            throw new NotFoundException("File not found: " + storedName);
        }
        return new FileSystemResource(file);
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(uploadDir.resolve(storedName).normalize());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete file: " + storedName, e);
        }
    }

    private String sanitize(String filename) {
        return filename == null ? "resume" : filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
