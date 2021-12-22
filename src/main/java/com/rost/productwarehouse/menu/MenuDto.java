package com.rost.productwarehouse.menu;

import java.util.List;

public class MenuDto implements Comparable<MenuDto> {

    private String groupName;
    private List<MenuItem> items;

    public MenuDto() {
    }

    public MenuDto(String groupName, List<MenuItem> items) {
        this.groupName = groupName;
        this.items = items;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    @Override
    public int compareTo(MenuDto menuDto) {
        String s1 = this.getGroupName();
        String s2 = menuDto.getGroupName();

        if (s1.isBlank() && s2.isBlank()) {
            return 0;
        } else if (s1.isBlank()) {
            return 1;
        } else if (s2.isBlank()) {
            return -1;
        } else {
            return s1.compareToIgnoreCase(s2);
        }
    }
}
