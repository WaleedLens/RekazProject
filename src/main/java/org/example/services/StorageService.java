package org.example.services;

import org.example.model.Blob;
import org.example.model.BlobDto;

/**
 * Service for local file storage operations.
 */
public interface StorageService {

    /**
     * Saves the provided blob data.
     *
     * @param blobDto The blob data transfer object containing the blob id and data.
     */
    void saveBlob(BlobDto blobDto);

    /**
     * Retrieves the blob data for the provided id.
     *
     * @param id The id of the blob.
     * @return The blob object containing the blob id and data.
     */
    Blob getBlob(String id);

}
