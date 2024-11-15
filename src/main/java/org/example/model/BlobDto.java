package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlobDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("data")
    private String data;

    public BlobDto() {
    }

    public BlobDto(String id, String data) {
        this.id = id;
        this.data = data;
    }

    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}