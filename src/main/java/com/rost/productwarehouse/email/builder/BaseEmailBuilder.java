package com.rost.productwarehouse.email.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public abstract class BaseEmailBuilder implements EmailBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(BaseEmailBuilder.class);

    @Override
    public String prepareEmailTemplate() {
        try {
            String emailTemplate = readTemplate("EmailTemplate.html");
            return emailTemplate.replace("${email-content}", readContentTemplate());

        } catch (Exception ex) {
            LOG.error("Mail Template of " + getEmailType() + " type has not been prepared", ex);
            return null;
        }
    }

    @Override
    public String fillData(String emailTemplate, Map<String, Object> data) {
        return emailTemplate;
    }

    abstract String readContentTemplate() throws Exception;

    String readTemplate(String fileName) throws Exception {
        try (InputStream in = this.getClass().getResourceAsStream("/emails/" + fileName)) {
            return StreamUtils.copyToString(in, Charset.defaultCharset());
        }
    }
}
