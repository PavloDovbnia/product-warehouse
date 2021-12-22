package com.rost.productwarehouse.menu;

import com.google.common.collect.Sets;
import com.rost.productwarehouse.menu.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/get")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_PRODUCT_PROVIDER', 'ROLE_PRODUCT_CONSUMER')")
    public ResponseEntity<Set<MenuDto>> getMenu() {
        Menu menu = menuService.getMenu();

        Set<MenuDto> response = Sets.newTreeSet();
        if (menu != null) {
            menu.getItems().forEach((group, items) -> response.add(new MenuDto(group.getName(), items)));
        }
        return ResponseEntity.ok(response);
    }
}
