package com.rost.productwarehouse.initdata;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemPropertyValueDataType;
import com.rost.productwarehouse.itemproperty.ItemPropertyValueType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/init/")
public class InitDataController {

    @GetMapping("getData")
    public ResponseEntity<Map<String, Object>> getInitData() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("itemLevels", ItemLevel.levels().stream().map(i -> ImmutableMap.of("token", i.name(), "name", i.getName())).collect(Collectors.toList()));
        data.put("itemPropertyValueTypes", Arrays.stream(ItemPropertyValueType.values()).map(i -> ImmutableMap.of("token", i.name(), "name", i.getName())).collect(Collectors.toList()));
        data.put("itemPropertyValueDataTypes", Arrays.stream(ItemPropertyValueDataType.values()).map(i -> ImmutableMap.of("token", i.name(), "name", i.getName())).collect(Collectors.toList()));
        return ResponseEntity.ok(data);
    }

}
