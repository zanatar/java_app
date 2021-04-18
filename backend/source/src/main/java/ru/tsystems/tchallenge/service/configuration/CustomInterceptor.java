package ru.tsystems.tchallenge.service.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

@Component
public class CustomInterceptor implements ChannelInterceptor {
    private AuthenticationManager authenticationManager;

    @Autowired
    CustomInterceptor(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String tokenPayload = accessor.getFirstNativeHeader("Authorization")
                    .split(" ")[1];
            UserAuthentication user = this.authenticationManager.authenticateByToken(tokenPayload);
            accessor.setUser(user);
        }
        return message;
    }

}
