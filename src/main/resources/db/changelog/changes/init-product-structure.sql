create table product
(
    id   bigint       not null auto_increment primary key,
    name varchar(255) not null
);

create table product_group
(
    id   bigint       not null auto_increment primary key,
    name varchar(255) not null
);

create table product_to_product_group
(
    product_id       bigint not null,
    product_group_id bigint not null,
    primary key (product_id, product_group_id),
    index idx_product_group_id (product_group_id),
    foreign key fk_product (product_id) references product (id),
    foreign key fk_product_group (product_group_id) references product_group (id)
);

create table manufacturer
(
    id   bigint       not null auto_increment primary key,
    name varchar(255) not null
);

create table product_group_to_manufacturer
(
    product_group_id bigint not null,
    manufacturer_id  bigint not null,
    primary key (product_group_id, manufacturer_id),
    index idx_manufacturer_id (manufacturer_id),
    foreign key fk_product_group (product_group_id) references product_group (id),
    foreign key fk_manufacturer (manufacturer_id) references manufacturer (id)
);

create table product_group_category
(
    id   bigint       not null auto_increment primary key,
    name varchar(255) not null
);

create table product_group_to_product_group_category
(
    product_group_id          bigint not null,
    product_group_category_id bigint not null,
    primary key (product_group_id, product_group_category_id),
    index idx_product_group_category_id (product_group_category_id),
    foreign key fk_product_group (product_group_id) references product_group (id),
    foreign key fk_product_group_category (product_group_category_id) references product_group_category (id)
);


create table item_property
(
    id         bigint       not null auto_increment primary key,
    token      varchar(50)  not null,
    name       varchar(100) null,
    item_level varchar(20)  not null,
    type       varchar(20)  not null,
    data_type  varchar(20)  not null,

    unique index idx_token_item_level (token, item_level),
    index idx_item_level (item_level)
);



create table product_property_value
(
    product_id     bigint       not null,
    property_id    bigint       not null,
    property_value varchar(255) null,
    primary key (product_id, property_id),
    index idx_property_id (property_id),
    foreign key fk_product (product_id) references product (id),
    foreign key fk_item_property (property_id) references item_property (id)
);



create table product_group_property_value
(
    product_group_id bigint       not null,
    property_id      bigint       not null,
    property_value   varchar(255) null,
    primary key (product_group_id, property_id),
    index idx_property_id (property_id),
    foreign key fk_product_group (product_group_id) references product_group (id),
    foreign key fk_item_property (property_id) references item_property (id)
);



create table manufacturer_property_value
(
    manufacturer_id bigint       not null,
    property_id     bigint       not null,
    property_value  varchar(255) null,
    primary key (manufacturer_id, property_id),
    index idx_property_id (property_id),
    foreign key fk_manufacturer (manufacturer_id) references manufacturer (id),
    foreign key fk_item_property (property_id) references item_property (id)
);



