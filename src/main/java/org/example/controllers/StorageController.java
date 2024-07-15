package org.example.controllers;

import com.google.inject.Inject;
import com.mongodb.MongoWriteException;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import org.example.annontations.ApiEndpoint;
import org.example.exception.BlobNotFoundException;
import org.example.exception.DuplicateBlobException;
import org.example.exception.InvalidJsonException;
import org.example.exception.InvalidRequestException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.services.StorageService;
import org.example.utils.FileUtils;
import org.example.utils.ParsingUtils;
import org.example.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * This class is responsible for handling storage related requests.
 * It provides endpoints to save and retrieve blobs.
 */
public class StorageController {


    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    private final StorageService storageService;

    /**
     * Constructor for the StorageController.
     * Initializes the storageService with the provided StorageService.
     *
     * @param storageService The StorageService.
     */
    @Inject
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Endpoint for saving a blob.
     * Expects a POST request at path "/v1/blobs".
     * The request body should contain a JSON object with the following fields:
     * - id: The id of the blob.
     * - data: The data of the blob.
     *
     * @return HttpHandler for handling the save blob request.
     */
    @ApiEndpoint(method = "POST", path = "/v1/blobs")
    public HttpHandler saveBlob() {
        return exchange -> {
            exchange.getRequestReceiver().receiveFullBytes((ex, data) -> {
                try {
                    BlobDto blobData = ParsingUtils.parseJson(new ByteArrayInputStream(data), BlobDto.class);
                    blobData.setData(FileUtils.decodeBase64ToString(blobData.getData()));
                    storageService.saveBlob(blobData);
                    exchange.setStatusCode(StatusCodes.CREATED);
                } catch (DuplicateBlobException e) {
                    handleDuplicateBlobException(ex, e);
                } catch (InvalidRequestException | InvalidJsonException e) {
                    handleInvalidRequestException(ex, e);
                } catch (Exception e) {
                    handleException(ex, e);
                }
            }, this::handleException);
        };
    }


    /**
     * Endpoint for retrieving a blob.
     * Expects a GET request at path "/v1/blobs/{id}".
     * The 'id' path parameter should be the id of the blob to retrieve.
     *
     * @return HttpHandler for handling the get blob request.
     */
    @ApiEndpoint(method = "GET", path = "/v1/blobs/{id}")
    public HttpHandler getBlob() {
        return exchange -> {
            try {
                String id = exchange.getQueryParameters().get("id").getFirst();
                Blob blob = storageService.getBlob(id);

                blob.setData(FileUtils.encodeStringToBase64(blob.getData()));
                // Convert blob to JSON
                String blobJson = ParsingUtils.objectToJson(blob);
                // send blob data in response
                exchange.getResponseSender().send(blobJson);
                logger.info("Retrieved blob with id {} and content {} ", id, blob.toString());
            } catch (BlobNotFoundException e) {
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                exchange.getResponseSender().send(e.getMessage());
            }
        };
    }


    /**
     * Handles exceptions that occur during the execution of the save blob endpoint.
     *
     * @param exchange The HttpServerExchange.
     * @param e        The Exception.
     */
    private void handleException(HttpServerExchange exchange, Exception e) {
        if (e instanceof MongoWriteException && e.getCause() instanceof DuplicateBlobException) {
            handleDuplicateBlobException(exchange, (DuplicateBlobException) e.getCause());
        } else {

            handleGeneralException(exchange, e);
        }
    }

    /**
     * Handles DuplicateBlobException that occur during the execution of the save blob endpoint.
     *
     * @param exchange The HttpServerExchange.
     * @param e        The DuplicateBlobException.
     */
    private void handleDuplicateBlobException(HttpServerExchange exchange, DuplicateBlobException e) {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        RequestUtils.sendResponse(exchange, e.getMessage());
        logger.error("An error occurred: ", e);
    }

    /**
     * Handles general exceptions that occur during the execution of the save blob endpoint.
     *
     * @param exchange The HttpServerExchange.
     * @param e        The Exception.
     */
    private void handleGeneralException(HttpServerExchange exchange, Exception e) {
        exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
        RequestUtils.sendResponse(exchange, "An error occurred: " + e.getMessage());
        logger.error("An error occurred: ", e);
    }

    /**
     * Handles InvalidRequestException that occur during the execution of the save blob endpoint.
     *
     * @param exchange The HttpServerExchange.
     * @param e        The InvalidRequestException.
     */
    private void handleInvalidRequestException(HttpServerExchange exchange, Exception e) {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        RequestUtils.sendResponse(exchange, "Invalid request: " + e.getMessage());
        logger.error("Invalid request: ", e);
    }


}
