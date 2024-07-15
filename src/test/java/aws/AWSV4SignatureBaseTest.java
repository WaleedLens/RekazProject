package aws;


import org.example.aws.AWSV4SignatureBase;
import org.example.utils.RequestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class AWSV4SignatureBaseTest {

    private AWSV4SignatureBase aws4SignatureBase;
    private String date;
    private String canonicalRequest;

    @BeforeEach
    void setUp() {
        aws4SignatureBase = new AWSV4SignatureBase("us-west-2", "s3", "accessKey", "secretKey");
        date = RequestUtils.formatDate(new Date());
        canonicalRequest = "GET\n/\n\nhost:s3.amazonaws.com\nhost\nhashedPayload";
    }

    @Test
    void createCanonicalRequest_ShouldReturnExpectedFormat() {
        String canonicalRequest = aws4SignatureBase.createCanonicalRequest("GET", "/", "", "host:s3.amazonaws.com", "host", "hashedPayload");
        String expected = "GET\n/\n\nhost:s3.amazonaws.com\nhost\nhashedPayload";
        assertEquals(expected, canonicalRequest);
    }

    @Test
    void createStringToSign_ShouldReturnExpectedFormat() {
        String stringToSign = aws4SignatureBase.createStringToSign(canonicalRequest);
        String expectedStart = "AWS4-HMAC-SHA256\n" + date + "\n" + date.substring(0, 8) + "/us-west-2/s3/aws4_request\n";
        assertTrue(stringToSign.startsWith(expectedStart));
    }

    @Test
    void createSigningKey_ShouldReturnExpectedLength() throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] signingKey = aws4SignatureBase.createSigningKey();
        assertEquals(32, signingKey.length); // SHA-256 HMAC results in a 32-byte key
    }

    @Test
    void createSigningKey_ShouldThrowRuntimeException_WhenNoSuchAlgorithmException() throws NoSuchAlgorithmException, InvalidKeyException {
        AWSV4SignatureBase aws4SignatureBaseSpy = Mockito.spy(aws4SignatureBase);
        doThrow(new NoSuchAlgorithmException()).when(aws4SignatureBaseSpy).hmacSHA256(any(byte[].class), any(String.class));
        assertThrows(RuntimeException.class, aws4SignatureBaseSpy::createSigningKey);
    }

    @Test
    void createSigningKey_ShouldThrowRuntimeException_WhenInvalidKeyException() throws NoSuchAlgorithmException, InvalidKeyException {
        AWSV4SignatureBase aws4SignatureBaseSpy = Mockito.spy(aws4SignatureBase);
        doThrow(new InvalidKeyException()).when(aws4SignatureBaseSpy).hmacSHA256(any(byte[].class), any(String.class));
        assertThrows(RuntimeException.class, aws4SignatureBaseSpy::createSigningKey);
    }

    @Test
    void sign_ShouldReturnExpectedLength() throws NoSuchAlgorithmException, InvalidKeyException {
        String stringToSign = "AWS4-HMAC-SHA256\n" + date + "\n" + date.substring(0, 8) + "/us-west-2/s3/aws4_request\n" + aws4SignatureBase.hash(canonicalRequest);
        byte[] signingKey = aws4SignatureBase.createSigningKey();
        String signature = aws4SignatureBase.sign(stringToSign, signingKey);
        assertEquals(64, signature.length()); // SHA-256 results in a 64-character string
    }

    @Test
    void sign_ShouldThrowRuntimeException_WhenNoSuchAlgorithmException() throws NoSuchAlgorithmException, InvalidKeyException {
        AWSV4SignatureBase aws4SignatureBaseSpy = Mockito.spy(aws4SignatureBase);
        doThrow(new NoSuchAlgorithmException()).when(aws4SignatureBaseSpy).hmacSHA256(any(byte[].class), any(String.class));
        assertThrows(RuntimeException.class, () -> aws4SignatureBaseSpy.sign("stringToSign", new byte[32]));
    }

    @Test
    void sign_ShouldThrowRuntimeException_WhenInvalidKeyException() throws NoSuchAlgorithmException, InvalidKeyException {
        AWSV4SignatureBase aws4SignatureBaseSpy = Mockito.spy(aws4SignatureBase);
        doThrow(new InvalidKeyException()).when(aws4SignatureBaseSpy).hmacSHA256(any(byte[].class), any(String.class));
        assertThrows(RuntimeException.class, () -> aws4SignatureBaseSpy.sign("stringToSign", new byte[32]));
    }
}