package com.example;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

@RestController
@SpringBootApplication
public class Main {
private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /* 解决druid 日志报错：discard long time none received connection:xxx */
    static {
        System.setProperty("druid.mysql.usePingMethod","false");
    }

    /**
     * 主启动函数 
     */
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(Main.class);
        springApplication.run(args);
        LOGGER.info(">>> {}", Main.class.getSimpleName().toUpperCase() + " STARTING SUCCESS");
    }

}