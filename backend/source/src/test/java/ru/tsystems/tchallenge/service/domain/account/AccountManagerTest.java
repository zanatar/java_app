package ru.tsystems.tchallenge.service.domain.account;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AccountPasswordHashEngine.class})
public class AccountManagerTest {
    @Autowired
    private AccountPasswordHashEngine accountPasswordHashEngine;
    @Mock
    private AccountPasswordValidator accountPasswordValidator;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountConverter accountConverter;

    private AccountManager accountManager;

    private final String ACCOUNT_ID = UUID.randomUUID().toString();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        accountManager = new AccountManager(accountPasswordHashEngine, accountPasswordValidator,
                accountRepository, accountConverter);
    }

    @Test
    public void updateCurrentPasswordDoesntMatch() {
        UserAuthentication authentication = UserAuthentication.builder()
                .accountId(ACCOUNT_ID)
                .build();
        AccountPasswordUpdateInvoice invoice = AccountPasswordUpdateInvoice.builder()
                .current("123456")
                .desired("qwerty")
                .build();
        AccountDocument accountDocument = AccountDocument.builder()
                .passwordHash(accountPasswordHashEngine.hash("123123"))
                .build();
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountDocument));
        expectedException.expect(OperationException.class);
        accountManager.updateCurrentPassword(authentication, invoice);
        verify(accountRepository, never()).save(any());

    }

    @Test
    public void updateCurrentPassword() {
        UserAuthentication authentication = UserAuthentication.builder()
                .accountId(ACCOUNT_ID)
                .build();
        String desired = "123457";
        String current = "123456";
        AccountPasswordUpdateInvoice invoice = AccountPasswordUpdateInvoice.builder()
                .current(current)
                .desired(desired)
                .build();
        AccountDocument accountDocument = AccountDocument.builder()
                .passwordHash(accountPasswordHashEngine.hash(current))
                .build();
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountDocument));
        accountManager.updateCurrentPassword(authentication, invoice);
        verify(accountRepository).save(any());
    }
}
