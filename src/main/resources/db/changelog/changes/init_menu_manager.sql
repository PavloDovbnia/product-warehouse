insert into menu (role_id, menu_item_id, menu_items_group_id)
select r.id, i.id, g.id
from roles r
         join menu_item i
              on i.url like '/api/product/%'
         left join menu_items_group g
                   on g.name = 'Products'
where r.type = 'ROLE_MANAGER';


insert into menu (role_id, menu_item_id, menu_items_group_id)
select r.id, i.id, null
from roles r
         join menu_item i
              on i.url in ('/api/provider/getAll', '/api/consumer/getAll')
where r.type = 'ROLE_MANAGER';