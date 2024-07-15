package org.example.aws;

import org.example.model.CanonicalRequest;
import org.example.utils.FileUtils;
import org.example.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for generating AWS V4 signatures.
 * AWS V4 signatures are used to authenticate requests to AWS services.
 */
public class AWSV4SignatureGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AWSV4SignatureGenerator.class);
    private AWSV4SignatureBase signatureBase;


    /**
     * Default constructor.
     * Initializes the signature base with the region, service, access key, and secret key from the system properties.
     */
    public AWSV4SignatureGenerator() {

        this.signatureBase = new AWSV4SignatureBase(System.getProperty("S3_REGION"), "aws", System.getProperty("S3_ACCESS_KEY"), System.getProperty("S3_SECRET_KEY"));

    }


    /**
     * Constructor for testing purposes.
     * Initializes the signature base with the provided region, access key, and secret key.
     *
     * @param region    The AWS region.
     * @param accessKey The AWS access key.
     * @param secretKey The AWS secret key.
     */
    public AWSV4SignatureGenerator(String region, String accessKey, String secretKey) {
        this.signatureBase = new AWSV4SignatureBase(region, "s3", accessKey, secretKey);
    }

    /**
     * Creates a canonical request string from a CanonicalRequest object.
     *
     * @param canonicalRequest The CanonicalRequest object.
     * @return The canonical request string.
     */
    public String createCanonicalRequest(CanonicalRequest canonicalRequest) {
        return canonicalRequest.getMethod() + '\n' +
                canonicalRequest.getCanonicalUri() + '\n' +
                canonicalRequest.getCanonicalQueryString() + '\n' +
                RequestUtils.headersToString(canonicalRequest.getCanonicalHeaders()) + '\n' + '\n' +
                canonicalRequest.getSignedHeaders() + '\n' +
                canonicalRequest.getHashedPayload();
    }

    /**
     * Creates a string to sign from a canonical request string.
     *
     * @param canonicalRequest The canonical request string.
     * @return The string to sign.
     */
    public String createStringToSign(String canonicalRequest) {
        return AWSConstants.AWS4_SIGNING_ALGORITHM + '\n' +
                signatureBase.getFormattedDate() + '\n' +
                this.getScope() + '\n' +
                FileUtils.hash(canonicalRequest);
    }

    /**
     * Generates the scope for the signature.
     *
     * @return The scope string.
     */
    private String getScope() {
        return signatureBase.getFormattedDate().substring(0, 8) + '/' + System.getProperty("S3_REGION") + '/' + AWSConstants.AWS_SERVICE + '/' + AWSConstants.AWS4_REQUEST;
    }

    /**
     * Generates the signature from a string to sign.
     *
     * @param stringToSign The string to sign.
     * @return The signature.
     */
    public String getSignature(String stringToSign) {
        return signatureBase.sign(stringToSign, signatureBase.createSigningKey());
    }

    /**
     * Generates the authorization header from a CanonicalRequest object.
     *
     * @param canonicalRequest The CanonicalRequest object.
     * @return The authorization header.
     */
    public String getAuthorizationHeader(CanonicalRequest canonicalRequest) {
        return AWSConstants.AWS4_SIGNING_ALGORITHM + " " +
                "Credential=" + System.getProperty("S3_ACCESS_KEY") + "/" + this.getScope() + ", " +
                "SignedHeaders=" + canonicalRequest.getSignedHeaders() + ", " +
                "Signature=" + getSignature(createStringToSign(createCanonicalRequest(canonicalRequest)));
    }

    /**
     * Gets the formatted date from the signature base.
     *
     * @return The formatted date.
     */
    public String getDate() {
        return this.signatureBase.getFormattedDate();
    }


}
