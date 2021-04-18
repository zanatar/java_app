package ru.tsystems.tchallenge.service.domain.account.management;

import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.domain.account.*;
import ru.tsystems.tchallenge.service.reliability.exception.OperationException;
import ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucherFacade;
import ru.tsystems.tchallenge.service.security.voucher.SecurityVoucherInvoice;
import ru.tsystems.tchallenge.service.utility.mail.MailData;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailInvoice;
import ru.tsystems.tchallenge.service.utility.mail.TemplateMailManager;
import ru.tsystems.tchallenge.service.utility.search.Filter;
import ru.tsystems.tchallenge.service.utility.search.SearchInvoice;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;
import ru.tsystems.tchallenge.service.utility.search.SortInvoice;

import java.util.*;
import java.util.stream.Collectors;

import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionBuilder.forbidden;
import static ru.tsystems.tchallenge.service.reliability.exception.OperationExceptionType.ERR_ACC;


@Service
@Log4j2
public class UserManagementManager {

    private final AccountRepository accountRepository;
    private final AccountConverter accountConverter;
    private final AccountSystemManager accountSystemManager;
    private final SecurityVoucherFacade securityVoucherFacade;
    private final TemplateMailManager templateMailManager;
    private final MongoTemplate mongoTemplate;

    public UserManagementManager(AccountRepository accountRepository, AccountConverter accountConverter,
                                 AccountSystemManager accountSystemManager, SecurityVoucherFacade securityVoucherFacade,
                                 TemplateMailManager templateMailManager, MongoTemplate mongoTemplate) {
        this.accountRepository = accountRepository;
        this.accountConverter = accountConverter;
        this.accountSystemManager = accountSystemManager;
        this.securityVoucherFacade = securityVoucherFacade;
        this.templateMailManager = templateMailManager;
        this.mongoTemplate = mongoTemplate;
    }

    public Account create(final AccountCreateInvoice invoice) {
        Account account = accountConverter.toMgmtDto(accountSystemManager.create(invoice));
        log.info("Created " + account.getCategory() + "account " + account.getEmail());
        SecurityVoucherInvoice voucherInvoice = SecurityVoucherInvoice.builder()
                .backlinkTemplate(invoice.getBacklinkTemplate())
                .email(account.getEmail())
                .resetPassword(false)
                .build();

        // Stub because there is no robot ui now
        if (account.getCategory() != AccountCategory.ROBOT) {
            securityVoucherFacade.createAndSend(voucherInvoice);
        }
        return account;
    }


    public Account update(final String id,
                          final AccountUpdateInvoice invoice) {
        invoice.validate();
        AccountDocument accountDocument = accountRepository.findById(id).orElseThrow(() -> accountIsMissing(id));
        if ((accountDocument.getStatus() == AccountStatus.SUSPENDED) && (!onlyStatusChanged(invoice, accountDocument))
        || (accountDocument.getStatus() == AccountStatus.DELETED)) {
            throw forbidden();
        }
        if ((accountDocument.getStatus() == AccountStatus.APPROVED) && (!onlyStatusChanged(invoice, accountDocument))) {
            invoice.setStatus(AccountStatus.MODIFIED);
        }
        accountDocument.setEmail(invoice.getEmail());
        accountDocument.setRoles(invoice.getRoles());
        accountDocument.setCategory(invoice.getCategory());
        accountDocument.setStatus(invoice.getStatus());
        accountDocument.setPersonality(accountConverter.fromPersonality(invoice.getPersonality()));
        accountDocument.setParticipantPersonality(
                accountConverter.fromParticipantPersonality(invoice.getParticipantPersonality()));

        accountRepository.save(accountDocument);
        log.info("Updated account " + accountDocument.getId());
        return accountConverter.toMgmtDto(accountDocument);
    }

    private boolean onlyStatusChanged(AccountUpdateInvoice invoice, AccountDocument accountDocument) {
        return invoice.getEmail().equals(accountDocument.getEmail())
                && invoice.getCategory().equals(accountDocument.getCategory())
                && invoice.getRoles().equals(accountDocument.getRoles())
                && invoice.getPersonality().equals(accountConverter.toPersonality(accountDocument.getPersonality()))
                && ((invoice.getParticipantPersonality() == null) && (accountDocument.getParticipantPersonality() == null)
                || (invoice.getParticipantPersonality() != null)&& invoice.getParticipantPersonality()
                .equals(accountConverter.toParticipantPersonality(accountDocument.getParticipantPersonality())))
                && Objects.equals(invoice.getVkId(), accountDocument.getVkId());
    }


    public List<Account> findAll() {
        return accountRepository.findAll().stream()
                .map(accountConverter::toMgmtDto)
                .collect(Collectors.toList());
    }

    public SearchResult<Account> find(SearchInvoice<UserFilterKey> invoice) {
        invoice.validate();
        Filter emailFilter = invoice.getFilters().get(UserFilterKey.email);
        String emailFilterText = (emailFilter != null) ? emailFilter.getFilter() : "";
        Filter nameFilter = invoice.getFilters().get(UserFilterKey.name);
        String nameFilterText = (nameFilter != null) ? nameFilter.getFilter() : "";
        Set<AccountStatus> statuses = getAccountStatuses(invoice);
        Set<AccountRole> roles = getAccountRoles(invoice);
        PageRequest pageRequest = PageRequest.of(invoice.getPageIndex(), invoice.getPageSize(),
                getSort(invoice.getSort()));
        List<Account> accounts = accountRepository.find(mongoTemplate,
                emailFilterText, nameFilterText, statuses,
                roles, invoice.getFilters().get(UserFilterKey.registeredAt), pageRequest)
                .stream()
                .map(accountConverter::toMgmtDto)
                .collect(Collectors.toList());
        Long total = accountRepository.count(mongoTemplate,
                emailFilterText, nameFilterText, statuses,
                roles, invoice.getFilters().get(UserFilterKey.registeredAt));
        return SearchResult.<Account>builder()
                .items(accounts)
                .total(total)
                .build();
    }

    private Set<AccountStatus> getAccountStatuses(SearchInvoice<UserFilterKey> invoice) {
        Set<AccountStatus> statuses = EnumSet.noneOf(AccountStatus.class);
        Filter statusFilter = invoice.getFilters().get(UserFilterKey.status);
        if ((statusFilter != null) && (statusFilter.getValues().length != 0)) {
            String[] statusNames = statusFilter.getValues();
            for (String name: statusNames) {
                AccountStatus accountStatus = AccountStatus.valueOf(name);
                statuses.add(accountStatus);
            }
        } else {
            statuses = EnumSet.allOf(AccountStatus.class);
        }
        return statuses;
    }

    private Set<AccountRole> getAccountRoles(SearchInvoice<UserFilterKey> invoice) {
        Set<AccountRole> roles = EnumSet.noneOf(AccountRole.class);
        Filter rolesFilter = invoice.getFilters().get(UserFilterKey.roles);
        if ((rolesFilter != null) && (rolesFilter.getValues().length != 0)) {
            String[] rolesNames = rolesFilter.getValues();
            for (String name: rolesNames) {
                AccountRole accountRole = AccountRole.valueOf(name);
                roles.add(accountRole);
            }
        } else {
            roles = EnumSet.allOf(AccountRole.class);
        }
        return roles;
    }

    private Sort getSort(List<SortInvoice<UserFilterKey>> sortModels) {
        if (sortModels.isEmpty()) {
            return Sort.by(UserFilterKey.registeredAt.name());
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (SortInvoice<UserFilterKey> model : sortModels) {
            UserFilterKey sortKey = model.getKey();
            Sort.Direction direction  = model.getAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
            orders.add(new Sort.Order(direction, sortKey.name()));
        }
        return Sort.by(orders);
    }

    private OperationException accountIsMissing(String id) {
        return OperationExceptionBuilder.operationException()
                .textcode(ERR_ACC)
                .description("Account is missing")
                .attachment(id)
                .build();
    }

    public void sendEmail(String id, EmailInvoice invoice) {
        AccountDocument accountDocument = accountRepository.findById(id).orElseThrow(() -> accountIsMissing(id));
        AccountPersonalityDocument personality = accountDocument.getPersonality();
        String name = Strings.isNullOrEmpty(personality.getFirstname()) ?
                personality.getQuickname() : personality.getFirstname();
        final MailData mailData = MailData.builder()
                .name(name)
                .customContent(invoice.getContent().split("\n"))
                .supportEmail(templateMailManager.getSupportEmail())
                .logoPath(templateMailManager.createLogoPath(accountDocument.getCategory()))
                .build();
        final TemplateMailInvoice templateMailInvoice = TemplateMailInvoice.builder()
                .email(accountDocument.getEmail())
                .subject(invoice.getSubject())
                .templateName("custom-email")
                .data(mailData)
                .build();
        log.info("Sending email to " + accountDocument.getEmail());
        templateMailManager.sendAsync(templateMailInvoice);
    }
}
