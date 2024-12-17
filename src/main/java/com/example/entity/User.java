package com.example.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.IdGeneratorType;
import com.example.TimestampIdGenerator;

@Entity
@Table(name = "users") 
public class User {

    @Id 
    @GeneratedValue(generator = "timestampId") // 指定生成器名称
    @GenericGenerator(name = "timestampId", strategy = "com.example.TimestampIdGenerator") // 配置生成器
    @Column(name = "id", length = 30)
    // @GeneratedValue(strategy = GenerationType.UUID)
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;
    private String email;

    // Default constructor (JPA requirement)
    public User() {}

    // Constructor with fields
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
