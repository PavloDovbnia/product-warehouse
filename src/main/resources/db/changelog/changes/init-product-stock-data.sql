### drop table if exists product_stock_data;

create table product_stock_data
(
    product_id  bigint not null primary key,
    stock_value int    not null default 0,
    index idx_stock_value (stock_value),
    foreign key fm_product (product_id) references product (id) on delete cascade on update cascade
);


insert into product_stock_data (product_id, stock_value)
select id, (FLOOR(1 + RAND() * 20)) random_stock_value
from product;
