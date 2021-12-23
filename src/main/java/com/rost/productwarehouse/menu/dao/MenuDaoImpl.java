package com.rost.productwarehouse.menu.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rost.productwarehouse.menu.Menu;
import com.rost.productwarehouse.menu.MenuItem;
import com.rost.productwarehouse.menu.MenuItemsGroup;
import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.dao.RoleMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MenuDaoImpl implements MenuDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public MenuDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Menu getMenu(List<Role.Type> roleTypes) {
        if (CollectionUtils.isNotEmpty(roleTypes)) {
            String sql = "select distinct r.id role_id, r.type role_type, i.id menu_item_id, i.name menu_item_name, i.url menu_item_url, " +
                    " i.access_type menu_item_access_type, g.id group_id, g.name group_name " +
                    " from roles r " +
                    " join menu m " +
                    " on r.id = m.role_id " +
                    " join menu_item i " +
                    " on m.menu_item_id = i.id " +
                    " left join menu_items_group g " +
                    " on m.menu_items_group_id = g.id " +
                    " where r.type in (:roleTypes) " +
                    " order by group_name, menu_item_name ";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("roleTypes", roleTypes.stream().map(Role.Type::name).collect(Collectors.toList())), new MenuExtractor());
        }
        return null;
    }

    private static class MenuExtractor implements ResultSetExtractor<Menu> {

        private RoleMapper roleMapper = new RoleMapper("role_");

        @Override
        public Menu extractData(ResultSet rs) throws SQLException, DataAccessException {
            Menu menu = null;
            Map<String, MenuItem> items = Maps.newHashMap();
            Map<MenuItemsGroup, Set<String>> groups = Maps.newLinkedHashMap();
            while (rs.next()) {
                if (menu == null) {
                    menu = mapMenu(rs);
                }
                MenuItemsGroup group = mapGroup(rs);
                groups.computeIfAbsent(group, key -> Sets.newLinkedHashSet()).add(rs.getString("menu_item_url"));

                MenuItem item = mapMenuItem(rs);
                switch (item.getAccessType()) {
                    case READ_ONLY:
                        items.putIfAbsent(item.getUrl(), item);
                        break;
                    case READ_WRITE:
                        items.put(item.getUrl(), item);
                        break;
                }
            }
            if (menu != null) {
                Map<MenuItemsGroup, List<MenuItem>> menuItemsGroups = Maps.newTreeMap(Comparator.comparing(MenuItemsGroup::getId));
                groups.forEach((group, urls) ->
                        urls.forEach(url -> {
                            MenuItem item = items.get(url);
                            if (item != null) {
                                menuItemsGroups.computeIfAbsent(group, key -> Lists.newArrayList()).add(item);
                            }
                        })
                );
                menu.setItems(menuItemsGroups);
            }
            return menu;
        }

        private Menu mapMenu(ResultSet rs) throws SQLException, DataAccessException {
            Menu menu = new Menu();
            menu.setRole(roleMapper.mapRow(rs, rs.getRow()));
            return menu;
        }

        private MenuItem mapMenuItem(ResultSet rs) throws SQLException, DataAccessException {
            MenuItem item = new MenuItem();
            item.setId(rs.getLong("menu_item_id"));
            item.setName(rs.getString("menu_item_name"));
            item.setUrl(rs.getString("menu_item_url"));
            item.setAccessType(MenuItem.AccessType.of(rs.getString("menu_item_access_type")));
            return item;
        }

        private MenuItemsGroup mapGroup(ResultSet rs) throws SQLException, DataAccessException {
            long id = rs.getLong("group_id");
            if (!rs.wasNull()) {
                MenuItemsGroup group = new MenuItemsGroup();
                group.setId(id);
                group.setName(rs.getString("group_name"));
                return group;
            } else {
                MenuItemsGroup group = new MenuItemsGroup();
                group.setId(-1);
                group.setName("");
                return group;
            }
        }
    }
}
