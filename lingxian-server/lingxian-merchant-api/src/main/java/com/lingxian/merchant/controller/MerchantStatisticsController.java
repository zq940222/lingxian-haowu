package com.lingxian.merchant.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/statistics")
@RequiredArgsConstructor
@Tag(name = "商户端-数据统计", description = "商户数据统计接口")
public class MerchantStatisticsController {

    @GetMapping("/sales")
    @Operation(summary = "销售统计")
    public Result<Map<String, Object>> getSalesStatistics(
            @RequestParam(defaultValue = "week") String period) {

        Map<String, Object> data = new HashMap<>();
        Random random = new Random();

        // 汇总数据
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAmount", new BigDecimal("18680.50"));
        summary.put("totalOrders", 256);
        summary.put("avgOrderAmount", new BigDecimal("72.97"));
        summary.put("compareLastPeriod", new BigDecimal("15.8")); // 较上期增长%
        data.put("summary", summary);

        // 趋势数据
        List<Map<String, Object>> trends = new ArrayList<>();
        int days = period.equals("week") ? 7 : (period.equals("month") ? 30 : 12);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = period.equals("year") ?
            DateTimeFormatter.ofPattern("yyyy-MM") : DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            LocalDate date = period.equals("year") ? today.minusMonths(i) : today.minusDays(i);
            item.put("date", date.format(formatter));
            item.put("amount", new BigDecimal(1500 + random.nextInt(2000)));
            item.put("orders", 20 + random.nextInt(30));
            trends.add(item);
        }
        data.put("trends", trends);

        return Result.success(data);
    }

    @GetMapping("/orders")
    @Operation(summary = "订单统计")
    public Result<Map<String, Object>> getOrderStatistics(
            @RequestParam(defaultValue = "week") String period) {

        Map<String, Object> data = new HashMap<>();
        Random random = new Random();

        // 汇总数据
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", 256);
        summary.put("completedOrders", 240);
        summary.put("canceledOrders", 12);
        summary.put("refundOrders", 4);
        summary.put("completionRate", new BigDecimal("93.75"));
        data.put("summary", summary);

        // 订单状态分布
        List<Map<String, Object>> statusDist = new ArrayList<>();
        String[] statuses = {"待接单", "待配送", "配送中", "已完成", "已取消"};
        int[] values = {8, 15, 5, 240, 12};
        for (int i = 0; i < statuses.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statuses[i]);
            item.put("value", values[i]);
            statusDist.add(item);
        }
        data.put("statusDistribution", statusDist);

        // 趋势数据
        List<Map<String, Object>> trends = new ArrayList<>();
        int days = period.equals("week") ? 7 : 30;
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", today.minusDays(i).format(DateTimeFormatter.ofPattern("MM-dd")));
            item.put("total", 25 + random.nextInt(20));
            item.put("completed", 22 + random.nextInt(18));
            trends.add(item);
        }
        data.put("trends", trends);

        return Result.success(data);
    }

    @GetMapping("/products")
    @Operation(summary = "商品统计")
    public Result<Map<String, Object>> getProductStatistics(
            @RequestParam(defaultValue = "week") String period) {

        Map<String, Object> data = new HashMap<>();
        Random random = new Random();

        // 汇总数据
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", 86);
        summary.put("onSaleProducts", 72);
        summary.put("offSaleProducts", 14);
        summary.put("lowStockProducts", 5);
        data.put("summary", summary);

        // 热销商品TOP10
        List<Map<String, Object>> hotProducts = new ArrayList<>();
        String[] names = {"新鲜蔬菜套餐", "精选水果拼盘", "有机鸡蛋", "进口牛肉", "海鲜礼盒",
                         "农家土鸡", "有机牛奶", "手工水饺", "现磨豆浆", "五谷杂粮"};
        for (int i = 0; i < 10; i++) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", i + 1);
            product.put("name", names[i]);
            product.put("image", "https://via.placeholder.com/60x60");
            product.put("sales", 150 - i * 12);
            product.put("amount", new BigDecimal(4200 - i * 350));
            hotProducts.add(product);
        }
        data.put("hotProducts", hotProducts);

        // 分类销售占比
        List<Map<String, Object>> categoryDist = new ArrayList<>();
        String[] categories = {"蔬菜", "水果", "肉类", "海鲜", "蛋奶", "其他"};
        int[] percentages = {25, 22, 18, 15, 12, 8};
        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", categories[i]);
            item.put("value", percentages[i]);
            categoryDist.add(item);
        }
        data.put("categoryDistribution", categoryDist);

        return Result.success(data);
    }
}
