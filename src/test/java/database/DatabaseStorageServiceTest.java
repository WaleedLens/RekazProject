package database;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.example.database.MongoDBClient;
import org.example.exception.BlobNotFoundException;
import org.example.model.BlobDto;
import org.example.services.DatabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DatabaseStorageServiceTest {
    private MongoDBClient mongoDBClient;
    private DatabaseStorageService databaseStorageService;

    @BeforeEach
    public void setup() {
        mongoDBClient = Mockito.mock(MongoDBClient.class);
        databaseStorageService = new DatabaseStorageService(mongoDBClient);

        Document document = new Document("id", "id").append("data", "data").append("integerField", 1);
        FindIterable findIterable = mock(FindIterable.class);
        when(findIterable.first()).thenReturn(document);
        when(mongoDBClient.findDocument(anyString(), any(Document.class))).thenReturn(findIterable);

        doNothing().when(mongoDBClient).insertDocument(anyString(), any(Document.class));
    }

    @Test
    public void saveBlob_ShouldCallInsertDocument_WhenBlobIsProvided() {
        BlobDto blobDto = new BlobDto("id", "data");

        databaseStorageService.saveBlob(blobDto);

        verify(mongoDBClient).insertDocument(anyString(), any(Document.class));
    }

    @Test
    public void getBlob_ShouldReturnBlob_WhenIdExists() {
        String id = "id";
        String data = "data";
        Integer size = 10;
        Date timestamp = new Date();

        Document document = new Document("id", id).append("data", data).append("size", size).append("timestamp", timestamp);
        FindIterable findIterable = mock(FindIterable.class);
        when(findIterable.first()).thenReturn(document);
        when(mongoDBClient.findDocument(anyString(), any(Document.class))).thenReturn(findIterable);

        var blob = databaseStorageService.getBlob(id);

        assertEquals(id, blob.getId());
        assertEquals(data, blob.getData());
        assertEquals(size, blob.getSize());
        assertEquals(timestamp.getTime(), blob.getCreatedAt().getTime());
    }

    @Test
    public void getBlob_ShouldThrowBlobNotFoundException_WhenIdDoesNotExist() {
        String id = "nonexistentId";
        when(mongoDBClient.findDocument(anyString(), any(Document.class))).thenReturn(null);

        assertThrows(BlobNotFoundException.class, () -> databaseStorageService.getBlob(id));
    }
}