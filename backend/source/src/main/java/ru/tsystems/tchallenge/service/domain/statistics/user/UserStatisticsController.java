package ru.tsystems.tchallenge.service.domain.statistics.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.tsystems.tchallenge.service.security.authentication.UserAuthentication;

import static ru.tsystems.tchallenge.service.security.authentication.AuthenticationManager.getAuthentication;

@RestController
@PreAuthorize("hasAnyAuthority('PARTICIPANT', 'REVIEWER', 'ROBOT')")
@Api(tags = "User statistics")
public class UserStatisticsController {

    private final UserStatisticsManager userStatisticsManager;

    public UserStatisticsController(UserStatisticsManager userStatisticsManager) {
        this.userStatisticsManager = userStatisticsManager;
    }

    @GetMapping("/statistics/presence/")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @ApiOperation("Check if current user have statistics. It means that user solved at least 2 workbooks " +
            "(need to know whether to show statistics link in header or not")
    public Boolean hasStatistics() {
        UserAuthentication authentication = getAuthentication();
        return userStatisticsManager.hasStatistics(authentication.getAccountId());
    }

    @GetMapping("/statistics/")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    @ApiOperation("Retrieve user statistics for current user")
    public GeneralUserStatistics getStatForCurrent() {
        UserAuthentication authentication = getAuthentication();
        return userStatisticsManager.retrieveGeneralStat(authentication.getAccountId());
    }

    @GetMapping("/{accountId}/statistics/")
    @PreAuthorize("hasAnyAuthority('REVIEWER', 'ROBOT')")
    @ApiOperation("Retrieve user statistics for specified user. For reviewer and robot only")
    public GeneralUserStatistics getStat(@PathVariable String accountId) {
        return userStatisticsManager.retrieveGeneralStat(accountId);
    }
}
