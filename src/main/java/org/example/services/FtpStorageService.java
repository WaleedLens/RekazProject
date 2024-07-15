package org.example.services;

import com.google.inject.Inject;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.bson.Document;
import org.example.database.MongoDBClient;
import org.example.model.Blob;
import org.example.model.BlobDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import org.apache.commons.net.ftp.FTP;
import org.example.model.FTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for managing the FTP storage service.
 * It provides methods to save blobs, get blobs, and connect to the FTP server.
 */
public class FtpStorageService implements StorageService {
    private static final Logger log = LoggerFactory.getLogger(FtpStorageService.class);
    private FTPClient ftpClient;
    private final MongoDBClient mongoDBClient;

    /**
     * Constructor for the FtpStorageService.
     * Initializes the mongoDBClient with the provided MongoDBClient, and connects to the FTP server.
     *
     * @param mongoDBClient The MongoDBClient.
     */
    @Inject
    public FtpStorageService(MongoDBClient mongoDBClient) {
        FTPServer ftpServer = new FTPServer(System.getProperty("FTP_HOST"), Integer.parseInt(System.getProperty("FTP_PORT")), System.getProperty("FTP_USER"), System.getProperty("FTP_PASSWORD"));
        connectFtpServer(ftpServer);
        this.mongoDBClient = mongoDBClient;
    }

    /**
     * Constructor for the FtpStorageService for testing.
     * Initializes the mongoDBClient with the provided MongoDBClient, and connects to the provided FTP server.
     *
     * @param ftpServer     The FTPServer.
     * @param mongoDBClient The MongoDBClient.
     */
    public FtpStorageService(FTPServer ftpServer, MongoDBClient mongoDBClient) {
        connectFtpServer(ftpServer);
        this.mongoDBClient = mongoDBClient;
    }

    /**
     * Saves a blob to the FTP server and inserts its metadata into the "metadata" collection.
     *
     * @param blobDto The blob to save.
     */
    @Override
    public void saveBlob(BlobDto blobDto) {
        String fileName = blobDto.getId();
        String data = blobDto.getData();

        try (InputStream inputStream = new ByteArrayInputStream(data.getBytes())) {
            boolean done = ftpClient.storeFile(fileName, inputStream);
            if (done) {
                Blob blob = new Blob(blobDto.getId(), blobDto.getData(), blobDto.getData().length());
                mongoDBClient.insertMetadata(blob);
                log.info("File is uploaded successfully.");

            } else {
                log.error("Failed to upload file.");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error uploading file to the FTP Server", ex);
        }
    }

    /**
     * Retrieves a blob from the FTP server and its metadata from the "metadata" collection.
     *
     * @param id The id of the blob to retrieve.
     * @return The retrieved blob.
     */
    @Override
    public Blob getBlob(String id) {
        try (InputStream inputStream = ftpClient.retrieveFileStream(id)) {
            if (inputStream == null) {
                throw new RuntimeException("File not found on the server.");
            }
            String data = new String(inputStream.readAllBytes());
            ftpClient.completePendingCommand();
            Document metadataDocument = mongoDBClient.findDocument("metadata", new Document("id", id)).first();
            Blob blob = new Blob(id, data);

            if (metadataDocument != null) {
                blob.setSize(metadataDocument.getInteger("size"));
                blob.setCreatedAt(new Timestamp(metadataDocument.getDate("timestamp").getTime()));
            }
            return blob;
        } catch (IOException ex) {
            throw new RuntimeException("Error retrieving file from the FTP Server", ex);
        }
    }

    /**
     * Connects to the provided FTP server.
     *
     * @param ftpServer The FTPServer to connect to.
     */
    private void connectFtpServer(FTPServer ftpServer) {
        String server = ftpServer.getHost();
        int port = ftpServer.getPort();
        String user = ftpServer.getUser();
        String pass = ftpServer.getPassword();

        ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                throw new RuntimeException("FTP server refused connection.");
            }
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.getReplyCode();

            log.info("Connected to the FTP Server");
        } catch (IOException ex) {
            throw new RuntimeException("Error connecting to the FTP Server", ex);
        }
    }
}