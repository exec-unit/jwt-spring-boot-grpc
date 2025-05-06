package com.execunit.jwt.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class GrpcReflectionService {
    private static final Logger log = LoggerFactory.getLogger(GrpcReflectionService.class);
    private final ApplicationContext applicationContext;
    
    public Optional<Method> findGrpcMethod(String serviceName, String methodName) {
        log.debug("Finding gRPC method: {}/{}", serviceName, methodName);

        try {
            // Map gRPC service name to implementation class
            String beanName = mapServiceNameToBeanName(serviceName);
            log.debug("Mapped service name '{}' to bean name '{}'", serviceName, beanName);

            if (beanName == null) {
                log.debug("No bean name found for service: {}", serviceName);
                return Optional.empty();
            }

            Object serviceBean = applicationContext.getBean(beanName);
            Class<?> serviceClass = serviceBean.getClass();
            log.debug("Found service bean of class: {}", serviceClass.getName());

            // Convert first letter of method name to lowercase for camelCase matching
            String camelCaseMethodName = Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
            log.debug("Looking for method: {} or {}", camelCaseMethodName, methodName);

            for (Method method : serviceClass.getDeclaredMethods()) {
                if (method.getName().equals(camelCaseMethodName) || method.getName().equals(methodName)) {
                    log.debug("Found method: {}", method.getName());
                    return Optional.of(method);
                }
            }

            // If method not found, log all available methods for debugging
            log.debug("Available methods in {}:", serviceClass.getName());
            Arrays.stream(serviceClass.getDeclaredMethods())
                    .forEach(method -> log.debug("  - {}", method.getName()));

        } catch (Exception e) {
            log.error("Error finding gRPC method: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }

    private String mapServiceNameToBeanName(String serviceName) {
        log.debug("Mapping service name: {}", serviceName);

        // For other services, try to derive bean name from service name
        // This is a fallback and might not work for all services
        String[] parts = serviceName.split("\\.");
        if (parts.length > 0) {
            String simpleName = parts[parts.length - 1];
            // Convert first letter to lowercase and remove "Service" suffix if present
            String baseName = simpleName;
            if (baseName.endsWith("Service")) {
                baseName = baseName.substring(0, baseName.length() - "Service".length());
            }
            String derivedName = Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1) + "GrpcService";
            log.debug("Derived bean name '{}' from service name '{}'", derivedName, serviceName);
            return derivedName;
        }

        log.debug("No mapping found for service name: {}", serviceName);
        return null;
    }
}
