package com.example;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TimestampIdGenerator implements IdentifierGenerator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        // 使用 Instant 获取当前时间戳（毫秒级）
        // Instant now = Instant.now();

        // 将时间戳格式化为字符串 (例如：20241027143520123)
        // String timestamp = formatter.format(now);

        // 可选：添加 UUID 以增加唯一性 (如果时间戳精度不足以保证唯一性)
        String uuid = UUID.randomUUID().toString().substring(0, 8); // 取 UUID 的前 8 位

        System.out.println( uuid);
        return uuid; // 将时间戳和 UUID 组合
    }
}