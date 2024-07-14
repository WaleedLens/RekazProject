package org.example.aws;

import org.example.utils.RequestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class AWS4SignatureBase {

    private String region;
    private String service;
    private String accessKey;
    private String secretKey;
    private String date;

    public AWS4SignatureBase(String region, String service, String accessKey, String secretKey) {
        this.region = region;
        this.service = service;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.date = RequestUtils.formatDate(new Date());

    }


    public String getFormattedDate() {
        return this.date;
    }

    public String createCanonicalRequest(String method, String canonicalUri, String canonicalQueryString, String canonicalHeaders, String signedHeaders, String hashedPayload) {
        return method + '\n' +
                canonicalUri + '\n' +
                canonicalQueryString + '\n' +
                canonicalHeaders + '\n' +
                signedHeaders + '\n' +
                hashedPayload;
    }

    public String createStringToSign(String canonicalRequest) {
        return AWSConstants.AWS4_SIGNING_ALGORITHM + '\n' +
                date + '\n' +
                date.substring(0, 8) + '/' + region + '/' + service + '/' + AWSConstants.AWS4_REQUEST + '\n' +
                hash(canonicalRequest);
    }

    private String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

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

    private byte[] hmacSHA256(byte[] key, String value) throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = AWSConstants.HMAC_ALGORITHM;
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    public String sign(String stringToSign, byte[] signingKey) {
        try {
            byte[] rawHmac = hmacSHA256(signingKey, stringToSign);
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}