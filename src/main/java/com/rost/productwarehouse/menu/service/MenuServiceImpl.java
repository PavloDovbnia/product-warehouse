package com.rost.productwarehouse.menu.service;

import com.rost.productwarehouse.menu.Menu;
import com.rost.productwarehouse.menu.dao.MenuDao;
import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.service.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuDao menuDao;

    public MenuServiceImpl(MenuDao menuDao) {
        this.menuDao = menuDao;
    }

    @Override
    public Menu getMenu() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<Role.Type> roleTypes = userDetails.getAuthorities().stream()
                    .map(authority -> Role.Type.of(authority.getAuthority()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            return menuDao.getMenu(roleTypes);
        }
        return null;
    }
}
