package local;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.example.database.MongoDBClient;
import org.example.exception.FileAlreadyExistsException;
import org.example.exception.BlobNotFoundException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.services.LocalFileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LocalFileStorageServiceTest {
    private MongoDBClient mongoDBClient;
    private LocalFileStorageService localFileStorageService;

    @BeforeEach
    void setUp() {
        String relativePath = Paths.get("src", "test", "resources", "storage_files").toString();
        File directory = new File(relativePath);
        if (!directory.exists()) {
            directory.mkdirs(); // This will create the directory if it doesn't exist
        }
        System.setProperty("LOCAL_STORAGE_PATH", relativePath);
        mongoDBClient = Mockito.mock(MongoDBClient.class);
        localFileStorageService = new LocalFileStorageService(mongoDBClient);
    }

    @Test
    public void saveBlob_ShouldStoreBlobInLocalStorage_WhenBlobIsProvided() {
        BlobDto blobDto = new BlobDto("test", "Hello, Waleed:))!");
        localFileStorageService.saveBlob(blobDto);

        Path filePath = Path.of(System.getProperty("LOCAL_STORAGE_PATH"), blobDto.getId());
        assertTrue(Files.exists(filePath));
    }

    @Test
    public void saveBlob_ShouldThrowFileAlreadyExistsException_WhenBlobAlreadyExists() {
        BlobDto blobDto = new BlobDto("test", "Hello, Waleed:))!");
        localFileStorageService.saveBlob(blobDto);

        assertThrows(FileAlreadyExistsException.class, () -> localFileStorageService.saveBlob(blobDto));
    }

    @Test
    public void getBlob_ShouldReturnBlobWithCorrectData_WhenFileExists() {
        BlobDto blobDto = new BlobDto("test", "Hello, Waleed:))!");
        localFileStorageService.saveBlob(blobDto);

        Document mockDocument = new Document("id", "test")
                .append("size", blobDto.getData().length())
                .append("timestamp", new Date());
        FindIterable<Document> mockFindIterable = Mockito.mock(FindIterable.class);
        when(mockFindIterable.first()).thenReturn(mockDocument);
        when(mongoDBClient.findDocument(anyString(), any(Document.class))).thenReturn(mockFindIterable);

        Blob retrievedBlob = localFileStorageService.getBlob("test");
        assertEquals("test", retrievedBlob.getId());
        assertEquals("Hello, Waleed:))!", retrievedBlob.getData());
    }

    @Test
    public void getBlob_ShouldThrowBlobNotFoundException_WhenFileDoesNotExist() {
        assertThrows(BlobNotFoundException.class, () -> localFileStorageService.getBlob("nonexistent"));
    }

    @AfterEach
    void tearDown() {
        try {
            Files.walk(Path.of(System.getProperty("LOCAL_STORAGE_PATH")))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException("Failed to clean up test files", e);
        }
    }
}