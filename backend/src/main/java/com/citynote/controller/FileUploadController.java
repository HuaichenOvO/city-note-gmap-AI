package com.citynote.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/upload")
public class FileUploadController {
    private final String filePrefix = "/api/upload/image";

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            System.out.printf("[File Controller] Request uploading file: %s, size: %s bytes, path: %s\n",
                    file.getOriginalFilename(), file.getSize(), uploadPath);

            // Check file size (50MB limit)
            if (file.getSize() > 50 * 1024 * 1024) {
                System.err.println("File too large: " + file.getSize() + " bytes");
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 50MB limit"));
            }

            // Check file type by extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid filename"));
            }

            String extension = getExtension(originalFilename);
            if (!isValidImageExtension(extension)) {
                System.err.println("Invalid file extension: " + extension);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only image files are allowed (jpg, jpeg, png, gif, webp, heic, heif)"));
            }

            // create upload directory - using absolute path
            Path uploadDir;
            if (uploadPath.startsWith("/")) {
                // absolute path
                uploadDir = Paths.get(uploadPath);
            } else {
                // convert the relative path to absolute apth
                uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
            }


            if (!Files.exists(uploadDir)) {
                System.out.println("Creating upload directory...");
                Files.createDirectories(uploadDir);
            }

            String filename = UUID.randomUUID() + extension;

            Path destPath = uploadDir.resolve(filename);

            // use Files.copy instead of transferTO
            Files.copy(file.getInputStream(), destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String serverUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String imageUrl = serverUrl + "/api/upload/image/" + filename;

            System.out.printf("""
                            [File Controller] File saved successfully.
                            \tUpload directory path: %s
                            \tGenerated filename: %s
                            \tGenerated full path: %s
                            \tImage URL: %s
                            """,
                    uploadDir.toAbsolutePath(), filename, destPath.toAbsolutePath(), imageUrl);

            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("filename", filename);
            // ------------------------------------------------

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("Error during file upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "File upload failed: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error during file upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        System.out.println("[File Controller] <GET /image/{filename:.+}> - " + filename);
        try {
            Path uploadDir;
            if (uploadPath.startsWith("/")) {
                // absolute path
                uploadDir = Paths.get(uploadPath);
            } else {
                // relative path to absolute path
                uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
            }
            Path filePath = uploadDir.resolve(filename).normalize();
            System.out.println("[GET /image/{filename:.+}] - " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                // 根据文件扩展名确定Content-Type
                String contentType = getContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<Integer> removeFile(@RequestBody String filename){
        System.out.println("[File Controller] Delete - " + filename);
        try {
            // assuming the file starts with prefix: "/api/upload/image"
            if (!filename.startsWith(filePrefix)) {
                return ResponseEntity.badRequest().body(-3);
            }
            String fileName = filename.substring(filePrefix.length());
            String rootDir = System.getProperty("user.dir");
            String fullPath = Paths.get(rootDir, uploadPath, fileName).toAbsolutePath().toString();

            System.out.printf("""
                [File Controller] request to remove URI: %s
                \tconverted to filepath: %s
                """, filename, fullPath);

            File targetFile = new File(fullPath);
            if (!targetFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            // delete the file
            if (targetFile.delete()) {
                System.out.println("[File Controller] successfully removed file: " + fullPath);
                return ResponseEntity.ok().body(0);
            }
            else {
                return ResponseEntity.badRequest().body(-2);
            }

        } catch (Error error) {
            error.printStackTrace();
            return ResponseEntity.badRequest().body(-1);
        }
    }

    private String getExtension(String filename) {
        return filename.lastIndexOf(".") != -1 ? filename.substring(filename.lastIndexOf(".")) : "";
    }

    private String getContentType(String filename) {
        String extension = getExtension(filename).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            case ".heic":
            case ".heif":
                // 对于HEIC文件，返回正确的MIME类型
                // 但浏览器可能仍然无法显示，建议用户转换为JPEG
                return "image/heic";
            default:
                return "application/octet-stream"; // 通用二进制文件类型
        }
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
                extension.equals(".png") || extension.equals(".gif") ||
                extension.equals(".webp") || extension.equals(".heic") ||
                extension.equals(".heif");
    }
}