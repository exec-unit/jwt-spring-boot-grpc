package com.execunit.jwt.config;

import com.execunit.jwt.validation.PublicKeyFileValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "jwt-grpc-auth")
@Getter
@Setter
@Validated
public class JwtProperties {
    
    @NotNull(message = "Public key path must not be null")
    @PublicKeyFileValidator
    private Path publicKeyPath;

    @NotBlank(message = "Header field name must not be blank")
    private String headerField = "authorization";

}
