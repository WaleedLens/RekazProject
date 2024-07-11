package org.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.example.annontations.ApiEndpoint;
import org.example.model.Blob;
import org.example.services.StorageService;
import org.example.utils.ParsingUtils;
import org.example.utils.RequestUtils;

import java.io.ByteArrayInputStream;

public class StorageController {
    private StorageService storageService;

    public StorageController(){
        this.storageService = new StorageService();
    }

    @ApiEndpoint(method = "POST", path = "/v1/blobs")
    public HttpHandler saveBlob(){
        return exchange -> {
            exchange.getRequestReceiver().receiveFullBytes((ex, data) -> {
                try {
                    Blob blobData = ParsingUtils.parseJson(new ByteArrayInputStream(data), Blob.class);
                    // Input validation
                    if (blobData.getId() == null || blobData.getData() == null) {
                        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                        RequestUtils.sendResponse(exchange, "Invalid JSON: id and data are required");
                        return;
                    }
                    storageService.saveBlob(blobData);
                    exchange.setStatusCode(StatusCodes.CREATED);
                    exchange.getResponseHeaders().put(Headers.LOCATION, "/blobs/" + blobData.getId());
                } catch (JsonProcessingException e) {
                    exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                    RequestUtils.sendResponse(exchange, "Invalid JSON: " + e.getMessage());
                } catch (Exception e) {
                    exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                    RequestUtils.sendResponse(exchange, "An error occurred: " + e.getMessage());
                }
            }, (ex, e) -> {
                exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
                RequestUtils.sendResponse(exchange, "An error occurred: " + e.getMessage());
            });
        };
    }
    @ApiEndpoint(method = "GET", path = "/v1/blobs/{id}")
    public HttpHandler getBlob(String id){
        return exchange -> {
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
