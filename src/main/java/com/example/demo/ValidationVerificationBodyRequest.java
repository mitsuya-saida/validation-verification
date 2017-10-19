package com.example.demo;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Builder
public class ValidationVerificationBodyRequest {

    @NotNull
    @Size(max = 3)
    private String str;
}
