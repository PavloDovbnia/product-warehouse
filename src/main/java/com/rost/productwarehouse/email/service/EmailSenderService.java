package com.rost.productwarehouse.email.service;

public interface EmailSenderService {

    void sendEmail(String to, String body, String topic) throws Exception;
}
