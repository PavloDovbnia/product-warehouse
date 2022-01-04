create table email_sending_data
(
    id           bigint       not null primary key auto_increment,
    email        varchar(255) not null,
    type         varchar(50)  not null,
    status       varchar(50)  not null default 'NOT_SENT',
    data         text         null,
    created      timestamp    not null default CURRENT_TIMESTAMP,
    sending_date timestamp    not null default CURRENT_TIMESTAMP,
    index idx_status (status),
    index idx_email (email),
    foreign key fk_user (email) references users (email) on delete cascade on update cascade
);