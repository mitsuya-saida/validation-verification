# このドキュメントについて

* リクエストパラメータでバリデーションエラーが発生する際に発生するExceptionの種類がなんか色々ありそうだったので調査してみた
* 今回調査したのは以下のケース
    * ①パスのパラメータ
    * ②引数で指定するケース
    * ③引数でDTOを指定するケース
    * ④引数で@RequestBodyをつけてDTOを指定するケース(JSON)
    

## 先に結論
調査したケースとExcpetionの対応は以下の表の通り  
引数でバリデーションするものはConstraingViolationExceptionになってそうでないものは下記に従うって言う感じだった

ケース|Exceptionの内容
---|---
①パスのパラメータ|ConstraingViolationException
②引数で指定するケース|ConstraingViolationException
③引数でDTOを指定するケース|BindException
④引数で@RequestBodyをつけてDTOを指定するケース(JSON)|MethodArgumentNotValidException

## 調査した内容
ExceptionをハンドリングするクラスとリクエストをさばくControllerを用意して、実際にどこでキャッチしているかをリクエストして調査した

#### ソース
[validation-verification]()

#### 調査する際に準備したクラス

###### Controllerの実装

```java
@Validated
@RestController
@RequestMapping("/v1/verify")
public class ValidationVerificationController {

    @PostMapping("/{path}")
    public String verify(@PathVariable(value = "path") @Valid @Size(max = 3) String path, // ①パスのパラメータ
                       @Valid @Size(max = 3) @NotNull String arg, // ②引数で指定するケース
                       @Valid ValidationVerificationGetRequest validationVerificationGetRequest, // ③引数でDTOを指定するケース
                       @RequestBody @Valid ValidationVerificationBodyRequest validationVerificationBodyRequest // ④引数で@RequestBodyをつけてDTOを指定するケース(JSON)
    ) {
        return "ok";
    }
}
```

###### ③でつかう引数で渡しているDTOの実装

```java
package com.example.demo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ValidationVerificationGetRequest {

    @Size(max = 3)
    @NotNull
    private String dto;
}

```

###### ④で使う引数で渡しているRequestBodyに渡すときのDTOの実装

```java
package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

@Getter
@Builder
public class ValidationVerificationBodyRequest {

    @NotBlank
    @Size(max = 3)
    private String str;

}

```

###### Exceptionをハンドリングするクラス

```java
package com.example.demo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (body == null) {
            body = ex.getClass().toString();
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, "MethodArgumentNotValid",headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, "BindException",headers, status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        return handleExceptionInternal(ex, "Constraint", null, HttpStatus.BAD_REQUEST, request);
    }

}


```

#### それぞれのテストコード

```java
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

}

```

