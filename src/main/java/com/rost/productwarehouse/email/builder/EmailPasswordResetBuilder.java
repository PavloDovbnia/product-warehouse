package com.rost.productwarehouse.email.builder;

import com.rost.productwarehouse.email.EmailSendingData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailPasswordResetBuilder extends BaseEmailBuilder {

    @Override
    public EmailSendingData.Type getEmailType() {
        return EmailSendingData.Type.PASSWORD_RESET_TOKEN;
    }

    @Override
    String readContentTemplate() throws Exception {
        return readTemplate("EmailPasswordResetContent.html");
    }

    @Override
    public String fillData(String emailTemplate, Map<String, Object> data) {
        return super.fillData(emailTemplate, data)
                .replace("${username}", (String) data.getOrDefault("username", "there"))
                .replace("${password-reset-link}", (String) data.getOrDefault("password-reset-link", ""));
    }
}
