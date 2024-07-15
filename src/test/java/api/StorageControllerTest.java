package api;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.example.controllers.StorageController;
import org.example.exception.BlobNotFoundException;
import org.example.exception.DuplicateBlobException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.services.StorageService;
import org.example.utils.FileUtils;
import org.example.utils.ParsingUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.mockito.ArgumentMatchers.any;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class StorageControllerTest {
    private static final Logger log = LoggerFactory.getLogger(StorageControllerTest.class);
    private Undertow server;
    private HttpClient client;
    private StorageService storageServiceMock;
    private StorageController storageController; // Controller under test
    private int serverPort;

    @BeforeEach
    void setUp() {
        storageServiceMock = Mockito.mock(StorageService.class);
        storageController = new StorageController(storageServiceMock);

        RoutingHandler routingHandler = Handlers.routing()
                .add(Methods.GET, "/v1/blobs/{id}", storageController.getBlob())
                .add(Methods.POST, "/v1/blobs", storageController.saveBlob());

        server = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(routingHandler)
                .build();
        server.start();
        serverPort = ((InetSocketAddress) server.getListenerInfo().get(0).getAddress()).getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testGetBlobEndpoint() throws Exception {

        String id = "testId";
        Blob blob = new Blob(id, "testData");
        when(storageServiceMock.getBlob(id)).thenReturn(blob);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + serverPort + "/v1/blobs/" + id))
                .GET()
                .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());


        assertEquals(200, response.statusCode());
        Blob responseBlob = ParsingUtils.parseJson(response.body(), Blob.class);
        assertEquals(id, responseBlob.getId());
        assertEquals("testData", FileUtils.decodeBase64ToString(responseBlob.getData()));
    }

    @Test
    void testSaveBlobEndpoint() throws Exception {
        // Arrange
        String id = "testId";
        String data = "testData";
        BlobDto blobDto = new BlobDto(id, data);
        doNothing().when(storageServiceMock).saveBlob(any(BlobDto.class));


        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + serverPort + "/v1/blobs"))
                .POST(HttpRequest.BodyPublishers.ofString(ParsingUtils.objectToJson(blobDto)))
                .header(Headers.CONTENT_TYPE.toString(), "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(201, response.statusCode());
    }

    @Test
    void testGetBlobEndpoint_NotFound() throws Exception {
        // Arrange
        String id = "nonexistentId";

        doThrow(new BlobNotFoundException(id)).when(storageServiceMock).getBlob(id);
        // Act
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + serverPort + "/v1/blobs/" + id))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(404, response.statusCode());
    }

    @Test
    void testSaveBlobEndpoint_AlreadyExists() throws Exception {
        // Arrange
        String id = "existingId";
        String data = "testData";
        BlobDto blobDto = new BlobDto(id, data);
        doThrow(new DuplicateBlobException("Blob already exists")).when(storageServiceMock).saveBlob(any(BlobDto.class));

        // Act
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + serverPort + "/v1/blobs"))
                .POST(HttpRequest.BodyPublishers.ofString(ParsingUtils.objectToJson(blobDto)))
                .header(Headers.CONTENT_TYPE.toString(), "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(400, response.statusCode());
    }

    @Test
    void testSaveBlobEndpoint_VerifyInteraction() throws Exception {
        // Arrange
        String id = "testId";
        String data = "testData";
        BlobDto blobDto = new BlobDto(id, data);
        doNothing().when(storageServiceMock).saveBlob(any(BlobDto.class));

        // Act
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + serverPort + "/v1/blobs"))
                .POST(HttpRequest.BodyPublishers.ofString(ParsingUtils.objectToJson(blobDto)))
                .header(Headers.CONTENT_TYPE.toString(), "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(201, response.statusCode());
        verify(storageServiceMock, times(1)).saveBlob(any(BlobDto.class));
    }
}