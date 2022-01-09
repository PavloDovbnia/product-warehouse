package com.rost.productwarehouse.scheduling;

import com.rost.productwarehouse.security.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiredPasswordResetTokensRemovingTask {

    private static final Logger LOG = LoggerFactory.getLogger(ExpiredPasswordResetTokensRemovingTask.class);

    private final UserService userService;

    public ExpiredPasswordResetTokensRemovingTask(UserService userService) {
        this.userService = userService;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void removeExpiredPasswordResetTokens() {
        long start = System.currentTimeMillis();
        LOG.info("ExpiredPasswordResetTokensRemovingTask has been started");
        userService.deleteExpiredPasswordResetTokens();
        LOG.info("ExpiredPasswordResetTokensRemovingTask has been ended in {} ms", System.currentTimeMillis() - start);
    }
}
