package org.example.controllers;

import com.google.inject.Inject;
import com.mongodb.MongoWriteException;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.example.annontations.ApiEndpoint;
import org.example.exception.BlobNotFoundException;
import org.example.exception.DuplicateBlobException;
import org.example.exception.InvalidJsonException;
import org.example.exception.InvalidRequestException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.services.StorageService;
import org.example.utils.BlobValidator;
import org.example.utils.FileUtils;
import org.example.utils.ParsingUtils;
import org.example.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class StorageController {


    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    private final StorageService storageService;

    @Inject
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Endpoint for saving a blob.
     * Expects a POST request at path "/v1/blobs".
     * The request body should contain a JSON with 'id' and 'data' fields.
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
                    //  BlobValidator.isValidBase64(blobData.getData());
                    // BlobValidator.validateBlobData(blobData);
                    storageService.saveBlob(blobData);
                    exchange.setStatusCode(StatusCodes.CREATED);
                } catch (InvalidRequestException | InvalidJsonException e) {
                    handleInvalidRequestException(ex, e);
                } catch (Exception e) {
                    handleException(ex, e);
                }
            }, this::handleException);
        };
    }

    private void handleException(HttpServerExchange exchange, Exception e) {
        if (e instanceof MongoWriteException && e.getCause() instanceof DuplicateBlobException) {
            handleDuplicateBlobException(exchange, (DuplicateBlobException) e.getCause());
        } else {
            handleGeneralException(exchange, e);
        }
    }

    private void handleDuplicateBlobException(HttpServerExchange exchange, DuplicateBlobException e) {
        exchange.setStatusCode(StatusCodes.CONFLICT);
        RequestUtils.sendResponse(exchange, e.getMessage());
        logger.error("An error occurred: ", e);
    }

    private void handleGeneralException(HttpServerExchange exchange, Exception e) {
        exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
        RequestUtils.sendResponse(exchange, "An error occurred: " + e.getMessage());
        logger.error("An error occurred: ", e);
    }

    private void handleInvalidRequestException(HttpServerExchange exchange, Exception e) {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        RequestUtils.sendResponse(exchange, "Invalid request: " + e.getMessage());
        logger.error("Invalid request: ", e);
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


}
