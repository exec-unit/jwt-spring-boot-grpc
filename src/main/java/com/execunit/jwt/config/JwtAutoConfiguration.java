package com.execunit.jwt.config;

import com.execunit.jwt.interceptor.JwtAuthInterceptor;
import com.execunit.jwt.service.GrpcReflectionService;
import com.execunit.jwt.service.JwtAuthorizationService;
import com.execunit.jwt.service.JwtClaimsService;
import net.devh.boot.grpc.server.interceptor.GlobalServerInterceptorConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(GlobalServerInterceptorConfigurer.class)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JwtAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public JwtClaimsService jwtService(JwtProperties jwtProperties) throws Exception {
        log.debug("Creating JwtService bean");
        return new JwtClaimsService(jwtProperties.getPublicKeyPath().toFile());
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcReflectionService grpcReflectionService(ApplicationContext applicationContext) {
        log.debug("Creating GrpcReflectionService bean");
        return new GrpcReflectionService(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthorizationService jwtAuthenticationService(
            GrpcReflectionService grpcReflectionService,
            JwtClaimsService jwtClaimsService) {
        log.debug("Creating JwtAuthenticationService bean");
        return new JwtAuthorizationService(grpcReflectionService, jwtClaimsService);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalServerInterceptorConfigurer jwtInterceptorConfigurer(
            JwtAuthorizationService authenticationService, JwtProperties jwtProperties) {
        log.debug("Creating JWT interceptor configurer");
        JwtAuthInterceptor interceptor = new JwtAuthInterceptor(authenticationService, jwtProperties);
        return registry -> registry.add(interceptor);
    }
}