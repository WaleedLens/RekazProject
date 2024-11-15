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

/**
 * This class is responsible for interacting with AWS S3 service.
 * It provides methods to get and put objects to S3.
 */
public class S3Client {
    private static final Logger logger = LoggerFactory.getLogger(S3Client.class);

    private AWSV4SignatureGenerator signatureBuilder;
    private final Date date = new Date();

    public S3Client() {
        this.signatureBuilder = new AWSV4SignatureGenerator();
    }

    /**
     * Constructor for testing purposes.
     * Initializes the signature builder with the provided AWSV4SignatureGenerator.
     *
     * @param signatureBuilder The AWSV4SignatureGenerator.
     */
    public S3Client(AWSV4SignatureGenerator signatureBuilder) {
        this.signatureBuilder = new AWSV4SignatureGenerator();
    }

    /**
     * Executes the provided HTTP request.
     *
     * @param httpRequest The HTTP request to execute.
     * @return The HTTP response.
     */
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

    /**
     * Gets an object from S3.
     *
     * @param key The key of the object to get.
     * @return The object data as a string.
     */
    public String getObjectFromS3(String key) {
        String hashedPayload = getHashedPayload("");
        SortedMap<String, String> canonicalHeaders = getCanonicalHeaders(hashedPayload);

        CanonicalRequest canonicalRequest = new CanonicalRequest("GET", "/" + key, "", canonicalHeaders, getSignedHeaders(canonicalHeaders), hashedPayload);

        HttpGet httpGet = new HttpGet("https://" + System.getProperty("S3_BUCKET") + ".s3." + System.getProperty("S3_REGION") + ".amazonaws.com/" + key);
        setRequestHeaders(httpGet, canonicalHeaders, hashedPayload, canonicalRequest);

        CloseableHttpResponse httpResponse = executeRequest(httpGet);


        return handleResponse(httpResponse);
    }

    /**
     * Puts an object to S3.
     *
     * @param key  The key of the object to put.
     * @param data The data of the object to put.
     * @return The HTTP response.
     */
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

    /**
     * Gets the hashed payload from the provided base64 data.
     *
     * @param base64Data The base64 data to hash.
     * @return The hashed payload.
     */
    private String getHashedPayload(String base64Data) {
        try {
            return FileUtils.hashStringContent(base64Data);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing payload: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the canonical headers for the request.
     *
     * @param hashedPayload The hashed payload of the request.
     * @return The canonical headers.
     */
    private SortedMap<String, String> getCanonicalHeaders(String hashedPayload) {
        SortedMap<String, String> canonicalHeaders = new TreeMap<>();
        canonicalHeaders.put("host", System.getProperty("S3_BUCKET") + ".s3." + "amazonaws.com");
        canonicalHeaders.put("x-amz-date", RequestUtils.formatDate(date));
        canonicalHeaders.put("x-amz-content-sha256", hashedPayload);
        return canonicalHeaders;
    }

    /**
     * Gets the signed headers for the request.
     *
     * @param canonicalHeaders The canonical headers of the request.
     * @return The signed headers.
     */
    private String getSignedHeaders(SortedMap<String, String> canonicalHeaders) {
        return RequestUtils.generateSignedHeaders(canonicalHeaders);
    }

    /**
     * Sets the request headers for the provided HTTP request.
     *
     * @param httpRequest      The HTTP request to set the headers for.
     * @param canonicalHeaders The canonical headers of the request.
     * @param hashedPayload    The hashed payload of the request.
     * @param canonicalRequest The canonical request of the request.
     */
    private void setRequestHeaders(HttpUriRequestBase httpRequest, SortedMap<String, String> canonicalHeaders, String hashedPayload, CanonicalRequest canonicalRequest) {
        httpRequest.setHeader("Host", canonicalHeaders.get("host"));
        httpRequest.setHeader("Authorization", signatureBuilder.getAuthorizationHeader(canonicalRequest));
        httpRequest.setHeader(AWSConstants.X_AMZ_DATE, canonicalHeaders.get("x-amz-date"));
        httpRequest.setHeader(AWSConstants.X_AMZ_CONTENT_SHA256, hashedPayload);
    }

    /**
     * Handles the response from the HTTP request.
     *
     * @param httpResponse The HTTP response to handle.
     * @return The response body as a string.
     */
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