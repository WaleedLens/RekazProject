package org.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.example.annontations.ApiEndpoint;
import org.example.exception.InvalidRequestException;
import org.example.model.Blob;
import org.example.model.BlobDto;
import org.example.services.StorageService;
import org.example.utils.ParsingUtils;
import org.example.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class StorageController {

    private final StorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

    public StorageController() {
        this.storageService = new StorageService();
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
                    storageService.saveBlob(blobData);
                    exchange.setStatusCode(StatusCodes.CREATED);
                    exchange.getResponseHeaders().put(Headers.LOCATION, "/blobs/" + blobData.getId());
                } catch (InvalidRequestException e) {
                    handleInvalidRequestException(ex, e);
                } catch (Exception e) {
                    handleException(ex, e);
                }
            }, this::handleException);
        };
    }

    private void handleException(HttpServerExchange exchange, Exception e) {
        exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
        RequestUtils.sendResponse(exchange, "An error occurred: " + e.getMessage());
        logger.error("An error occurred: ", e);
    }

    private void handleInvalidRequestException(HttpServerExchange exchange, InvalidRequestException e) {
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
            String id = exchange.getQueryParameters().get("id").getFirst();
            Blob blob = storageService.getBlob(id);
            if (blob == null) {
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                RequestUtils.sendResponse(exchange, "Blob not found");
                return;
            }
            RequestUtils.sendResponse(exchange, blob.getData());
        };
    }


}
