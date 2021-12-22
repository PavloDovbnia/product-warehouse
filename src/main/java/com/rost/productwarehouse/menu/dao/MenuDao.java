package com.rost.productwarehouse.menu.dao;

import com.rost.productwarehouse.menu.Menu;
import com.rost.productwarehouse.security.Role;

import java.util.List;

public interface MenuDao {

    Menu getMenu(List<Role.Type> roleTypes);
}
