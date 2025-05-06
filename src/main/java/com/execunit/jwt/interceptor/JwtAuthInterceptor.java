package com.execunit.jwt.interceptor;

import com.execunit.jwt.config.JwtProperties;
import com.execunit.jwt.service.JwtAuthorizationService;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JwtAuthInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private final JwtProperties jwtProperties;
    private final JwtAuthorizationService authorizationService;

    public JwtAuthInterceptor(JwtAuthorizationService authorizationService, JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.authorizationService = authorizationService;
        log.debug("JwtAuthInterceptor initialized");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        MethodDescriptor<ReqT, RespT> methodDescriptor = call.getMethodDescriptor();
        String fullMethodName = methodDescriptor.getFullMethodName();
        log.debug("Intercepting call to: {}", fullMethodName);

        String[] parts = fullMethodName.split("/");
        if (parts.length != 2) {
            log.debug("Invalid method name format: {}", fullMethodName);
            return next.startCall(call, headers);
        }

        String serviceName = parts[0];
        String methodName = parts[1];
        log.debug("Service name: {}, Method name: {}", serviceName, methodName);

        // Check if the method requires JWT authentication
        boolean requiresJwt = authorizationService.requiresAuthentication(serviceName, methodName);

        if (requiresJwt) {
            log.debug("Method requires JWT authentication");

            // Check for JWT token in headers
            String headerField = jwtProperties.getHeaderField();
            String authHeader = headers.get(Metadata.Key.of(headerField, Metadata.ASCII_STRING_MARSHALLER));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("Missing or invalid {} header", headerField);
                call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid " + headerField + " header"), headers);
                return new ServerCall.Listener<>() {
                };
            }

            // Extract and validate the token
            String token = authHeader.substring(7);
            if (!authorizationService.validateToken(token)) {
                log.debug("JWT validation failed");
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT token"), headers);
                return new ServerCall.Listener<>() {
                };
            }

            log.debug("JWT token validated successfully");
        } else {
            log.debug("JWT not required for this method");
        }

        // Proceed with the call
        log.debug("Proceeding with call");
        return next.startCall(call, headers);
    }
}
