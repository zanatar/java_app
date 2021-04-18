package ru.tsystems.tchallenge.service.security.authentication;

/**
 * UserAuthentication method types.
 *
 * @author Ilia Gubarev
 */
public enum AuthenticationMethod {

    /**
     * User credentials must be sent in the body of the request.
     */
    PASSWORD,

    /**
     * Security token payload must be sent among query parameters.
     */
    TOKEN,

    /**
     * Voucher payload must be sent among query parameters.
     */
    VOUCHER,

    /**
     * Google Id token must be sent.
     */
    GOOGLE,

    /**
     * Vk session data must be sent.
     */
    VK
}
