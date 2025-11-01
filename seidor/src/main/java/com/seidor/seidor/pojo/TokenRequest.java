package com.seidor.seidor.pojo;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class TokenRequest {

    @JsonProperty("mail")
    @NotBlank
    private String mailBase64;

    @NotBlank
    private String category;

    @NotBlank
    private String subcategory;
}
