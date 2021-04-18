package ru.tsystems.tchallenge.service.domain.statistics.wallboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WallboardController {
    private final WallboardManager wallboardManager;

    @Autowired
    public WallboardController(WallboardManager wallboardManager) {
        this.wallboardManager = wallboardManager;
    }

    @MessageMapping("/wallboard/{eventId}")
    @SendTo("/wallboard/{eventId}")
    public EventStatistics statisticsWallboard(@DestinationVariable String eventId) {
        return wallboardManager.retrieveWallboardDataForEvent(eventId);
    }
}
