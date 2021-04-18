package ru.tsystems.tchallenge.service.domain.account;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.tsystems.tchallenge.service.utility.search.SearchResult;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ParticipantInfoManager {
    private final AccountRepository accountRepository;

    public ParticipantInfoManager(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public SearchResult<String> findParticipantEmails(int pageIndex, int pageSize, String emailFilter) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Order.asc("email")));
        List<String> emails = accountRepository
                .findByCategoryAndEmailContainingIgnoreCase(AccountCategory.PARTICIPANT,
                        emailFilter, pageable)
                .stream()
                .map(AccountDocument::getEmail)
                .collect(Collectors.toList());
        Long total = accountRepository.countByCategoryAndEmailContainingIgnoreCase(AccountCategory.PARTICIPANT,
                emailFilter);
        return SearchResult.<String>builder()
                .items(emails)
                .total(total)
                .build();
    }

}
