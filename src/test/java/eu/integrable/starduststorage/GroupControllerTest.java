package eu.integrable.starduststorage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private BuildProperties buildProperties;


    final static String adminToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImFkbWluIjp0cnVlLCJ3cml0ZXIiOmZhbHNlLCJleHAiOjE2MjQ5MTk1MDF9.ZgYaAE0AvBRhbmQuBRY3l5wdPaZGw123LJWEVK2F2QM";

    private static HttpEntity getHttpEntity() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
        return new HttpEntity(httpHeaders);
    }

    @Test
    public void testIfTheReturnedVersionIsCorrect() throws Exception {

        ResponseEntity responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1/storage/info/getVersion",
                HttpMethod.GET,
                getHttpEntity(),
                String.class);

        assertEquals(responseEntity.getBody(), buildProperties.getVersion());
    }
}
