package com.zhibiao.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智标云后端入口（模块化单体）。
 * 默认激活 profile：api,dev —— 提供 REST 接口（8080）；
 * 独立 worker 进程使用 SPRING_PROFILES_ACTIVE=api,worker 启动（端口 8081）。
 * Mapper 均标注 @Mapper，由 MyBatis-Plus starter 自动扫描，无需 @MapperScan。
 */
@SpringBootApplication
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}