package com.rost.productwarehouse.email.builder;

import com.rost.productwarehouse.email.EmailSendingData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailOrderCanNotBeHandledBuilder extends BaseEmailBuilder {

    @Override
    public EmailSendingData.Type getEmailType() {
        return EmailSendingData.Type.ORDER_CAN_NOT_BE_HANDLED;
    }

    @Override
    String readContentTemplate() throws Exception {
        return readTemplate("EmailOrderCanNotBeHandledContent.html");
    }

    @Override
    public String fillData(String emailTemplate, Map<String, Object> data) {
        return super.fillData(emailTemplate, data)
                .replace("${username}", (String) data.getOrDefault("username", "there"))
                .replace("${view-user-orders-link}", (String) data.getOrDefault("view-user-orders-link", ""))
                .replace("${order-id}", (String) data.getOrDefault("order-id", ""));
    }
}
