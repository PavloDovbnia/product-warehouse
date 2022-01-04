package com.rost.productwarehouse.email.dao;

import com.rost.productwarehouse.email.EmailSendingData;

import java.util.Collection;
import java.util.List;

public interface EmailSendingDataDao {

    List<EmailSendingData> getEmailsData(EmailSendingData.Status status);

    void save(Collection<EmailSendingData> emailsData);

    void delete(List<Long> emailsDataIds);

    void delete(EmailSendingData.Status status);
}
