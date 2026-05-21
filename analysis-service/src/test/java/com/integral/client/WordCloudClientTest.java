package com.integral.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import сom.integral.client.WordCloudClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest()
@ContextConfiguration(classes = WordCloudClient.class)
@TestPropertySource(properties = "app.external-api.word-cloud-url=http://fake-cloud-api.com/cloud")
class WordCloudClientTest {

    @Autowired
    private WordCloudClient wordCloudClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        org.springframework.web.client.RestTemplate internalTemplate =
                (org.springframework.web.client.RestTemplate) ReflectionTestUtils.getField(wordCloudClient, "restTemplate");

        mockServer = MockRestServiceServer.createServer(internalTemplate);
    }

    @Test
    void generateWordCloud_Success() {

        String textToAnalyze = "java spring kotlin docker rabbitmq";
        byte[] expectedBytes = new byte[]{1, 2, 3, 4, 5};
        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith("http://fake-cloud-api.com/cloud")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(requestTo(org.hamcrest.Matchers.containsString("text=java")))
                .andExpect(requestTo(org.hamcrest.Matchers.containsString("width=500")))
                .andExpect(requestTo(org.hamcrest.Matchers.containsString("height=500")))
                .andRespond(withSuccess(expectedBytes, MediaType.APPLICATION_OCTET_STREAM));

        byte[] actualBytes = wordCloudClient.generateWordCloud(textToAnalyze);


        assertNotNull(actualBytes);
        assertArrayEquals(expectedBytes, actualBytes);
        mockServer.verify();
    }

    @Test
    void generateWordCloud_Exception_ReturnsNull() {
        String textToAnalyze = "error text";

        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith("http://fake-cloud-api.com/cloud")))
                .andRespond(withServerError());

        byte[] actualBytes = wordCloudClient.generateWordCloud(textToAnalyze);

        assertNull(actualBytes);
        mockServer.verify();
    }
}