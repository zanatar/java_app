package ru.tsystems.tchallenge.service.security.authentication;

import lombok.Builder;
import org.apache.commons.codec.digest.DigestUtils;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType;

@Builder
public class VkVerifier {
    private String secretKey;

    void verifyVkAuth(VkSession vkSession) {
        String data = "expire=" + vkSession.getExpire() + "mid=" + vkSession.getMid()
                + "secret=" + vkSession.getSecret() + "sid=" + vkSession.getSid() + secretKey;
        String hashed = DigestUtils.md5Hex(data);
        if (!hashed.equals(vkSession.getSig())) {
            throw userDidNotSignIn(vkSession.getUserId());
        }
    }

    private OperationException userDidNotSignIn(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(OperationExceptionType.ERR_ACC_TOKEN)
                .description("User did not sign in")
                .attachment(id)
                .build();
    }
}
