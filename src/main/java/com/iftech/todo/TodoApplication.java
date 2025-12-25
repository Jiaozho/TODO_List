package com.iftech.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoApplication {
    /**
     * 应用入口方法。
     *
     * <p>启动 Spring Boot，并完成组件扫描、依赖注入与 Web 容器初始化。
     *
     * @param args 启动参数（通常为空）
     */
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
