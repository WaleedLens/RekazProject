package org.example.services;

import org.example.exception.InvalidRequestException;
import org.example.model.Blob;
import org.example.utils.FileUtils;

import java.util.Base64;

public class StorageService {


    public void saveBlob(Blob blob){
        isValidBase64(blob.getData());

    }


    public Blob getBlob(String id){
        System.out.println("Getting blob with id: " + id);
        return null;
    }


    private void isValidBase64(String base64String) throws InvalidRequestException {
        try {
            Base64.getDecoder().decode(base64String);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid Base64 data", e);
        }
    }


}
