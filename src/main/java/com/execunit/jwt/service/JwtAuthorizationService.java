package com.execunit.jwt.service;

import com.execunit.jwt.annotation.JwtSecured;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthorizationService {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationService.class);

    private final GrpcReflectionService grpcReflectionService;
    private final JwtClaimsService jwtClaimsService;

    public boolean requiresAuthentication(String serviceName, String methodName) {
        log.debug("Checking if method requires authorization: {}/{}", serviceName, methodName);

        Optional<Method> grpcMethod = grpcReflectionService.findGrpcMethod(serviceName, methodName);

        if (grpcMethod.isPresent()) {
            Method method = grpcMethod.get();
            log.debug("Found method: {} in class: {}", method.getName(), method.getDeclaringClass().getName());

            boolean methodHasAnnotation = method.isAnnotationPresent(JwtSecured.class);
            boolean classHasAnnotation = method.getDeclaringClass().isAnnotationPresent(JwtSecured.class);
            boolean requiresJwt = methodHasAnnotation || classHasAnnotation;

            log.debug("Method has @JwtSecured: {}", methodHasAnnotation);
            log.debug("Class has @JwtSecured: {}", classHasAnnotation);
            log.debug("Requires JWT: {}", requiresJwt);

            return requiresJwt;
        } else {
            log.debug("Method not found: {}", methodName);
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            jwtClaimsService.validateToken(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}