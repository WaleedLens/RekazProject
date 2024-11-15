package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.FileUtils;

import java.sql.Timestamp;
import java.util.Base64;

public class Blob {
    @JsonProperty("id")
    private String id;
    @JsonProperty("data")
    private String data;
    @JsonProperty("size")
    private int size;
    @JsonProperty("createdAt")
    private Timestamp createdAt;

    // Add a default constructor
    public Blob() {
    }

    public Blob(String id, String data, int size) {
        this.id = id;
        this.data = data;
        this.size = size;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Blob(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public int getSize() {
        return size;
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return "Blob{" +
                "id='" + id + '\'' +
                ", data='" + data + '\'' +
                ", size=" + size +
                ", createdAt=" + createdAt +
                '}';
    }
}
