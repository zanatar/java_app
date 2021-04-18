package ru.tsystems.tchallenge.service.domain.account.management;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tsystems.tchallenge.service.domain.account.*;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucherFacade;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailManager;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC;

@RunWith(SpringRunner.class)
public class UserManagementManagerTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountConverter accountConverter;
    @Mock
    private AccountSystemManager accountSystemManager;
    @Mock
    private SecurityVoucherFacade securityVoucherFacade;
    @Mock
    private TemplateMailManager templateMailManager;
    @Mock
    private MongoTemplate mongoTemplate;
    @InjectMocks
    private UserManagementManager userManagementManager;

    private AccountDocument accountDocument;
    private AccountUpdateInvoice updateInvoice;
    private static final String ACCOUNT_ID = UUID.randomUUID().toString();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        accountDocument = AccountDocument.builder()
                .email("email@example.com")
                .category(AccountCategory.PARTICIPANT)
                .roles(Collections.singleton(AccountRole.PARTICIPANT))
                .personality(AccountPersonalityDocument
                        .builder()
                        .firstname("Test")
                        .quickname("Test")
                        .build())
                .participantPersonality(ParticipantPersonalityDocument
                        .builder()
                        .build())
                .status(AccountStatus.APPROVED)
                .build();
        updateInvoice = new AccountUpdateInvoice();
        updateInvoice.setEmail("email@example.com");
        updateInvoice.setCategory(AccountCategory.PARTICIPANT);
        updateInvoice.setRoles(Collections.singleton(AccountRole.PARTICIPANT));
        AccountPersonality personality = new AccountPersonality();
        personality.setQuickname("Test");
        personality.setFirstname("Test 2");
        updateInvoice.setPersonality(personality);
        updateInvoice.setParticipantPersonality(new ParticipantPersonality());
        updateInvoice.setStatus(AccountStatus.APPROVED);
    }

    @Test
    public void cannotUpdateDeletedAccounts() {
        accountDocument.setStatus(AccountStatus.DELETED);
        when(accountRepository.findById(any())).thenReturn(Optional.of(accountDocument));
        expectedException.expect(OperationException.class);
        expectedException.expectMessage("ERR_FORBIDDEN: Access denied");
        userManagementManager.update(ACCOUNT_ID, updateInvoice);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void updateMissingAccount() {
        when(accountRepository.findById(any())).thenReturn(Optional.empty());
        expectedException.expect(OperationException.class);
        expectedException.expectMessage(ERR_ACC.name());
        userManagementManager.update(ACCOUNT_ID, updateInvoice);
    }

}
