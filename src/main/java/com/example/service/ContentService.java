package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.model.Content;
import com.example.model.User;

@Service
public class ContentService {

    private final Map<Long, Content> contents = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Path uploadDir;

    public ContentService() {
        // Start the server with -Dedustream.upload.dir=/some/folder to choose where uploads are kept
        String defaultDir = Paths.get(System.getProperty("java.io.tmpdir"), "edustream-uploads").toString();
        uploadDir = Paths.get(System.getProperty("edustream.upload.dir", defaultDir)).toAbsolutePath();
    }

    /**
     * Returns the normalized content type if the file is an image or a video, otherwise null.
     * SVG is rejected because it can carry scripts.
     */
    public static String allowedContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        try {
            MediaType type = MediaType.parseMediaType(contentType);
            boolean allowed = ("image".equals(type.getType()) || "video".equals(type.getType()))
                    && !type.isWildcardSubtype()
                    && !type.getSubtype().contains("svg");
            return allowed ? type.getType() + "/" + type.getSubtype() : null;
        } catch (InvalidMediaTypeException e) {
            return null;
        }
    }

    public Content store(String title, String description, MultipartFile file, String contentType, User uploader)
            throws IOException {
        Files.createDirectories(uploadDir);
        // Files are saved under a random name so user input never becomes part of a path on disk
        String storedFilename = UUID.randomUUID().toString();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, uploadDir.resolve(storedFilename));
        }
        Content content = new Content(nextId.getAndIncrement(), title, description, originalFilename(file),
                storedFilename, contentType, file.getSize(), uploader.getUsername(), uploader.getFullName(),
                new Date());
        contents.put(content.getId(), content);
        return content;
    }

    public Content findById(long id) {
        return contents.get(id);
    }

    // Newest uploads first
    public List<Content> findAll() {
        List<Content> result = new ArrayList<>(contents.values());
        Collections.sort(result, Comparator.comparingLong(Content::getId).reversed());
        return result;
    }

    public List<Content> findByUploader(String username) {
        List<Content> result = new ArrayList<>();
        for (Content content : findAll()) {
            if (content.getUploadedBy().equals(username)) {
                result.add(content);
            }
        }
        return result;
    }

    public int count() {
        return contents.size();
    }

    public Path getFile(Content content) {
        return uploadDir.resolve(content.getStoredFilename());
    }

    private static String originalFilename(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null) {
            // Some browsers send the full client-side path; keep only the file name
            name = StringUtils.getFilename(StringUtils.cleanPath(name));
        }
        return StringUtils.hasText(name) ? name : "file";
    }
}
