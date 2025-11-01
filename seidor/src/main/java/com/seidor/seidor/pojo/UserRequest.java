package com.seidor.seidor.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @JsonProperty("mail")
    @NotBlank
    private String mailBase64;

    @NotBlank
    private String category;

    @NotBlank
    private String subcategory;
}
