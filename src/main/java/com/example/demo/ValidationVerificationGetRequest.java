package com.example.demo;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ValidationVerificationGetRequest {

    @NotNull
    @Size(max = 3)
    private String first;

    @NotNull
    @Max(4)
    private int second;

}
