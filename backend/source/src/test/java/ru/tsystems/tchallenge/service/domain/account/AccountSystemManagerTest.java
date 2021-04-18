package ru.tsystems.tchallenge.service.domain.account;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;

import java.util.EnumSet;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AccountSystemManagerTest {
    @Mock
    private AccountConverter accountConverter;
    @Mock
    private AccountPasswordHashEngine accountPasswordHashEngine;
    @Mock
    private AccountPasswordValidator accountPasswordValidator;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private AccountSystemManager accountSystemManager;
    private AccountInvoice invoice;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        invoice = new AccountInvoice();
        invoice.setEmail("test@example.com");
        invoice.setCategory(AccountCategory.COWORKER);
        invoice.setRoles(EnumSet.of(AccountRole.REVIEWER));
        AccountPersonality personality = new AccountPersonality();
        personality.setQuickname("Test");
        invoice.setPersonality(personality);
    }

    @Test
    public void createWithInvalidRoles() {
        invoice.setCategory(AccountCategory.COWORKER);
        invoice.setRoles(EnumSet.of(AccountRole.REVIEWER, AccountRole.PARTICIPANT));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_ACC_CAT: Invalid set of roles for specified category");
        accountSystemManager.create(invoice);
    }

    @Test
    public void createEmailAlreadyExists() {
        when(accountRepository.findByEmailIgnoreCase(anyString())).thenReturn(AccountDocument.builder().build());
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_REG_EMAIL: Account with such email already exists");
        accountSystemManager.create(invoice);
    }
}
