### category ###
insert into product_group_category (name)
values ('Mineral Waters'),
       ('Soft Drinks');

### manufacturer ###
insert into manufacturer (name)
values ('Morshynska'),
       ('Coca-Cola');

### product ###
insert into product (name)
values ('Morshynska Not Sparkling 0.5L'),
       ('Morshynska Not Sparkling 1L'),
       ('Morshynska Not Sparkling 1.5L'),
       ('Morshynska Sparkling 0.5L'),
       ('Morshynska Sparkling 1L'),
       ('Morshynska Sparkling 1.5L'),
       ('Coca-Cola 0.25L'),
       ('Coca-Cola 0.33L'),
       ('Fanta 0.25L'),
       ('Fanta 0.33L'),
       ('Sprite 0.25L'),
       ('Sprite 0.33L');

### group ###
insert into product_group (name)
values ('Morshynska Not Sparkling'),
       ('Morshynska Sparkling'),
       ('Coca-Cola'),
       ('Fanta'),
       ('Sprite');

### product - group ###
insert into product_to_product_group (product_id, product_group_id)
select p.id, g.id
from product_group g,
     product p
where p.name like concat('%', g.name, '%');

### group - category ###
insert into product_group_to_product_group_category (product_group_id, product_group_category_id)
select g.id, c.id
from product_group g,
     product_group_category c
where g.name like 'Morshynska%'
  and c.name = 'Mineral Waters';

insert into product_group_to_product_group_category (product_group_id, product_group_category_id)
select g.id, c.id
from product_group g,
     product_group_category c
where g.name not like 'Morshynska%'
  and c.name = 'Soft Drinks';

### group - manufacturer ###
insert into product_group_to_manufacturer (product_group_id, manufacturer_id)
select g.id, m.id
from product_group g,
     manufacturer m
where g.name like 'Morshynska%'
  and m.name = 'Morshynska';

insert into product_group_to_manufacturer (product_group_id, manufacturer_id)
select g.id, m.id
from product_group g,
     manufacturer m
where g.name not like 'Morshynska%'
  and m.name != 'Morshynska';

### item property ###
insert into item_property (token, name, item_level, type, data_type)
values ('name', 'Name', 'MANUFACTURER', 'SINGLE', 'STRING'),
       ('name', 'Name', 'PRODUCT_GROUP', 'SINGLE', 'STRING'),
       ('name', 'Name', 'PRODUCT', 'SINGLE', 'STRING'),
       ('bottle-type', 'Bottle Type', 'PRODUCT_GROUP', 'SINGLE', 'STRING'),
       ('bottle-type', 'Bottle Type', 'PRODUCT', 'SINGLE', 'STRING'),
       ('measurement', 'Measurement', 'PRODUCT_GROUP', 'SINGLE', 'STRING'),
       ('measurement-value', 'Measurement Value', 'PRODUCT', 'SINGLE', 'BIG_DECIMAL'),
       ('sparkling', 'Sparkling', 'PRODUCT_GROUP', 'SINGLE', 'STRING');

### product properties values ###
insert into product_property_value (product_id, property_id, property_value)
select pr.id, p.id, 'Can'
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'bottle-type'
  and pr.name like '%0.25L%'
union
select pr.id, p.id, 'Glass Bottle'
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'bottle-type'
  and pr.name like '%0.33L%';

insert into product_property_value (product_id, property_id, property_value)
select pr.id, p.id, '0.5'
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'measurement-value'
  and pr.name like '%0.5L%'
union
select pr.id, p.id, '1'
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'measurement-value'
  and pr.name like '%1L%'
union
select pr.id, p.id, '1.5'
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'measurement-value'
  and pr.name like '%1.5L%'
union
select pr.id, p.id, '0.33'
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'measurement-value'
  and pr.name like '%0.33L%'
union
select pr.id, p.id, '0.25'
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'measurement-value'
  and pr.name like '%0.25L%';


insert into product_property_value (product_id, property_id, property_value)
select pr.id, p.id, pr.name
from item_property p,
     product pr
where p.item_level = 'PRODUCT'
  and p.token = 'name';


### group properties values ###
insert into product_group_property_value (product_group_id, property_id, property_value)
select g.id, p.id, '0'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'sparkling'
  and g.name = 'Morshynska Not Sparkling'
union
select g.id, p.id, '1'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'sparkling'
  and g.name = 'Morshynska Sparkling'
union
select g.id, p.id, '1'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'sparkling'
  and g.name = 'Coca-Cola'
union
select g.id, p.id, '1'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'sparkling'
  and g.name = 'Fanta'
union
select g.id, p.id, '1'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'sparkling'
  and g.name = 'Sprite';



insert into product_group_property_value (product_group_id, property_id, property_value)
select g.id, p.id, 'L'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'measurement'
  and g.name = 'Morshynska Not Sparkling'
union
select g.id, p.id, 'L'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'measurement'
  and g.name = 'Morshynska Sparkling'
union
select g.id, p.id, 'L'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'measurement'
  and g.name = 'Coca-Cola'
union
select g.id, p.id, 'L'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'measurement'
  and g.name = 'Fanta'
union
select g.id, p.id, 'L'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'measurement'
  and g.name = 'Sprite';


insert into product_group_property_value (product_group_id, property_id, property_value)
select g.id, p.id, 'Morshynska (Not Sparkling)'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'name'
  and g.name = 'Morshynska Not Sparkling'
union
select g.id, p.id, 'Morshynska (Sparkling)'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'name'
  and g.name = 'Morshynska Sparkling'
union
select g.id, p.id, 'Coca-Cola'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'name'
  and g.name = 'Coca-Cola'
union
select g.id, p.id, 'Fanta'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'name'
  and g.name = 'Fanta'
union
select g.id, p.id, 'Sprite'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'name'
  and g.name = 'Sprite';


insert into product_group_property_value (product_group_id, property_id, property_value)
select g.id, p.id, 'plastic bottle'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'bottle-type'
  and g.name = 'Morshynska Not Sparkling'
union
select g.id, p.id, 'plastic bottle'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'bottle-type'
  and g.name = 'Morshynska Sparkling'
union
select g.id, p.id, 'plastic bottle'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'bottle-type'
  and g.name = 'Coca-Cola'
union
select g.id, p.id, 'plastic bottle'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'bottle-type'
  and g.name = 'Fanta'
union
select g.id, p.id, 'plastic bottle'
from item_property p,
     product_group g
where p.item_level = 'PRODUCT_GROUP'
  and p.token = 'bottle-type'
  and g.name = 'Sprite';

### manufacturer properties values ###
insert into manufacturer_property_value (manufacturer_id, property_id, property_value)
select m.id, p.id, 'The Morshyska Company'
from item_property p,
     manufacturer m
where p.item_level = 'MANUFACTURER'
  and p.token = 'name'
  and m.name = 'Morshynska';

insert into manufacturer_property_value (manufacturer_id, property_id, property_value)
select m.id, p.id, 'The Coca-Cola Company'
from item_property p,
     manufacturer m
where p.item_level = 'MANUFACTURER'
  and p.token = 'name'
  and m.name = 'Coca-Cola';