package com.example.AccountSystem.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.Socket;

@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.host}")
    private String redisHost;

    private RedisServer redisServer;
    private static final String REDIS_SERVER_MAX_MEMORY = "maxmemory 512M";

    @PostConstruct
    public void startRedis() {
        if(!isRedisRunning(redisPort)) {
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting(REDIS_SERVER_MAX_MEMORY)
                    .build();
            redisServer.start();
        }
    }

    // 포트 확인
    private boolean isRedisRunning(int redisPort) {
        try (Socket socket = new Socket(redisHost, redisPort)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @PreDestroy
    public void stopRedis() {
        if(redisServer != null) {
            redisServer.stop();
        }
    }

}
