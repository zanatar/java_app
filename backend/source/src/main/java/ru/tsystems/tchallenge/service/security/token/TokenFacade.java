package ru.tsystems.tchallenge.service.security.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

@Component
public class TokenFacade{

    private TokenManager tokenManager;

    @Autowired
	public TokenFacade(TokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}

    public SecurityToken createForCurrentAccount(UserAuthentication authentication) {
        return tokenManager.create(authentication.getAccountId());
    }

    public SecurityToken retrieveCurrent(UserAuthentication authentication) {
        return tokenManager.retrieveByPayload(authentication.getTokenPayload());
    }

    public void deleteCurrent(UserAuthentication authentication) {
        tokenManager.deleteByPayload(authentication.getTokenPayload());
    }


}
