package org.example.services;

import org.example.model.Blob;
import org.example.model.BlobDto;

public interface StorageService {
    void saveBlob(BlobDto blobDto);
    Blob getBlob(String id);

}
