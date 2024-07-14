package org.example.model;

import java.util.SortedMap;

public class CanonicalRequest {
    private String method;
    private String canonicalUri;
    private String canonicalQueryString;
    private SortedMap<String, String> canonicalHeaders;
    private String signedHeaders;
    private String hashedPayload;

    public CanonicalRequest(String method, String canonicalUri, String canonicalQueryString, SortedMap<String, String> canonicalHeaders, String signedHeaders, String hashedPayload) {
        this.method = method;
        this.canonicalUri = canonicalUri;
        this.canonicalQueryString = canonicalQueryString;
        this.canonicalHeaders = canonicalHeaders;
        this.signedHeaders = signedHeaders;
        this.hashedPayload = hashedPayload;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCanonicalUri() {
        return canonicalUri;
    }

    public void setCanonicalUri(String canonicalUri) {
        this.canonicalUri = canonicalUri;
    }

    public String getCanonicalQueryString() {
        return canonicalQueryString;
    }

    public void setCanonicalQueryString(String canonicalQueryString) {
        this.canonicalQueryString = canonicalQueryString;
    }

    public SortedMap<String, String> getCanonicalHeaders() {
        return canonicalHeaders;
    }

    public void setCanonicalHeaders(SortedMap<String, String> canonicalHeaders) {
        this.canonicalHeaders = canonicalHeaders;
    }

    public String getSignedHeaders() {
        return signedHeaders;
    }

    public void setSignedHeaders(String signedHeaders) {
        this.signedHeaders = signedHeaders;
    }

    public String getHashedPayload() {
        return hashedPayload;
    }

    public void setHashedPayload(String hashedPayload) {
        this.hashedPayload = hashedPayload;
    }



}
