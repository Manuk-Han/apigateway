package com.example.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

@Profile({"8081", "8082"})
@Slf4j
@Component
public class IpAddressFilter implements WebFilter {
    private static final Set<String> ALLOWED_IPS = Set.of(
            "127.0.0.1",
            "::1",
            "0:0:0:0:0:0:0:1",
            "192.168.50.46"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        log.info("요청 들어온 IP: {}", ip);

        if (ALLOWED_IPS.contains(ip)) {
            return chain.filter(exchange);
        } else {
            log.warn("허용되지 않은 IP 접근 시도: {}", ip);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
    }

}
