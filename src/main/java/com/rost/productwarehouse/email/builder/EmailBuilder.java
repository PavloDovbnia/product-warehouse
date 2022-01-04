package com.rost.productwarehouse.email.builder;

import com.rost.productwarehouse.email.EmailSendingData;

import java.util.Map;

public interface EmailBuilder {

    EmailSendingData.Type getEmailType();

    String prepareEmailTemplate();

    String fillData(String emailTemplate, Map<String, Object> data);
}
