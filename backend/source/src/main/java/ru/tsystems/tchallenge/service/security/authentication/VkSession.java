package ru.tsystems.tchallenge.service.security.authentication;

import lombok.Data;

@Data
public class VkSession {
    private Integer expire;
    private Integer mid;
    private String secret;
    private String sig;
    private String sid;
    private String userId;
    private String firstName;
    private String lastName;
}
