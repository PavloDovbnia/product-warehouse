drop table if exists menu;
drop table if exists menu_items_group, menu_item;

create table menu_item
(
    id          bigint       not null auto_increment primary key,
    name        varchar(100) not null,
    url         varchar(255) not null,
    access_type varchar(50)  not null default 'READ_ONLY',
    unique index idx_url_name_access_type (url, name, access_type)
);

create table menu_items_group
(
    id   bigint       not null auto_increment primary key,
    name varchar(100) not null
);


create table menu
(
    role_id             bigint not null,
    menu_item_id        bigint not null,
    menu_items_group_id bigint null,
    primary key (role_id, menu_item_id),
    index idx_menu_item_id_role_id_group_id (menu_item_id, role_id, menu_items_group_id),
    index idx_group_id_role_id_item_id (menu_items_group_id, role_id, menu_item_id),
    foreign key fk_role (role_id) references roles (id) on delete cascade on update cascade,
    foreign key fk_menu_item (menu_item_id) references menu_item (id) on delete cascade on update cascade,
    foreign key fk_group (menu_items_group_id) references menu_items_group (id) on delete cascade on update cascade
);


insert into menu_item (`name`, url, access_type)
values ('Products', '/api/product/getAll', 'READ_WRITE'),
       ('Categories', '/api/product/category/getAll', 'READ_WRITE'),
       ('Groups', '/api/product/group/getAll', 'READ_WRITE'),
       ('Manufacturers', '/api/product/manufacturer/getAll', 'READ_WRITE'),
       ('Properties', '/api/product/property/getAll', 'READ_WRITE'),
       ('Stock Data', '/api/stock/getValues', 'READ_WRITE'),
       ('Orders', '/api/order/getAll', 'READ_WRITE'),
       ('Users', '/api/auth/registerUser', 'READ_WRITE');


insert into menu_items_group (`name`)
values ('Products');


insert into menu (role_id, menu_item_id, menu_items_group_id)
select r.id, i.id, g.id
from roles r
         join menu_item i
              on i.url like '/api/product/%'
                  and i.access_type = 'READ_WRITE'
         left join menu_items_group g
                   on g.name = 'Products'
where r.type in ('ROLE_ADMIN', 'ROLE_MANAGER');


insert into menu (role_id, menu_item_id, menu_items_group_id)
select r.id, i.id, null
from roles r
         join menu_item i
              on i.url in ('/api/stock/getValues', '/api/order/getAll')
                  and i.access_type = 'READ_WRITE'
where r.type in ('ROLE_ADMIN', 'ROLE_MANAGER');


insert into menu (role_id, menu_item_id, menu_items_group_id)
select r.id, i.id, null
from roles r
         join menu_item i
              on i.url in ('/api/auth/registerUser')
                  and i.access_type = 'READ_WRITE'
where r.type in ('ROLE_ADMIN');