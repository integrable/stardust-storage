package eu.integrable.storagemanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


@SpringBootTest
public class InfoControllerTest {

    final static String TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJrYW1pbCIsIm5hbWUiOiJLYW1pbCBTemV3YyIsImFkbWluIjpmYWxzZSwianRpIjoiODFkY2RmNDItNGE0MC00MmUzLWI4YmItZjRkZGVlZGM3NTRlIiwiaWF0IjoxNjIyOTcyOTI3LCJleHAiOjE2MjI5Nzc0NTZ9.ovBJW8yflaATRizvdSgk3AOtQ60W2TT3k4f_4jioVNY";

    @Test
    public void testIfTheReturnedVersionIsCorrect() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        httpHeaders.add("Authorization", TOKEN);
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8082/api/v1/info/getVersion",
                HttpMethod.GET,
                httpEntity,
                String.class
        );
    }
}
