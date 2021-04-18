package ru.tsystems.tchallenge.service.security.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.account.*;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType;
import ru.tsystems.tchallenge.service.security.registration.SecurityRegistration;
import ru.tsystems.tchallenge.service.security.registration.SecurityRegistrationManager;
import ru.tsystems.tchallenge.service.security.token.SecurityToken;
import ru.tsystems.tchallenge.service.security.token.TokenManager;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucher;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucherManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.UUID;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AuthenticationManagerTest {
    private static final String ACCOUNT_IS_MISSING = "ERR_INTERNAL: Account is missing";
    @Mock
    private AccountPasswordHashEngine accountPasswordHashEngine;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountSystemManager accountSystemManager;
    @Mock
    private SecurityVoucherManager securityVoucherManager;
    @Mock
    private TokenManager tokenManager;
    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock
    private VkVerifier vkVerifier;
    @Mock
    private SecurityRegistrationManager securityRegistrationManager;

    @InjectMocks
    AuthenticationManager authenticationManager;

    private String email = "test@example.com";
    private String accountId = UUID.randomUUID().toString();
    private Account account;
    private AuthenticationInvoice googleInvoice;
    private AuthenticationInvoice voucherInvoice;
    private GoogleIdToken idToken;
    private final String firstName = "John";
    private final String lastName = "Snow";
    private final String payload = UUID.randomUUID().toString();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AuthenticationInvoice passwordInvoice;
    private SecurityToken token;
    private AuthenticationInvoice vkInvoice;
    private final String vkId = "000000";

    @Before
    public void setUp() {
        prepareAccount();
        preparePasswordInvoice();
        prepareGoogleAuthData();
        prepareVoucher();
        prepareToken();
        prepareVkInvoice();
    }

    private void prepareAccount() {
        account = new Account();
        account.setId(accountId);
        account.setEmail(email);
        account.setRoles(EnumSet.of(AccountRole.PARTICIPANT));
        account.setStatus(AccountStatus.CREATED);
        account.setCategory(AccountCategory.PARTICIPANT);
        AccountPersonality personality = new AccountPersonality();
        personality.setQuickname("testUser");
        account.setPersonality(personality);
    }

    private void preparePasswordInvoice() {
        passwordInvoice = new AuthenticationInvoice();
        passwordInvoice.setMethod(AuthenticationMethod.PASSWORD);
        passwordInvoice.setEmail(email);
        passwordInvoice.setPassword("12345");
    }

    private void prepareGoogleAuthData() {
        googleInvoice = new AuthenticationInvoice();
        googleInvoice.setMethod(AuthenticationMethod.GOOGLE);
        googleInvoice.setEmail(email);
        googleInvoice.setGoogleIdToken("token");
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.set("given_name", firstName);
        payload.set("family_name", lastName);
        idToken = new GoogleIdToken(new GoogleIdToken.Header(), payload, new byte[0], new byte[0]);
    }

    private void prepareVoucher() {
        String voucherPayload = UUID.randomUUID().toString();
        voucherInvoice = new AuthenticationInvoice();
        voucherInvoice.setMethod(AuthenticationMethod.VOUCHER);
        voucherInvoice.setEmail(email);
        voucherInvoice.setVoucherPayload(voucherPayload);
        SecurityVoucher voucher = SecurityVoucher.builder()
                .accountEmail(email)
                .build();
        when(securityVoucherManager.utilizeByPayload(anyString())).thenReturn(voucher);
    }

    private void prepareToken() {
        token = SecurityToken.builder()
                .accountId(accountId)
                .validUntil(Instant.now().plus(1, ChronoUnit.HOURS))
                .payload(payload)
                .build();
    }

    private void prepareVkInvoice() {
        vkInvoice = new AuthenticationInvoice();
        vkInvoice.setMethod(AuthenticationMethod.VK);
        VkSession vkSession = new VkSession();
        vkSession.setUserId(vkId);
        vkSession.setFirstName(firstName);
        vkSession.setLastName(lastName);
        vkInvoice.setVkSession(vkSession);
    }

    @Test
    public void authenticateByGoogleCreateAccountIfNoSuchEmail() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(anyString())).thenReturn(idToken);
        when(accountSystemManager.findByEmail(email)).thenReturn(null);
        when(securityRegistrationManager.createWithGoogle(email, firstName, lastName))
                .thenReturn(SecurityRegistration.builder()
                        .id(accountId)
                        .build());
        when(accountSystemManager.findById(any())).thenReturn(account);
        UserAuthentication userAuthentication = authenticationManager.authenticateByGoogleToken(googleInvoice);
        verify(securityRegistrationManager).createWithGoogle(email, firstName, lastName);
        verify(accountSystemManager).findById(accountId);
        assertEquals(accountId, userAuthentication.getAccountId());
        assertTrue(userAuthentication.isAuthenticated());
        assertEquals(AuthenticationMethod.GOOGLE, userAuthentication.getMethod());
        assertEquals(email, userAuthentication.getAccountEmail());
    }

    @Test
    public void authenticateByGoogleDontCreateAccountIfEmailExists() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(anyString())).thenReturn(idToken);
        when(accountSystemManager.findByEmail(email)).thenReturn(account);
        UserAuthentication userAuthentication = authenticationManager.authenticateByGoogleToken(googleInvoice);
        verifyNoMoreInteractions(securityRegistrationManager);
        assertEquals(accountId, userAuthentication.getAccountId());
        assertTrue(userAuthentication.isAuthenticated());
        assertEquals(AuthenticationMethod.GOOGLE, userAuthentication.getMethod());
        assertEquals(email, userAuthentication.getAccountEmail());
    }

    @Test
    public void authenticateByGoogleIllegalAccountStatus() throws GeneralSecurityException, IOException {
        account.setStatus(AccountStatus.SUSPENDED);
        when(googleIdTokenVerifier.verify(anyString())).thenReturn(idToken);
        when(accountSystemManager.findByEmail(email)).thenReturn(account);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_ILLEGAL_STATUS.name());
        authenticationManager.authenticateByGoogleToken(googleInvoice);
        verifyNoMoreInteractions(securityRegistrationManager);
    }

    @Test
    public void authenticateByVoucherNoVoucher() {
        when(securityVoucherManager.utilizeByPayload(anyString())).thenReturn(null);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_VOUCHER.name());
        authenticationManager.authenticateByVoucher(voucherInvoice);
    }

    @Test
    public void authenticateByVoucherNoAccount() {
        when(accountSystemManager.findByEmail(email)).thenReturn(null);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ACCOUNT_IS_MISSING);
        authenticationManager.authenticateByVoucher(voucherInvoice);
    }

    @Test
    public void authenticateByVoucherIllegalAccountStatus() {
        account.setStatus(AccountStatus.SUSPENDED);
        when(accountSystemManager.findByEmail(email)).thenReturn(account);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_ILLEGAL_STATUS.name());
        authenticationManager.authenticateByVoucher(voucherInvoice);
    }

    @Test
    public void authenticateByVoucherVerifyAccount() {
        account.setStatus(AccountStatus.CREATED);
        when(accountSystemManager.findByEmail(email)).thenReturn(account);
        UserAuthentication userAuthentication = authenticationManager.authenticateByVoucher(voucherInvoice);
        verify(accountSystemManager).findByEmail(email);
        verify(accountSystemManager).verifyAccount(account.getId());
        assertEquals(accountId, userAuthentication.getAccountId());
        assertTrue(userAuthentication.isAuthenticated());
        assertEquals(AuthenticationMethod.VOUCHER, userAuthentication.getMethod());
        assertEquals(email, userAuthentication.getAccountEmail());
    }

    @Test
    public void authenticateByPasswordNoAccount() {
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(null);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_OR_PASS.name());
        authenticationManager.authenticateByPassword(passwordInvoice);
    }

    @Test
    public void authenticateByPasswordWrongPassword() {
        AccountDocument document = AccountDocument.builder()
                .status(AccountStatus.APPROVED)
                .email(email)
                .passwordHash("hash")
                .build();
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(document);
        when(accountPasswordHashEngine.match(anyString(), anyString())).thenReturn(false);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_OR_PASS.name());
        authenticationManager.authenticateByPassword(passwordInvoice);
    }

    @Test
    public void authenticateByPasswordIllegalAccountStatus() {
        AccountDocument document = AccountDocument.builder()
                .status(AccountStatus.SUSPENDED)
                .email(email)
                .passwordHash("hash")
                .build();
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(document);
        when(accountPasswordHashEngine.match(anyString(), anyString())).thenReturn(true);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_ILLEGAL_STATUS.name());
        authenticationManager.authenticateByPassword(passwordInvoice);
    }

    @Test
    public void authenticateByPasswordAccountNotVerified() {
        AccountDocument document = AccountDocument.builder()
                .status(AccountStatus.CREATED)
                .email(email)
                .passwordHash("hash")
                .build();
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(document);
        when(accountPasswordHashEngine.match(anyString(), anyString())).thenReturn(true);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_NEED_CONFIRMATION.name());
        authenticationManager.authenticateByPassword(passwordInvoice);
    }

    @Test
    public void authenticateByPassword(){
        AccountDocument document = AccountDocument.builder()
                .status(AccountStatus.APPROVED)
                .email(email)
                .passwordHash("hash")
                .build();
        document.setId(accountId);
        when(accountRepository.findByEmailIgnoreCase(email)).thenReturn(document);
        when(accountPasswordHashEngine.match(anyString(), anyString())).thenReturn(true);
        UserAuthentication userAuthentication = authenticationManager.authenticateByPassword(passwordInvoice);
        assertEquals(accountId, userAuthentication.getAccountId());
        assertEquals(AuthenticationMethod.PASSWORD, userAuthentication.getMethod());
        assertEquals(email, userAuthentication.getAccountEmail());
    }

    @Test
    public void authenticateByTokenMissingToken() {
        when(tokenManager.retrieveByPayload(payload)).thenReturn(null);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_TOKEN.name());
        authenticationManager.authenticateByToken(payload);
        verify(tokenManager).retrieveByPayload(payload);
    }

    @Test
    public void authenticateByTokenNoAccount() {
        when(tokenManager.retrieveByPayload(payload)).thenReturn(token);
        when(accountSystemManager.findById(anyString())).thenReturn(null);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ACCOUNT_IS_MISSING);
        authenticationManager.authenticateByToken(payload);
        verify(tokenManager).retrieveByPayload(payload);
        verify(accountSystemManager).findById(accountId);
    }

    @Test
    public void authenticateByTokenIllegalAccountStatus() {
        account.setStatus(AccountStatus.SUSPENDED);
        when(tokenManager.retrieveByPayload(payload)).thenReturn(token);
        when(accountSystemManager.findById(accountId)).thenReturn(account);
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(OperationExceptionType.ERR_ACC_ILLEGAL_STATUS.name());
        authenticationManager.authenticateByToken(payload);
        verify(tokenManager).retrieveByPayload(payload);
        verify(accountSystemManager).findById(accountId);
    }

    @Test
    public void authenticateByToken() {
        account.setStatus(AccountStatus.APPROVED);
        when(tokenManager.retrieveByPayload(payload)).thenReturn(token);
        when(accountSystemManager.findById(accountId)).thenReturn(account);
        UserAuthentication userAuthentication = authenticationManager.authenticateByToken(payload);
        verify(tokenManager).retrieveByPayload(payload);
        verify(accountSystemManager).findById(accountId);
        assertEquals(accountId, userAuthentication.getAccountId());
        assertTrue(userAuthentication.isAuthenticated());
        assertEquals(AuthenticationMethod.TOKEN, userAuthentication.getMethod());
        assertEquals(email, userAuthentication.getAccountEmail());
    }

    @Test
    public void authenticateByVkCreateNewAccount() {
        AuthenticationInvoice vkInvoice = new AuthenticationInvoice();
        vkInvoice.setMethod(AuthenticationMethod.VK);
        VkSession vkSession = new VkSession();
        String vkId = "000000";
        vkSession.setUserId(vkId);
        vkSession.setFirstName(firstName);
        vkSession.setLastName(lastName);
        vkInvoice.setVkSession(vkSession);
        when(accountSystemManager.findByVkId(vkId)).thenReturn(null);
        SecurityRegistration registration = SecurityRegistration.builder()
                .id(accountId)
                .build();
        when(securityRegistrationManager.createWithVK(anyString(), anyString(), anyString())).thenReturn(registration);
        when(accountSystemManager.findById(accountId)).thenReturn(account);
        UserAuthentication userAuthentication = authenticationManager.authenticateByVK(vkInvoice);
        verify(securityRegistrationManager).createWithVK(vkId, firstName, lastName);
        assertEquals(AuthenticationMethod.VK, userAuthentication.getMethod());
        assertEquals(accountId, userAuthentication.getAccountId());
        assertTrue(userAuthentication.isAuthenticated());
    }

    @Test
    public void authenticateByVkAccountExists() {
        when(accountSystemManager.findByVkId(vkId)).thenReturn(account);
        UserAuthentication userAuthentication = authenticationManager.authenticateByVK(vkInvoice);
        verifyNoMoreInteractions(securityRegistrationManager);
        assertEquals(AuthenticationMethod.VK, userAuthentication.getMethod());
        assertEquals(accountId, userAuthentication.getAccountId());
        assertTrue(userAuthentication.isAuthenticated());
    }
}
