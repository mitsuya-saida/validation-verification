package com.example.demo;

import net.minidev.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ValidationVerifiationControllerFunctionalTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void 正常系() {
        String body = "{\"str\":\"123\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity actual = testRestTemplate.exchange(
                "/v1/verify/all/123?first=123&second=1&arg=123", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        assertThat(actual.getBody().toString(), is("ok"));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void パスでバリデーションエラー() {
        String body = "{\"str\":\"123\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity actual = testRestTemplate.exchange(
                "/v1/verify/all/1234?first=123&second=1&arg=123", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        assertThat(actual.getBody().toString(), is("ConstraintViolationException"));
        assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void 引数でバリデーションエラー() {
        String body = "{\"str\":\"123\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity actual = testRestTemplate.exchange(
                "/v1/verify/all/123?first=123&second=1&arg=1234", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        assertThat(actual.getBody().toString(), is("ConstraintViolationException"));
        assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void DTOでバリデーションエラー() {
        String body = "{\"str\":\"123\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity actual = testRestTemplate.exchange(
                "/v1/verify/all/123?first=1234&second=1&arg=123", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        assertThat(actual.getBody().toString(), is("BindException"));
        assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void Bodyでバリデーションエラー() {
        String body = "{\"str\":\"1234\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity actual = testRestTemplate.exchange(
                "/v1/verify/all/123?first=123&second=1&arg=123", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        assertThat(actual.getBody().toString(), is("MethodArgumentNotValidException"));
        assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    public void 複数発生_DTOとパス() {
        String body = "{\"str\":\"1234\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity actual = testRestTemplate.exchange(
                "/v1/verify/all/123?first=123&second=1&arg=123", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        assertThat(actual.getBody().toString(), is("MethodArgumentNotValidException"));
        assertThat(actual.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

}
