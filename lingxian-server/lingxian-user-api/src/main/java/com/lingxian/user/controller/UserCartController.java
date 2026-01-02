package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingxian.common.entity.Cart;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.Product;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CartService;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户购物车控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/cart")
@RequiredArgsConstructor
@Tag(name = "用户端-购物车", description = "购物车相关接口")
public class UserCartController {

    private final CartService cartService;
    private final ProductService productService;
    private final MerchantService merchantService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取购物车列表")
    public Result<Map<String, Object>> getCartList(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("获取购物车列表: userId={}", userId);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        // 查询购物车列表 (@TableLogic 会自动过滤 deleted=1 的记录)
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getUpdateTime);

        List<Cart> cartList = cartService.list(queryWrapper);

        // 统计数据
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal selectedPrice = BigDecimal.ZERO;
        int totalCount = 0;
        int selectedCount = 0;

        // 按商户分组的数据
        List<Map<String, Object>> merchantGroups = new ArrayList<>();

        if (!cartList.isEmpty()) {
            // 获取所有商品ID和商户ID
            List<Long> productIds = cartList.stream()
                    .map(Cart::getProductId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Long> merchantIds = cartList.stream()
                    .map(Cart::getMerchantId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询商品和商户信息
            List<Product> products = productService.listByIds(productIds);
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            Map<Long, Merchant> merchantMap = new HashMap<>();
            if (!merchantIds.isEmpty()) {
                List<Merchant> merchants = merchantService.listByIds(merchantIds);
                merchantMap = merchants.stream()
                        .collect(Collectors.toMap(Merchant::getId, m -> m));
            }

            // 按商户分组购物车商品
            Map<Long, List<Cart>> cartByMerchant = cartList.stream()
                    .filter(c -> c.getMerchantId() != null)
                    .collect(Collectors.groupingBy(Cart::getMerchantId));

            // 构建按商户分组的返回数据
            for (Map.Entry<Long, List<Cart>> entry : cartByMerchant.entrySet()) {
                Long merchantId = entry.getKey();
                List<Cart> merchantCarts = entry.getValue();
                Merchant merchant = merchantMap.get(merchantId);

                Map<String, Object> merchantGroup = new HashMap<>();
                merchantGroup.put("merchantId", merchantId);
                merchantGroup.put("merchantName", merchant != null ? merchant.getName() : "未知商户");
                merchantGroup.put("merchantLogo", merchant != null ? imageUrlUtil.generateUrl(merchant.getLogo()) : null);

                List<Map<String, Object>> items = new ArrayList<>();
                BigDecimal merchantTotalPrice = BigDecimal.ZERO;
                BigDecimal merchantSelectedPrice = BigDecimal.ZERO;
                boolean allSelected = true;

                for (Cart cart : merchantCarts) {
                    Product product = productMap.get(cart.getProductId());
                    if (product == null) {
                        continue;
                    }

                    Map<String, Object> item = new HashMap<>();
                    item.put("id", cart.getId());
                    item.put("productId", cart.getProductId());
                    item.put("merchantId", merchantId);
                    item.put("productName", product.getName());
                    item.put("productImage", imageUrlUtil.generateUrl(product.getImage()));
                    item.put("price", product.getPrice());
                    item.put("quantity", cart.getQuantity());
                    item.put("selected", cart.getSelected() == 1);
                    item.put("stock", product.getStock());
                    item.put("status", product.getStatus());

                    // 计算小计
                    BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
                    item.put("subtotal", subtotal);

                    items.add(item);

                    totalCount += cart.getQuantity();
                    totalPrice = totalPrice.add(subtotal);
                    merchantTotalPrice = merchantTotalPrice.add(subtotal);

                    if (cart.getSelected() == 1) {
                        selectedCount += cart.getQuantity();
                        selectedPrice = selectedPrice.add(subtotal);
                        merchantSelectedPrice = merchantSelectedPrice.add(subtotal);
                    } else {
                        allSelected = false;
                    }
                }

                merchantGroup.put("items", items);
                merchantGroup.put("totalPrice", merchantTotalPrice);
                merchantGroup.put("selectedPrice", merchantSelectedPrice);
                merchantGroup.put("allSelected", allSelected);
                merchantGroups.add(merchantGroup);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("merchantGroups", merchantGroups);
        result.put("totalCount", totalCount);
        result.put("totalPrice", totalPrice);
        result.put("selectedCount", selectedCount);
        result.put("selectedPrice", selectedPrice);

        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "添加商品到购物车")
    public Result<Void> addToCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        Long productId = body.get("productId") != null ? Long.parseLong(body.get("productId").toString()) : null;
        Integer quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;

        log.info("添加到购物车: userId={}, productId={}, quantity={}", userId, productId, quantity);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        if (productId == null) {
            return Result.failed("商品ID不能为空");
        }

        // 检查商品是否存在且上架
        Product product = productService.getById(productId);
        if (product == null) {
            return Result.failed("商品不存在");
        }
        if (product.getStatus() != 1) {
            return Result.failed("商品已下架");
        }

        // 检查库存
        if (product.getStock() < quantity) {
            return Result.failed("库存不足");
        }

        // 查询购物车是否已有该商品 (@TableLogic 会自动过滤 deleted=1 的记录)
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);

        Cart existCart = cartService.getOne(queryWrapper);

        if (existCart != null) {
            // 已存在，增加数量
            int newQuantity = existCart.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                return Result.failed("库存不足");
            }
            existCart.setQuantity(newQuantity);
            existCart.setUpdateTime(LocalDateTime.now());
            cartService.updateById(existCart);
        } else {
            // 不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setMerchantId(product.getMerchantId());
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setPrice(product.getPrice());
            cart.setSelected(1);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cart.setDeleted(0);
            cartService.save(cart);
        }

        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新购物车商品数量")
    public Result<Void> updateQuantity(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : null;

        log.info("更新购物车数量: userId={}, cartId={}, quantity={}", userId, id, quantity);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        if (quantity == null || quantity < 1) {
            return Result.failed("数量必须大于0");
        }

        // 查询购物车项
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId) || cart.getDeleted() == 1) {
            return Result.failed("购物车商品不存在");
        }

        // 检查库存
        Product product = productService.getById(cart.getProductId());
        if (product != null && quantity > product.getStock()) {
            return Result.failed("库存不足");
        }

        cart.setQuantity(quantity);
        cart.setUpdateTime(LocalDateTime.now());
        cartService.updateById(cart);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除购物车商品")
    public Result<Void> removeFromCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("删除购物车商品: userId={}, cartId={}", userId, id);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        // 查询购物车项
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.failed("购物车商品不存在");
        }

        // 使用 removeById 触发 @TableLogic 逻辑删除
        cartService.removeById(id);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "清空购物车")
    public Result<Void> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("清空购物车: userId={}", userId);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        // 使用 remove 触发 @TableLogic 逻辑删除
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId);
        cartService.remove(queryWrapper);

        return Result.success();
    }

    @PutMapping("/{id}/select")
    @Operation(summary = "选中/取消选中购物车商品")
    public Result<Void> selectItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Boolean selected = (Boolean) body.get("selected");

        log.info("选中购物车商品: userId={}, cartId={}, selected={}", userId, id, selected);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId) || cart.getDeleted() == 1) {
            return Result.failed("购物车商品不存在");
        }

        cart.setSelected(Boolean.TRUE.equals(selected) ? 1 : 0);
        cart.setUpdateTime(LocalDateTime.now());
        cartService.updateById(cart);

        return Result.success();
    }

    @PutMapping("/select-all")
    @Operation(summary = "全选/取消全选")
    public Result<Void> selectAll(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        Boolean selected = (Boolean) body.get("selected");

        log.info("全选购物车: userId={}, selected={}", userId, selected);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getDeleted, 0)
                .set(Cart::getSelected, Boolean.TRUE.equals(selected) ? 1 : 0)
                .set(Cart::getUpdateTime, LocalDateTime.now());

        cartService.update(updateWrapper);

        return Result.success();
    }

    @PutMapping("/merchant/{merchantId}/select")
    @Operation(summary = "选中/取消选中某商户的所有商品")
    public Result<Void> selectMerchant(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long merchantId,
            @RequestBody Map<String, Object> body) {
        Boolean selected = (Boolean) body.get("selected");

        log.info("选中商户商品: userId={}, merchantId={}, selected={}", userId, merchantId, selected);

        if (userId == null) {
            return Result.failed(401, "请先登录");
        }

        LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getMerchantId, merchantId)
                .eq(Cart::getDeleted, 0)
                .set(Cart::getSelected, Boolean.TRUE.equals(selected) ? 1 : 0)
                .set(Cart::getUpdateTime, LocalDateTime.now());

        cartService.update(updateWrapper);

        return Result.success();
    }
}
