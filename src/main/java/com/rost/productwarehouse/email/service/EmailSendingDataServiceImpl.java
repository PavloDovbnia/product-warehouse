package com.rost.productwarehouse.email.service;

import com.rost.productwarehouse.email.EmailSendingData;
import com.rost.productwarehouse.email.dao.EmailSendingDataDao;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.UserDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailSendingDataServiceImpl implements EmailSendingDataService {

    private final EmailSendingDataDao emailSendingDataDao;
    private final UserDao userDao;

    public EmailSendingDataServiceImpl(EmailSendingDataDao emailSendingDataDao, UserDao userDao) {
        this.emailSendingDataDao = emailSendingDataDao;
        this.userDao = userDao;
    }

    @Override
    public List<EmailSendingData> getEmailsData(EmailSendingData.Status status) {
        return decorate(emailSendingDataDao.getEmailsData(status));
    }

    @Override
    public void save(Collection<EmailSendingData> emailsData) {
        emailSendingDataDao.save(emailsData);
    }

    @Override
    public void delete(List<Long> emailsDataIds) {
        emailSendingDataDao.delete(emailsDataIds);
    }

    @Override
    public void delete(EmailSendingData.Status status) {
        emailSendingDataDao.delete(status);
    }

    private List<EmailSendingData> decorate(List<EmailSendingData> emailsData) {
        if (CollectionUtils.isNotEmpty(emailsData)) {
            List<String> usersEmails = emailsData.stream().map(EmailSendingData::getEmail).collect(Collectors.toList());
            Map<String, User> users = userDao.getByEmails(usersEmails);

            emailsData.forEach(data -> data.setUser(users.get(data.getEmail())));
        }
        return emailsData;
    }
}
