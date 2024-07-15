package org.example.aws;

import org.example.utils.RequestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * This class provides the base for creating an AWS V4 Signature.
 * It includes methods for creating a canonical request, a string to sign,
 * hashing a string, converting bytes to hex, creating a signing key,
 * and signing a string with a given signing key.
 */
public class AWSV4SignatureBase {

    private String region;
    private String service;
    private String accessKey;
    private String secretKey;
    private String date;

    /**
     * Constructor for AWSV4SignatureBase.
     *
     * @param region AWS region
     * @param service AWS service
     * @param accessKey AWS access key
     * @param secretKey AWS secret key
     */
    public AWSV4SignatureBase(String region, String service, String accessKey, String secretKey) {
        this.region = region;
        this.service = service;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.date = RequestUtils.formatDate(new Date());
    }

    /**
     * Returns the formatted date.
     *
     * @return formatted date
     */
    public String getFormattedDate() {
        return this.date;
    }

    /**
     * Creates a canonical request.
     *
     * @param method HTTP method
     * @param canonicalUri Canonical URI
     * @param canonicalQueryString Canonical query string
     * @param canonicalHeaders Canonical headers
     * @param signedHeaders Signed headers
     * @param hashedPayload Hashed payload
     * @return canonical request
     */
    public String createCanonicalRequest(String method, String canonicalUri, String canonicalQueryString, String canonicalHeaders, String signedHeaders, String hashedPayload) {
        return method + '\n' +
                canonicalUri + '\n' +
                canonicalQueryString + '\n' +
                canonicalHeaders + '\n' +
                signedHeaders + '\n' +
                hashedPayload;
    }

    /**
     * Creates a string to sign.
     *
     * @param canonicalRequest Canonical request
     * @return string to sign
     */
    public String createStringToSign(String canonicalRequest) {
        return AWSConstants.AWS4_SIGNING_ALGORITHM + '\n' +
                date + '\n' +
                date.substring(0, 8) + '/' + region + '/' + service + '/' + AWSConstants.AWS4_REQUEST + '\n' +
                hash(canonicalRequest);
    }

    /**
     * Hashes a string using SHA-256.
     *
     * @param text String to hash
     * @return hashed string
     */
    public String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts bytes to hex.
     *
     * @param bytes Bytes to convert
     * @return hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Creates a signing key.
     *
     * @return signing key
     */
    public byte[] createSigningKey() {
        try {
            byte[] kSecret = ("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8);
            byte[] kDate = hmacSHA256(kSecret, date.substring(0, 8));
            byte[] kRegion = hmacSHA256(kDate, region);
            byte[] kService = hmacSHA256(kRegion, service);
            return hmacSHA256(kService, AWSConstants.AWS4_REQUEST);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a HMAC SHA-256 hash.
     *
     * @param key Key for HMAC
     * @param value String to hash
     * @return hashed bytes
     * @throws NoSuchAlgorithmException if HMAC-SHA256 is not available
     * @throws InvalidKeyException if the given key is inappropriate for initializing this MAC
     */
    public byte[] hmacSHA256(byte[] key, String value) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = AWSConstants.HMAC_ALGORITHM;
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Signs a string with a given signing key.
     *
     * @param stringToSign String to sign
     * @param signingKey Signing key
     * @return signed string
     */
    public String sign(String stringToSign, byte[] signingKey) {
        try {
            byte[] rawHmac = hmacSHA256(signingKey, stringToSign);
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}