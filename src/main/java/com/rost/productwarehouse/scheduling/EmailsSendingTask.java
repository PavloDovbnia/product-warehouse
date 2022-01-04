package com.rost.productwarehouse.scheduling;

import com.google.common.collect.Maps;
import com.rost.productwarehouse.email.EmailSendingData;
import com.rost.productwarehouse.email.builder.EmailBuilder;
import com.rost.productwarehouse.email.service.EmailSenderService;
import com.rost.productwarehouse.email.service.EmailSendingDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EmailsSendingTask {

    private static final Logger LOG = LoggerFactory.getLogger(EmailsSendingTask.class);

    private final EmailSendingDataService emailSendingDataService;
    private final EmailSenderService emailSenderService;
    private final Map<EmailSendingData.Type, EmailBuilder> emailBuilders;

    public EmailsSendingTask(EmailSendingDataService emailSendingDataService, EmailSenderService emailSenderService, List<EmailBuilder> emailBuilders) {
        this.emailSendingDataService = emailSendingDataService;
        this.emailSenderService = emailSenderService;
        this.emailBuilders = emailBuilders.stream().collect(Collectors.toMap(EmailBuilder::getEmailType, Function.identity()));
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void sendEmails() {
        long start = System.currentTimeMillis();
        LOG.info("EmailsSendingTask has been started");

        Map<EmailSendingData.Type, String> emailTemplates = getEmailsTemplates();
        List<EmailSendingData> emailsData = emailSendingDataService.getEmailsData(EmailSendingData.Status.NOT_SENT);

        emailsData.forEach(data -> {
            EmailSendingData.Type emailType = data.getType();
            EmailBuilder emailBuilder = emailBuilders.get(emailType);
            String emailTemplate = emailTemplates.get(emailType);
            if (emailBuilder != null && emailTemplate != null) {
                String body = emailBuilder.fillData(emailTemplate, data.getData());
                try {
                    emailSenderService.sendEmail(data.getEmail(), body, (String) data.getData().getOrDefault("topic", ""));
                    data.setStatus(EmailSendingData.Status.SENT);
                } catch (Exception ex) {
                    data.setStatus(EmailSendingData.Status.ERROR);
                    LOG.error(emailType + " type mail was not sent to " + data.getEmail(), ex);
                }
            } else {
                LOG.error("Email Template or Email Builder was not found for " + emailType + " type, mail was not sent to " + data.getEmail());
            }
        });

        emailSendingDataService.save(emailsData);
        emailSendingDataService.delete(EmailSendingData.Status.SENT);

        LOG.info("EmailsSendingTask has been ended in {} ms", System.currentTimeMillis() - start);
    }

    private Map<EmailSendingData.Type, String> getEmailsTemplates() {
        Map<EmailSendingData.Type, String> emailTemplates = Maps.newHashMap();
        emailBuilders.forEach((type, emailBuilder) -> {
            String emailTemplate = emailBuilder.prepareEmailTemplate();
            if (emailTemplate != null) {
                emailTemplates.put(type, emailBuilder.prepareEmailTemplate());
            }
        });
        return emailTemplates;
    }
}
