package ru.tsystems.tchallenge.service.reliability.exception;

public enum OperationExceptionType {
    // Empty password on registration
    ERR_REG_PASS,
    // Incorrect current password
    ERR_UPD_PASS,
    // Illegal account status
    ERR_UPD_STATUS,
    // Empty account email
    ERR_ACC_EMAIL,
    // Account roles are missing
    ERR_ACC_ROLES,
    // Account status is missing
    ERR_ACC_STATUS,
    // Account quickname missing
    ERR_ACC_QUICKNAME,
    // Account with such email already registered
    ERR_REG_EMAIL,
    // Account category is missing
    ERR_ACC_CAT,
    // Permalink is missing
    ERR_PERMALINK,
    // Event with such permalink already exists
    ERR_PERMALINK_TAKEN,
    // Caption is missing
    ERR_CAPTION,
    // Workbook not found
    ERR_WORKBOOK,
    // Authentication invoice is missing
    ERR_AUTH_INVOICE,
    // Account not found
    ERR_ACC,
    // Invalid credentials or account not found
    ERR_ACC_OR_PASS,
    // Can't login due to illegal status
    ERR_ACC_ILLEGAL_STATUS,
    // Account is not approved, need to verify it (check email ang go to the link in the letter)
    ERR_ACC_NEED_CONFIRMATION,
    // Token is invalid or expired
    ERR_ACC_TOKEN,
    // Voucher is invalid or expired
    ERR_ACC_VOUCHER,
    // Not authorized
    ERR_NOT_AUTHORIZED,
    ERR_NEED_EMAIL,
    // Event ids are missing
    ERR_EVENT_IDS_MISSING,
    // Event is a part of another series
    ERR_EVENT_IN_ANOTHER_SERIES,
    // Problem option is missing
    ERR_PROBLEM_OPTION,
    // If problem has text expectation, it should have exactly one (correct) option
    // with non-empty content
    ERR_PROBLEM_TEXT_OPTION,
    // Problem doesn't contain correct option
    ERR_PROBLEM_NO_CORRECT_OPTION,
    // Problem category is missing
    ERR_PROBLEM_CATEGORY,
    // Problem expectation is missing
    ERR_PROBLEM_EXPECTATION,
    // Problem question is missing
    ERR_PROBLEM_QUESTION,
    // Problem difficulties are missing
    ERR_PROBLEM_DIFFICULTIES,
    // Wrong number of problem difficulties
    ERR_PROBLEM_DIFFICULTIES_NUMBER,
    // Wrong number of problems
    ERR_PROBLEMS_NUMBER,
    // Problem snippet content is missing
    ERR_PROBLEM_CONTENT,
    // When there aren't any attempts left for the participant during the event
    ERR_ATTEMPTS_LEFT,
    // Invalid number of attempts
    ERR_ATTEMPTS_NUMBER_INVALID,
    // Assignment solution is missing
    ERR_ASSIGNMENT_SOLUTION,
    // When participant try to submit workbook after submittableUntil date
    ERR_WORKBOOK_EXPIRED,
    // Internal server error
    ERR_INTERNAL,
    // Not enough rights for operation
    ERR_FORBIDDEN,
    // Try to load file more than 10 MB
    ERR_MAX_UPLOAD_SIZE_EXCEEDED,
    // Invalid url in linkedin
    ERR_LINKEDIN,
    // Invalid url in hh
    ERR_HH,
    // Invalid url in github
    ERR_GITHUB,
    // Invalid url in bitbucket
    ERR_BITBUCKET,
    // Invalid url in website
    ERR_WEBSITE,
    // Backlink is missing
    ERR_BACKLINK,
    //Participant personality missing
    ERR_PARTICIPANT_PERSONALITY,
    // Tests should not be empty
    ERR_TESTS_EMPTY,
    // Contest invoice is missing
    ERR_CONTEST,
    // Code is missing in assignment update invoice
    ERR_ASSIGNMENT_UPDATE_CODE,
    // Language is missing in assignment update invoice
    ERR_ASSIGNMENT_UPDATE_LANG,
    // Illegal update of assignment (e.g. update code solution for problem with single options expectation
    ERR_ASSIGNMENT_UPDATE_ILLEGAL,
    // All participant satisfying threshold is already winners
    ERR_NO_MORE_WINNERS,
    // Page index is missing or incorrect
    ERR_PAGE_INDEX,
    // Page size is missing or incorrect
    ERR_PAGE_SIZE
}
