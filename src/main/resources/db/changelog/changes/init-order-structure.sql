### drop table if exists product_to_provider, order_data;
### drop table if exists order;

create table `order`
(
    id            bigint       not null primary key auto_increment,
    type          varchar(20)  not null,
    user_id       bigint       not null,
    created       timestamp    not null default CURRENT_TIMESTAMP,
    state         varchar(40)  not null default 'REQUESTED',
    state_changed timestamp    not null default CURRENT_TIMESTAMP,
    state_comment varchar(255) null,
    index idx_type_user_created (type, user_id, created),
    index idx_type_created (type, created),
    index idx_type_state (type, state)
);

create table order_data
(
    id         bigint not null primary key auto_increment,
    order_id   bigint not null,
    product_id bigint not null,
    value      int    not null,

    unique index idx_order_product (order_id, product_id),
    foreign key fk_order (order_id) references `order` (id) on delete cascade on update cascade
);


create table product_to_provider
(
    user_id    bigint not null,
    product_id bigint not null,
    primary key (user_id, product_id),
    index idx_product (product_id),
    foreign key fk_product (product_id) references product (id) on delete cascade on update cascade,
    foreign key fk_user (user_id) references users (id) on delete cascade on update cascade
);