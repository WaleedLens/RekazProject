package aws;

import org.example.aws.AWSV4SignatureGenerator;
import org.example.model.CanonicalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.SortedMap;
import java.util.TreeMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AWSV4SignatureGeneratorTest {

    private AWSV4SignatureGenerator generator;
    private CanonicalRequest canonicalRequest;

    @BeforeEach
    void setUp() {
        generator = new AWSV4SignatureGenerator("eu-north-1", "myAccessKey", "mySecretKey");
        canonicalRequest = Mockito.mock(CanonicalRequest.class);
        when(canonicalRequest.getMethod()).thenReturn("GET");
        when(canonicalRequest.getCanonicalUri()).thenReturn("/");
        when(canonicalRequest.getCanonicalQueryString()).thenReturn("");
        SortedMap<String, String> headers = new TreeMap<>();
        headers.put("host", "s3.amazonaws.com");
        when(canonicalRequest.getCanonicalHeaders()).thenReturn(headers);
        when(canonicalRequest.getSignedHeaders()).thenReturn("host");
        when(canonicalRequest.getHashedPayload()).thenReturn("hashedPayload");
    }
    @Test
    void createCanonicalRequest_ShouldReturnExpectedFormat() {
        String result = generator.createCanonicalRequest(canonicalRequest);
        String expected = "GET\n/\n\nhost:s3.amazonaws.com\n\nhost\nhashedPayload";
        assertEquals(expected, result);
    }

    @Test
    void createStringToSign_ShouldStartWithAWS4SigningAlgorithm() {
        String canonicalRequest = generator.createCanonicalRequest(this.canonicalRequest);
        String result = generator.createStringToSign(canonicalRequest);
        assertTrue(result.startsWith("AWS4-HMAC-SHA256"));
    }

    @Test
    void getSignature_ShouldReturnExpectedLength() {
        String stringToSign = generator.createStringToSign(generator.createCanonicalRequest(canonicalRequest));
        String result = generator.getSignature(stringToSign);
        assertEquals(64, result.length());
    }

    @Test
    void getAuthorizationHeader_ShouldContainCredentialAndSignature() {
        String result = generator.getAuthorizationHeader(canonicalRequest);
        assertTrue(result.contains("Credential="));
        assertTrue(result.contains("Signature="));
    }
}