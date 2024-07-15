package org.example.aws;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.model.CanonicalRequest;
import org.example.utils.FileUtils;
import org.example.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class S3Client {
    private static final Logger logger = LoggerFactory.getLogger(S3Client.class);

    private AWSV4SignatureGenerator signatureBuilder;
    private final Date date = new Date();

    public S3Client() {
        this.signatureBuilder = new AWSV4SignatureGenerator();
    }
    // For testing purposes, we will add a constructor that takes the AWSV4SignatureGenerator as an argument.
    public S3Client(AWSV4SignatureGenerator signatureBuilder) {
        this.signatureBuilder = new AWSV4SignatureGenerator();
    }


    private CloseableHttpResponse executeRequest(HttpUriRequestBase httpRequest) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpRequest);
            logger.info("Executed request: {}", httpRequest.getMethod());
        } catch (Exception e) {
            logger.error("Error executing request: {}", e.getMessage());
        }
        return response;
    }

    public String getObjectFromS3(String key) {
        String hashedPayload = getHashedPayload("");
        SortedMap<String, String> canonicalHeaders = getCanonicalHeaders(hashedPayload);

        CanonicalRequest canonicalRequest = new CanonicalRequest("GET", "/" + key, "", canonicalHeaders, getSignedHeaders(canonicalHeaders), hashedPayload);

        HttpGet httpGet = new HttpGet("https://" + System.getProperty("S3_BUCKET") + ".s3." + System.getProperty("S3_REGION") + ".amazonaws.com/" + key);
        setRequestHeaders(httpGet, canonicalHeaders, hashedPayload, canonicalRequest);

        CloseableHttpResponse httpResponse = executeRequest(httpGet);


        return handleResponse(httpResponse);
    }

    public HttpResponse putObjectToS3(String key, String data) {
        byte[] decodedData = data.getBytes();
        String hashedPayload = getHashedPayload(FileUtils.encodeStringToBase64(data));

        SortedMap<String, String> canonicalHeaders = getCanonicalHeaders(hashedPayload);

        CanonicalRequest canonicalRequest = new CanonicalRequest("PUT", "/" + key, "", canonicalHeaders, getSignedHeaders(canonicalHeaders), hashedPayload);

        HttpPut httpPut = new HttpPut("https://" + System.getProperty("S3_BUCKET") + ".s3." + System.getProperty("S3_REGION") + ".amazonaws.com/" + key);
        setRequestHeaders(httpPut, canonicalHeaders, hashedPayload, canonicalRequest);
        httpPut.setEntity(new ByteArrayEntity(decodedData, ContentType.APPLICATION_OCTET_STREAM));

        CloseableHttpResponse httpResponse = executeRequest(httpPut);

        handleResponse(httpResponse);

        return httpResponse;
    }

    private String getHashedPayload(String base64Data) {
        try {
            return FileUtils.hashStringContent(base64Data);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing payload: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private SortedMap<String, String> getCanonicalHeaders(String hashedPayload) {
        SortedMap<String, String> canonicalHeaders = new TreeMap<>();
        canonicalHeaders.put("host", System.getProperty("S3_BUCKET") + ".s3." + "amazonaws.com");
        canonicalHeaders.put("x-amz-date", RequestUtils.formatDate(date));
        canonicalHeaders.put("x-amz-content-sha256", hashedPayload);
        return canonicalHeaders;
    }

    private String getSignedHeaders(SortedMap<String, String> canonicalHeaders) {
        return RequestUtils.generateSignedHeaders(canonicalHeaders);
    }

    private void setRequestHeaders(HttpUriRequestBase httpRequest, SortedMap<String, String> canonicalHeaders, String hashedPayload, CanonicalRequest canonicalRequest) {
        httpRequest.setHeader("Host", canonicalHeaders.get("host"));
        httpRequest.setHeader("Authorization", signatureBuilder.getAuthorizationHeader(canonicalRequest));
        httpRequest.setHeader(AWSConstants.X_AMZ_DATE, canonicalHeaders.get("x-amz-date"));
        httpRequest.setHeader(AWSConstants.X_AMZ_CONTENT_SHA256, hashedPayload);
    }

    private String handleResponse(CloseableHttpResponse httpResponse) {
        logger.info("Response: {}", httpResponse.getCode());
        logger.info("Response: {}", httpResponse.getReasonPhrase());

        try {
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            logger.info("Response body: {}", responseBody);
            EntityUtils.consume(httpResponse.getEntity());
            return responseBody;
        } catch (IOException | ParseException e) {
            logger.error("Error handling response: {}", e.getMessage());
        }
        return "";
    }

}