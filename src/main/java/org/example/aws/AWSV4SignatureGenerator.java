package org.example.aws;

import org.example.model.CanonicalRequest;
import org.example.utils.FileUtils;
import org.example.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AWSV4SignatureGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AWSV4SignatureGenerator.class);
    private AWSV4SignatureBase signatureBase;

    public AWSV4SignatureGenerator() {

        this.signatureBase = new AWSV4SignatureBase(System.getProperty("S3_REGION"), "aws", System.getProperty("S3_ACCESS_KEY"), System.getProperty("S3_SECRET_KEY"));

    }

    /*
    For testing purposes, we will add a constructor that takes the region, access key, and secret key as arguments.
     */

    public AWSV4SignatureGenerator(String region, String accessKey, String secretKey) {
        this.signatureBase = new AWSV4SignatureBase(region, "s3", accessKey, secretKey);
    }


    public String createCanonicalRequest(CanonicalRequest canonicalRequest) {
        return canonicalRequest.getMethod() + '\n' +
                canonicalRequest.getCanonicalUri() + '\n' +
                canonicalRequest.getCanonicalQueryString() + '\n' +
                RequestUtils.headersToString(canonicalRequest.getCanonicalHeaders()) + '\n' + '\n' +
                canonicalRequest.getSignedHeaders() + '\n' +
                canonicalRequest.getHashedPayload();
    }

    public String createStringToSign(String canonicalRequest) {
        return AWSConstants.AWS4_SIGNING_ALGORITHM + '\n' +
                signatureBase.getFormattedDate() + '\n' +
                this.getScope() + '\n' +
                FileUtils.hash(canonicalRequest);
    }

    private String getScope() {
        return signatureBase.getFormattedDate().substring(0, 8) + '/' + System.getProperty("S3_REGION") + '/' + AWSConstants.AWS_SERVICE + '/' + AWSConstants.AWS4_REQUEST;
    }


    public String getSignature(String stringToSign) {
        return signatureBase.sign(stringToSign, signatureBase.createSigningKey());
    }

    public String getAuthorizationHeader(CanonicalRequest canonicalRequest) {
        return AWSConstants.AWS4_SIGNING_ALGORITHM + " " +
                "Credential=" + System.getProperty("S3_ACCESS_KEY") + "/" + this.getScope() + ", " +
                "SignedHeaders=" + canonicalRequest.getSignedHeaders() + ", " +
                "Signature=" + getSignature(createStringToSign(createCanonicalRequest(canonicalRequest)));
    }

    public String getDate() {
        return this.signatureBase.getFormattedDate();
    }


}
