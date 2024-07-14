package ftp;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import org.bson.Document;
import org.example.database.MongoDBClient;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.model.FTPServer;
import org.example.services.FtpStorageService;
import org.junit.jupiter.api.*;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mockito;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class FtpStorageServiceTest {
    private FakeFtpServer fakeFtpServer;
    private FtpStorageService ftpStorageService;
    private MongoDBClient mongoDBClient;

    @BeforeEach
    public void setup() throws IOException {
        setupFakeFtpServer();
        setupMongoDBClientMock();
        setupFtpStorageService();
    }

    private void setupMongoDBClientMock() {
        MongoClient mockMongoClient = Mockito.mock(MongoClient.class);
        MongoDatabase mockDatabase = Mockito.mock(MongoDatabase.class);
        MongoCollection mockCollection = Mockito.mock(MongoCollection.class);
        FindIterable mockFindIterable = Mockito.mock(FindIterable.class);
        Document mockDocument = Mockito.mock(Document.class);
        Mockito.when(mockMongoClient.getDatabase(Mockito.anyString())).thenReturn(mockDatabase);
        Mockito.when(mockDatabase.getCollection(Mockito.anyString())).thenReturn(mockCollection);
        Mockito.when(mockCollection.find(Mockito.any(Document.class))).thenReturn(mockFindIterable);
        Mockito.when(mockFindIterable.first()).thenReturn(mockDocument);
        Mockito.when(mockDocument.getInteger(Mockito.anyString())).thenReturn(1);
        Mockito.when(mockDocument.getDate(Mockito.anyString())).thenReturn(new java.util.Date());

        mongoDBClient = new MongoDBClient(mockMongoClient);
    }

    private void setupFtpStorageService() {
        ftpStorageService = new FtpStorageService(new FTPServer("localhost", fakeFtpServer.getServerControlPort(), "user", "password"), mongoDBClient);
    }

    private void setupFakeFtpServer() {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.setServerControlPort(0);
        fakeFtpServer.start();
    }

    @Test
    public void testSaveBlob() throws IOException {
        BlobDto blobDto = new BlobDto("test", "Hello, Waleed:))!");
        ftpStorageService.saveBlob(blobDto);

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect("localhost", fakeFtpServer.getServerControlPort());
        ftpClient.login("user", "password");

        FTPFile[] files = ftpClient.listFiles("/data");
        assertEquals(1, files.length);
        assertEquals("test", files[0].getName());

        ftpClient.disconnect();
    }


    @Test
    public void saveBlob_WhenCalled_StoresBlobOnServer() throws IOException {
        BlobDto blobDto = new BlobDto("test", "Hello, Waleed:))!");
        ftpStorageService.saveBlob(blobDto);

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect("localhost", fakeFtpServer.getServerControlPort());
        ftpClient.login("user", "password");

        FTPFile[] files = ftpClient.listFiles("/data");
        assertEquals(1, files.length);
        assertEquals("test", files[0].getName());

        ftpClient.disconnect();
    }

    @Test
    public void saveBlob_WhenCalledWithEmptyData_StoresEmptyFileOnServer() throws IOException {
        BlobDto blobDto = new BlobDto("test", "");
        ftpStorageService.saveBlob(blobDto);

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect("localhost", fakeFtpServer.getServerControlPort());
        ftpClient.login("user", "password");

        FTPFile[] files = ftpClient.listFiles("/data");
        assertEquals(1, files.length);
        assertEquals("test", files[0].getName());

        ftpClient.disconnect();
    }

    @Test
    public void getBlob_WhenFileExists_ReturnsBlobWithCorrectData() throws IOException {
        BlobDto blobDto = new BlobDto("test", "Hello, Waleed:))!");
        ftpStorageService.saveBlob(blobDto);

        Blob retrievedBlob = ftpStorageService.getBlob("test");
        assertEquals("test", retrievedBlob.getId());
        assertEquals("Hello, World!", retrievedBlob.getData());
    }

    @Test
    public void getBlob_WhenFileDoesNotExist_ThrowsException() {
        assertThrows(RuntimeException.class, () -> ftpStorageService.getBlob("nonexistent"));
    }

    @AfterEach
    public void teardown() {
        mongoDBClient.close();
        fakeFtpServer.stop();
    }
}