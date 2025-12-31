package com.lingxian.user.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户端-搜索", description = "搜索相关接口")
public class UserSearchController {

    @GetMapping("/search")
    @Operation(summary = "搜索商品")
    public Result<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> products = new ArrayList<>();

        for (int i = 1; i <= pageSize; i++) {
            int id = (page - 1) * pageSize + i;
            Map<String, Object> product = new HashMap<>();
            product.put("id", id);
            product.put("name", keyword + "-搜索结果" + id);
            product.put("price", 9.9 + id % 10);
            product.put("originalPrice", 19.9 + id % 10);
            product.put("mainImage", "https://via.placeholder.com/200x200");
            product.put("sales", 100 + id * 10);
            products.add(product);
        }

        result.put("list", products);
        result.put("total", 20);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }
}
