package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.AdminUser;
import com.lingxian.common.entity.Permission;
import com.lingxian.common.entity.Role;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.AdminUserService;
import com.lingxian.common.service.PermissionService;
import com.lingxian.common.service.RoleService;
import com.lingxian.common.storage.StorageServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
@Tag(name = "管理后台-系统管理", description = "系统管理相关接口")
public class AdminSystemController {

    private final StorageServiceFactory storageServiceFactory;
    private final AdminUserService adminUserService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ============ 管理员管理 ============

    @GetMapping("/admins")
    @Operation(summary = "获取管理员列表")
    public Result<PageResult<AdminUser>> getAdminList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取管理员列表: page={}, size={}, username={}, status={}", page, size, username, status);

        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(AdminUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(AdminUser::getStatus, status);
        }
        wrapper.orderByDesc(AdminUser::getCreateTime);

        Page<AdminUser> pageResult = adminUserService.page(new Page<>(page, size), wrapper);

        // 填充角色名称
        for (AdminUser admin : pageResult.getRecords()) {
            admin.setPassword(null); // 不返回密码
            if (admin.getRoleId() != null) {
                Role role = roleService.getById(admin.getRoleId());
                if (role != null) {
                    admin.setRoleName(role.getName());
                }
            }
        }

        return Result.success(PageResult.of(
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getRecords()
        ));
    }

    @PostMapping("/admins")
    @Operation(summary = "创建管理员")
    public Result<AdminUser> createAdmin(@RequestBody AdminUser adminUser) {
        log.info("创建管理员: username={}", adminUser.getUsername());

        // 检查用户名是否已存在
        AdminUser existUser = adminUserService.getByUsername(adminUser.getUsername());
        if (existUser != null) {
            return Result.failed("用户名已存在");
        }

        // 密码加密
        if (StringUtils.hasText(adminUser.getPassword())) {
            adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        } else {
            // 默认密码
            adminUser.setPassword(passwordEncoder.encode("123456"));
        }

        // 设置默认值
        if (adminUser.getStatus() == null) {
            adminUser.setStatus(1);
        }

        adminUserService.save(adminUser);
        adminUser.setPassword(null);
        return Result.success(adminUser);
    }

    @PutMapping("/admins/{id}")
    @Operation(summary = "更新管理员")
    public Result<Void> updateAdmin(@PathVariable Long id, @RequestBody AdminUser adminUser) {
        log.info("更新管理员: id={}", id);

        AdminUser existUser = adminUserService.getById(id);
        if (existUser == null) {
            return Result.failed("管理员不存在");
        }

        adminUser.setId(id);

        // 如果传入了密码，进行加密
        if (StringUtils.hasText(adminUser.getPassword())) {
            adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        } else {
            // 不更新密码
            adminUser.setPassword(null);
        }

        // 用户名不允许修改
        adminUser.setUsername(null);

        adminUserService.updateById(adminUser);
        return Result.success();
    }

    @DeleteMapping("/admins/{id}")
    @Operation(summary = "删除管理员")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        log.info("删除管理员: id={}", id);

        AdminUser admin = adminUserService.getById(id);
        if (admin == null) {
            return Result.failed("管理员不存在");
        }

        // 不允许删除超级管理员
        if ("admin".equals(admin.getUsername())) {
            return Result.failed("不能删除超级管理员");
        }

        adminUserService.removeById(id);
        return Result.success();
    }

    @PutMapping("/admins/{id}/reset-password")
    @Operation(summary = "重置管理员密码")
    public Result<Void> resetAdminPassword(@PathVariable Long id) {
        log.info("重置管理员密码: id={}", id);

        AdminUser admin = adminUserService.getById(id);
        if (admin == null) {
            return Result.failed("管理员不存在");
        }

        AdminUser update = new AdminUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode("123456"));
        adminUserService.updateById(update);

        return Result.success();
    }

    // ============ 角色管理 ============

    @GetMapping("/roles")
    @Operation(summary = "获取角色列表")
    public Result<List<Role>> getRoleList() {
        log.info("获取角色列表");

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getSort);
        List<Role> roles = roleService.list(wrapper);

        // 填充管理员数量和权限ID列表
        for (Role role : roles) {
            role.setAdminCount(adminUserService.countByRoleId(role.getId()));
            role.setPermissionIds(permissionService.getPermissionIdsByRoleId(role.getId()));
        }

        return Result.success(roles);
    }

    @PostMapping("/roles")
    @Operation(summary = "创建角色")
    public Result<Role> createRole(@RequestBody Role role) {
        log.info("创建角色: name={}", role.getName());

        // 检查角色编码是否已存在
        Role existRole = roleService.getByCode(role.getCode());
        if (existRole != null) {
            return Result.failed("角色编码已存在");
        }

        // 设置默认值
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getSort() == null) {
            role.setSort(0);
        }

        roleService.save(role);
        return Result.success(role);
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "更新角色")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Role role) {
        log.info("更新角色: id={}", id);

        Role existRole = roleService.getById(id);
        if (existRole == null) {
            return Result.failed("角色不存在");
        }

        // 不允许修改超级管理员角色编码
        if ("ROLE_ADMIN".equals(existRole.getCode()) && !existRole.getCode().equals(role.getCode())) {
            return Result.failed("不能修改超级管理员角色编码");
        }

        role.setId(id);
        roleService.updateById(role);

        // 更新权限关联
        if (role.getPermissionIds() != null) {
            roleService.updateRolePermissions(id, role.getPermissionIds());
        }

        return Result.success();
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        log.info("删除角色: id={}", id);

        Role role = roleService.getById(id);
        if (role == null) {
            return Result.failed("角色不存在");
        }

        // 不允许删除超级管理员角色
        if ("ROLE_ADMIN".equals(role.getCode())) {
            return Result.failed("不能删除超级管理员角色");
        }

        // 检查是否有管理员使用该角色
        Integer count = adminUserService.countByRoleId(id);
        if (count > 0) {
            return Result.failed("该角色下还有管理员，不能删除");
        }

        roleService.removeById(id);
        return Result.success();
    }

    // ============ 权限管理 ============

    @GetMapping("/permissions")
    @Operation(summary = "获取权限树")
    public Result<List<Permission>> getPermissionTree() {
        log.info("获取权限树");
        List<Permission> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }

    // ============ 系统配置 ============

    @GetMapping("/configs")
    @Operation(summary = "获取系统配置")
    public Result<Map<String, Object>> getConfigs() {
        log.info("获取系统配置");

        Map<String, Object> configs = new HashMap<>();

        // 基础设置
        Map<String, Object> basic = new HashMap<>();
        basic.put("siteName", "铃鲜好物");
        basic.put("siteLogo", "https://placeholder.com/logo.png");
        basic.put("siteDescription", "新鲜好物，送货到家");
        basic.put("contactPhone", "400-123-4567");
        basic.put("contactEmail", "service@lingxian.com");
        basic.put("icp", "粤ICP备12345678号");
        configs.put("basic", basic);

        // 订单设置
        Map<String, Object> order = new HashMap<>();
        order.put("autoCancelMinutes", 30);
        order.put("autoReceiveDays", 7);
        order.put("autoCompleteHours", 24);
        order.put("refundDeadlineDays", 7);
        configs.put("order", order);

        // 配送设置
        Map<String, Object> delivery = new HashMap<>();
        delivery.put("freeShippingAmount", 49);
        delivery.put("defaultFreight", 5);
        delivery.put("deliveryTimeStart", "08:00");
        delivery.put("deliveryTimeEnd", "22:00");
        configs.put("delivery", delivery);

        // 积分设置
        Map<String, Object> points = new HashMap<>();
        points.put("orderPointsRate", 1);
        points.put("pointsToMoneyRate", 100);
        points.put("signInPoints", 5);
        points.put("maxPointsDeduction", 50);
        configs.put("points", points);

        // 佣金设置
        Map<String, Object> commission = new HashMap<>();
        commission.put("defaultRate", 5);
        commission.put("settlementCycle", 7);
        commission.put("minWithdrawAmount", 100);
        configs.put("commission", commission);

        return Result.success(configs);
    }

    @PutMapping("/configs")
    @Operation(summary = "更新系统配置")
    public Result<Void> updateConfigs(@RequestBody Map<String, Object> body) {
        log.info("更新系统配置: body={}", body);
        return Result.success();
    }

    // ============ 存储测试 ============

    @PostMapping("/storage/test")
    @Operation(summary = "测试存储连接")
    public Result<Map<String, Object>> testStorage(@RequestBody Map<String, Object> body) {
        String type = (String) body.getOrDefault("type", "local");
        log.info("测试存储连接: type={}", type);

        Map<String, Object> result = new HashMap<>();
        result.put("type", type);

        try {
            boolean success = storageServiceFactory.testConnection(type);
            result.put("success", success);
            result.put("message", success ? "连接成功" : "连接失败");

            if (success) {
                return Result.success(result);
            } else {
                return Result.failed("存储连接测试失败");
            }
        } catch (Exception e) {
            log.error("存储连接测试异常", e);
            result.put("success", false);
            result.put("message", "连接异常: " + e.getMessage());
            return Result.failed("存储连接测试异常: " + e.getMessage());
        }
    }
}
