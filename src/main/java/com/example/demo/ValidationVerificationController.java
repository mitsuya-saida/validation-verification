package com.example.demo;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
