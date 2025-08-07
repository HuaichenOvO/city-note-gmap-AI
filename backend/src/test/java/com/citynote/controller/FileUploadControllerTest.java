package com.citynote.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private FileUploadController fileUploadController;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set up the upload path to use temporary directory
        ReflectionTestUtils.setField(fileUploadController, "uploadPath", tempDir.toString() + "/");
    }

    @Test
    void uploadImage_ValidImageFile_ReturnsSuccessResponse() throws IOException {
        // Arrange
        String originalFilename = "test-image.jpg";
        long fileSize = 1024L; // 1KB
        InputStream inputStream = new ByteArrayInputStream("test image content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("url"));
        assertTrue(response.getBody().containsKey("filename"));
        assertTrue(response.getBody().get("url").contains("http://localhost:8080/api/upload/image/"));
        assertTrue(response.getBody().get("filename").endsWith(".jpg"));
    }

    @Test
    void uploadImage_FileSizeExceeds50MB_ReturnsBadRequest() {
        // Arrange
        long fileSizeOver50MB = 51 * 1024 * 1024L; // 51MB
        when(multipartFile.getSize()).thenReturn(fileSizeOver50MB);
        when(multipartFile.getOriginalFilename()).thenReturn("large-file.jpg");

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File size exceeds 50MB limit", response.getBody().get("error"));
    }

    @Test
    void uploadImage_NullFilename_ReturnsBadRequest() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid filename", response.getBody().get("error"));
    }

    @Test
    void uploadImage_EmptyFilename_ReturnsBadRequest() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("");
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid filename", response.getBody().get("error"));
    }

    @Test
    void uploadImage_InvalidFileExtension_ReturnsBadRequest() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Only image files are allowed (jpg, jpeg, png, gif, webp, heic, heif)", response.getBody().get("error"));
    }

    @Test
    void uploadImage_ValidPngFile_ReturnsSuccessResponse() throws IOException {
        // Arrange
        String originalFilename = "test-image.png";
        long fileSize = 2048L;
        InputStream inputStream = new ByteArrayInputStream("png image content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("https");
        when(httpServletRequest.getServerName()).thenReturn("example.com");
        when(httpServletRequest.getServerPort()).thenReturn(443);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("url").contains("https://example.com:443/api/upload/image/"));
        assertTrue(response.getBody().get("filename").endsWith(".png"));
    }

    @Test
    void uploadImage_ValidGifFile_ReturnsSuccessResponse() throws IOException {
        // Arrange
        String originalFilename = "animated.gif";
        long fileSize = 512L;
        InputStream inputStream = new ByteArrayInputStream("gif content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("filename").endsWith(".gif"));
    }

    @Test
    void uploadImage_ValidWebpFile_ReturnsSuccessResponse() throws IOException {
        // Arrange
        String originalFilename = "image.webp";
        InputStream inputStream = new ByteArrayInputStream("webp content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("filename").endsWith(".webp"));
    }

    @Test
    void uploadImage_ValidHeicFile_ReturnsSuccessResponse() throws IOException {
        // Arrange
        String originalFilename = "photo.heic";
        InputStream inputStream = new ByteArrayInputStream("heic content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(2048L);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("filename").endsWith(".heic"));
    }

    @Test
    void uploadImage_ValidHeifFile_ReturnsSuccessResponse() throws IOException {
        // Arrange
        String originalFilename = "photo.heif";
        InputStream inputStream = new ByteArrayInputStream("heif content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(1536L);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("filename").endsWith(".heif"));
    }

    @Test
    void uploadImage_IOException_ReturnsInternalServerError() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO error"));

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("File upload failed"));
    }

    @Test
    void uploadImage_UnexpectedException_ReturnsInternalServerError() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream()).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Unexpected error"));
    }

    @Test
    void getImage_ExistingFile_ReturnsFileResource() throws IOException {
        // Arrange
        String filename = "test-image.jpg";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "test image content".getBytes());

        // Act
        ResponseEntity<Resource> response = fileUploadController.getImage(filename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().exists());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
    }

    @Test
    void getImage_NonExistingFile_ReturnsNotFound() {
        // Arrange
        String filename = "non-existing.jpg";

        // Act
        ResponseEntity<Resource> response = fileUploadController.getImage(filename);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getImage_PngFile_ReturnsCorrectContentType() throws IOException {
        // Arrange
        String filename = "test-image.png";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "png content".getBytes());

        // Act
        ResponseEntity<Resource> response = fileUploadController.getImage(filename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("image/png", response.getHeaders().getContentType().toString());
    }

    @Test
    void getImage_GifFile_ReturnsCorrectContentType() throws IOException {
        // Arrange
        String filename = "animated.gif";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "gif content".getBytes());

        // Act
        ResponseEntity<Resource> response = fileUploadController.getImage(filename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("image/gif", response.getHeaders().getContentType().toString());
    }

    @Test
    void getImage_WebpFile_ReturnsCorrectContentType() throws IOException {
        // Arrange
        String filename = "image.webp";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "webp content".getBytes());

        // Act
        ResponseEntity<Resource> response = fileUploadController.getImage(filename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("image/webp", response.getHeaders().getContentType().toString());
    }

    @Test
    void getImage_HeicFile_ReturnsCorrectContentType() throws IOException {
        // Arrange
        String filename = "photo.heic";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "heic content".getBytes());

        // Act
        ResponseEntity<Resource> response = fileUploadController.getImage(filename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("image/heic", response.getHeaders().getContentType().toString());
    }

    @Test
    void getImage_UnknownExtension_ReturnsOctetStream() throws IOException {
        // Arrange
        String filename = "file.unknown";
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, "unknown content".getBytes());

        // Act
        ResponseEntity<Resource> response = fileUploadController.getImage(filename);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/octet-stream", response.getHeaders().getContentType().toString());
    }

    @Test
    void uploadImage_WithAbsoluteUploadPath_CreatesCorrectDirectory() throws IOException {
        // Arrange
        Path absolutePath = tempDir.resolve("absolute-uploads");
        ReflectionTestUtils.setField(fileUploadController, "uploadPath", absolutePath.toString() + "/");

        String originalFilename = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Files.exists(absolutePath));
    }

    @Test
    void uploadImage_FileWithoutExtension_ReturnsBadRequest() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("filename_without_extension");
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Only image files are allowed (jpg, jpeg, png, gif, webp, heic, heif)", response.getBody().get("error"));
    }

    @Test
    void uploadImage_JpegExtension_ReturnsSuccessResponse() throws IOException {
        // Arrange
        String originalFilename = "test-image.jpeg";
        InputStream inputStream = new ByteArrayInputStream("jpeg content".getBytes());

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        when(httpServletRequest.getScheme()).thenReturn("http");
        when(httpServletRequest.getServerName()).thenReturn("localhost");
        when(httpServletRequest.getServerPort()).thenReturn(8080);

        // Act
        ResponseEntity<Map<String, String>> response = fileUploadController.uploadImage(multipartFile, httpServletRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("filename").endsWith(".jpeg"));
    }
}