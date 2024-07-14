package ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class FtpStorageServiceTest {
    private FakeFtpServer fakeFtpServer;
    private FtpStorageService ftpStorageService;

    @BeforeEach
    public void setup() throws IOException {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.setServerControlPort(0);
        fakeFtpServer.start();

        ftpStorageService = new FtpStorageService(new FTPServer("localhost", fakeFtpServer.getServerControlPort(), "user", "password"),new MongoDBClient());
    }

    @Test
    public void testSaveBlob() throws IOException {
        BlobDto blobDto = new BlobDto("test", "Hello, World!");
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
        BlobDto blobDto = new BlobDto("test", "Hello, World!");
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
        BlobDto blobDto = new BlobDto("test", "Hello, World!");
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
        fakeFtpServer.stop();
    }
}