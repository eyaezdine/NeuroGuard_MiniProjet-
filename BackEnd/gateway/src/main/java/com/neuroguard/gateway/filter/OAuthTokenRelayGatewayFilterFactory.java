package com.neuroguard.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom gateway filter factory to relay OAuth2 tokens from Keycloak to downstream services
 * Replaces the TokenRelay filter which is not available in Spring Cloud 2025.0.1
 */
@Component
public class OAuthTokenRelayGatewayFilterFactory extends AbstractGatewayFilterFactory<OAuthTokenRelayGatewayFilterFactory.Config> {

    public OAuthTokenRelayGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return exchange.getPrincipal()
                .filter(principal -> principal instanceof BearerTokenAuthentication)
                .map(principal -> (BearerTokenAuthentication) principal)
                .flatMap(auth -> {
                    String token = auth.getToken().getTokenValue();
                    ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build())
                        .build();
                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
        };
    }

    public static class Config {
        // Empty config class for the filter factory
    }
}

