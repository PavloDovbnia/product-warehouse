package com.rost.productwarehouse.email.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.email.EmailSendingData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class EmailSendingDataDaoImpl implements EmailSendingDataDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmailSendingDataDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<EmailSendingData> getEmailsData(EmailSendingData.Status status) {
        String sql = "select id, email, type, data, status, created, sending_date " +
                "from email_sending_data ";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("status", status.name()), new EmailSendingDataMapper());
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void save(Collection<EmailSendingData> emailsData) {
        addEmailsData(emailsData.stream().filter(EmailSendingData::isNew).collect(Collectors.toList()));
        updateEmailsData(emailsData.stream().filter(data -> !data.isNew()).collect(Collectors.toList()));
    }

    private void addEmailsData(Collection<EmailSendingData> emailsData) {
        if (CollectionUtils.isNotEmpty(emailsData)) {
            ObjectMapper objectMapper = new ObjectMapper();
            String sql = "insert into email_sending_data (email, type, status, data, created) " +
                    "values (:email, :type, :status, :data, :created)";

            SqlParameterSource[] batchParams = new MapSqlParameterSource[emailsData.size()];
            int i = 0;
            for (EmailSendingData data : emailsData) {
                batchParams[i] = getEmailDataParams(data, objectMapper);
                i++;
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    private void updateEmailsData(Collection<EmailSendingData> emailsData) {
        if (CollectionUtils.isNotEmpty(emailsData)) {
            ObjectMapper objectMapper = new ObjectMapper();
            String sql = "update email_sending_data " +
                    "set status = :status, " +
                    "created = :created " +
                    "where id = :id";

            SqlParameterSource[] batchParams = new MapSqlParameterSource[emailsData.size()];
            int i = 0;
            for (EmailSendingData data : emailsData) {
                batchParams[i] = getEmailDataParams(data, objectMapper);
                i++;
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    private SqlParameterSource getEmailDataParams(EmailSendingData data, ObjectMapper objectMapper) {
        MapSqlParameterSource params = new MapSqlParameterSource("email", data.getEmail())
                .addValue("id", data.getId())
                .addValue("type", data.getType().name())
                .addValue("created", data.getCreated() != null ? data.getCreated() : LocalDateTime.now());
        try {
            params = params.addValue("status", data.getStatus().name())
                    .addValue("data", objectMapper.writeValueAsString(data.getData()));

        } catch (JsonProcessingException ex) {
            params = params.addValue("status", EmailSendingData.Status.BROKEN_DATA)
                    .addValue("data", null);
        }
        return params;
    }

    @Override
    public void delete(List<Long> emailsDataIds) {
        String sql = "delete from email_sending_data where id in (:emailsDataIds)";
        jdbcTemplate.update(sql, new MapSqlParameterSource("emailsDataIds", emailsDataIds));
    }

    @Override
    public void delete(EmailSendingData.Status status) {
        String sql = "delete from email_sending_data where status = :status";
        jdbcTemplate.update(sql, new MapSqlParameterSource("status", status.name()));
    }

    private static class EmailSendingDataMapper implements RowMapper<EmailSendingData> {

        private ObjectMapper objectMapper = new ObjectMapper();
        private TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };

        @Override
        public EmailSendingData mapRow(ResultSet rs, int rowNum) throws SQLException {
            EmailSendingData data = new EmailSendingData();
            data.setId(rs.getLong("id"));
            data.setEmail(rs.getString("email"));
            data.setType(EmailSendingData.Type.valueOf(rs.getString("type")));
            data.setCreated(rs.getTimestamp("created").toLocalDateTime());
            data.setSendingDate(rs.getTimestamp("sending_date").toLocalDateTime());
            readDataJson(data, rs);
            return data;
        }

        private void readDataJson(EmailSendingData data, ResultSet rs) throws SQLException {
            String dataJson = rs.getString("data");
            try {
                data.setData(dataJson != null ? objectMapper.readValue(dataJson, typeReference) : Maps.newHashMap());
                data.setStatus(EmailSendingData.Status.valueOf(rs.getString("status")));
            } catch (JsonProcessingException ex) {
                data.setData(Maps.newHashMap());
                data.setStatus(EmailSendingData.Status.BROKEN_DATA);
            }
        }
    }
}
