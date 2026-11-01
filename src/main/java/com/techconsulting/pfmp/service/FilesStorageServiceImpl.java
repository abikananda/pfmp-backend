package com.techconsulting.pfmp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FilesStorageServiceImpl implements FilesStorageService {

    private final Path root = Paths.get("uploads");

    @Override
    public void init() {
        log.debug("Initializing upload directory: {}", root);
        try {
            Files.createDirectories(root);
            log.info("Upload directory initialized successfully");
        } catch (IOException e) {
            log.error("Could not initialize folder for upload", e);
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public void save(MultipartFile file) {
        log.debug("Saving file: {}", file.getOriginalFilename());
        try {
            String filename = file.getOriginalFilename() + "_" + System.currentTimeMillis();
            Files.copy(file.getInputStream(), this.root.resolve(filename));
            log.info("File saved successfully: {}", filename);
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                log.warn("File already exists: {}", file.getOriginalFilename());
                throw new RuntimeException("A file of that name already exists.");
            }
            log.error("Error saving file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        log.debug("Loading file: {}", filename);
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                log.debug("File loaded successfully: {}", filename);
                return resource;
            } else {
                log.error("File not found or not readable: {}", filename);
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL for file: {}", filename, e);
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

}
