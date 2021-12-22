create table roles
(
    id   bigint      not null auto_increment primary key,
    type varchar(50) not null,
    unique index idx_type (type)
);

create table users
(
    id       bigint       not null auto_increment primary key,
    username varchar(255) not null,
    email    varchar(255) not null,
    password varchar(255) not null,
    unique index idx_email (email),
    unique index idx_username (username)
);

create table user_roles
(
    user_id bigint not null,
    role_id bigint not null,
    primary key (user_id, role_id),
    index idx_role_id (role_id),
    foreign key fk_user (user_id) references users (id),
    foreign key fk_role (role_id) references roles (id)
);

insert into roles (type)
values ('ROLE_MANAGER'),
       ('ROLE_PRODUCT_PROVIDER'),
       ('ROLE_PRODUCT_CONSUMER'),
       ('ROLE_ADMIN');

insert into users(username, email, password)
values ('Rost', 'rost@gmail.com', '$2a$10$lidNN0oBxqXlYafWseKVxOeiN9eQ3wpvkp4XEg9xh3kMk1xlEiqIW');

# '$2a$10$lidNN0oBxqXlYafWseKVxOeiN9eQ3wpvkp4XEg9xh3kMk1xlEiqIW' - 'password'

insert into user_roles (user_id, role_id)
select u.id, r.id
from users u,
     roles r
where u.username = 'Rost'
  and r.type = 'ROLE_ADMIN';


create table password_reset_token
(
    token   varchar(255) not null primary key,
    user_id bigint       not null,
    created timestamp    not null default CURRENT_TIMESTAMP,

    unique index idx_user_id (user_id),
    foreign key fk_user (user_id) references users (id)
);
