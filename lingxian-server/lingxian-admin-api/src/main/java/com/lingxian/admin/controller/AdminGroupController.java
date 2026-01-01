package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/groups")
@RequiredArgsConstructor
@Tag(name = "管理后台-拼团管理", description = "拼团活动管理相关接口")
public class AdminGroupController {

    private final GroupActivityService groupActivityService;
    private final GroupRecordService groupRecordService;
    private final GroupMemberService groupMemberService;
    private final ProductService productService;
    private final MerchantService merchantService;
    private final UserService userService;

    private static final String[] ACTIVITY_STATUS_NAMES = {"未开始", "进行中", "已结束", "已下架"};
    private static final String[] RECORD_STATUS_NAMES = {"拼团中", "拼团成功", "拼团失败"};

    @GetMapping("/activities")
    @Operation(summary = "获取拼团活动列表")
    public Result<PageResult<GroupActivity>> getActivityList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "活动名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取拼团活动列表: page={}, size={}, name={}, status={}", page, size, name, status);

        LambdaQueryWrapper<GroupActivity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(GroupActivity::getName, name);
        }
        if (status != null) {
            wrapper.eq(GroupActivity::getStatus, status);
        }
        wrapper.orderByDesc(GroupActivity::getCreateTime);

        Page<GroupActivity> pageResult = groupActivityService.page(new Page<>(page, size), wrapper);

        // 填充额外信息
        for (GroupActivity activity : pageResult.getRecords()) {
            fillActivityInfo(activity);
        }

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "获取拼团活动详情")
    public Result<Map<String, Object>> getActivityDetail(@PathVariable Long id) {
        log.info("获取拼团活动详情: id={}", id);

        GroupActivity activity = groupActivityService.getById(id);
        if (activity == null) {
            return Result.failed("拼团活动不存在");
        }

        fillActivityInfo(activity);

        // 统计数据
        long successGroupCount = groupRecordService.count(new LambdaQueryWrapper<GroupRecord>()
                .eq(GroupRecord::getActivityId, id)
                .eq(GroupRecord::getStatus, 1));
        long pendingGroupCount = groupRecordService.count(new LambdaQueryWrapper<GroupRecord>()
                .eq(GroupRecord::getActivityId, id)
                .eq(GroupRecord::getStatus, 0));
        long failedGroupCount = groupRecordService.count(new LambdaQueryWrapper<GroupRecord>()
                .eq(GroupRecord::getActivityId, id)
                .eq(GroupRecord::getStatus, 2));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", activity.getSoldCount());
        stats.put("pendingGroups", pendingGroupCount);
        stats.put("successGroups", successGroupCount);
        stats.put("failedGroups", failedGroupCount);

        Map<String, Object> result = new HashMap<>();
        result.put("id", activity.getId());
        result.put("name", activity.getName());
        result.put("productId", activity.getProductId());
        result.put("productName", activity.getProductName());
        result.put("productImage", activity.getProductImage());
        result.put("merchantId", activity.getMerchantId());
        result.put("merchantName", activity.getMerchantName());
        result.put("originalPrice", activity.getOriginalPrice());
        result.put("groupPrice", activity.getGroupPrice());
        result.put("groupSize", activity.getGroupSize());
        result.put("limitPerUser", activity.getLimitPerUser());
        result.put("stock", activity.getStock());
        result.put("soldCount", activity.getSoldCount());
        result.put("groupCount", activity.getGroupCount());
        result.put("successGroupCount", successGroupCount);
        result.put("status", activity.getStatus());
        result.put("statusName", activity.getStatusName());
        result.put("expireHours", activity.getExpireHours());
        result.put("autoCancel", activity.getAutoCancel());
        result.put("startTime", activity.getStartTime());
        result.put("endTime", activity.getEndTime());
        result.put("createTime", activity.getCreateTime());
        result.put("description", activity.getDescription());
        result.put("stats", stats);

        return Result.success(result);
    }

    @PostMapping("/activities")
    @Operation(summary = "创建拼团活动")
    public Result<GroupActivity> createActivity(@RequestBody GroupActivity activity) {
        log.info("创建拼团活动: activity={}", activity);

        // 校验商品是否存在
        Product product = productService.getById(activity.getProductId());
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 设置商户ID
        if (activity.getMerchantId() == null) {
            activity.setMerchantId(product.getMerchantId());
        }

        // 初始化字段
        activity.setSoldCount(0);
        activity.setGroupCount(0);

        // 根据时间设置状态
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime().isAfter(now)) {
            activity.setStatus(0); // 未开始
        } else if (activity.getEndTime().isBefore(now)) {
            activity.setStatus(2); // 已结束
        } else {
            activity.setStatus(1); // 进行中
        }

        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        groupActivityService.save(activity);

        return Result.success(activity);
    }

    @PutMapping("/activities/{id}")
    @Operation(summary = "更新拼团活动")
    public Result<Void> updateActivity(@PathVariable Long id, @RequestBody GroupActivity activity) {
        log.info("更新拼团活动: id={}, activity={}", id, activity);

        GroupActivity existing = groupActivityService.getById(id);
        if (existing == null) {
            return Result.failed("拼团活动不存在");
        }

        activity.setId(id);
        activity.setUpdateTime(LocalDateTime.now());

        // 保持原有的统计数据
        activity.setSoldCount(existing.getSoldCount());
        activity.setGroupCount(existing.getGroupCount());

        // 根据时间更新状态（如果状态不是已下架）
        if (existing.getStatus() != 3) {
            LocalDateTime now = LocalDateTime.now();
            if (activity.getStartTime() != null && activity.getEndTime() != null) {
                if (activity.getStartTime().isAfter(now)) {
                    activity.setStatus(0);
                } else if (activity.getEndTime().isBefore(now)) {
                    activity.setStatus(2);
                } else {
                    activity.setStatus(1);
                }
            }
        } else {
            activity.setStatus(3); // 保持下架状态
        }

        groupActivityService.updateById(activity);
        return Result.success();
    }

    @PutMapping("/activities/{id}/status")
    @Operation(summary = "更新拼团活动状态")
    public Result<Void> updateActivityStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        log.info("更新拼团活动状态: id={}, status={}", id, status);

        GroupActivity activity = groupActivityService.getById(id);
        if (activity == null) {
            return Result.failed("拼团活动不存在");
        }

        activity.setStatus(status);
        activity.setUpdateTime(LocalDateTime.now());
        groupActivityService.updateById(activity);
        return Result.success();
    }

    @GetMapping("/records")
    @Operation(summary = "获取拼团记录列表")
    public Result<PageResult<GroupRecord>> getRecordList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "活动ID") @RequestParam(required = false) Long activityId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取拼团记录列表: page={}, size={}, activityId={}, status={}", page, size, activityId, status);

        LambdaQueryWrapper<GroupRecord> wrapper = new LambdaQueryWrapper<>();
        if (activityId != null) {
            wrapper.eq(GroupRecord::getActivityId, activityId);
        }
        if (status != null) {
            wrapper.eq(GroupRecord::getStatus, status);
        }
        wrapper.orderByDesc(GroupRecord::getCreateTime);

        Page<GroupRecord> pageResult = groupRecordService.page(new Page<>(page, size), wrapper);

        // 填充额外信息
        for (GroupRecord record : pageResult.getRecords()) {
            fillRecordInfo(record);
        }

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    private void fillActivityInfo(GroupActivity activity) {
        // 填充商品信息
        if (activity.getProductId() != null) {
            Product product = productService.getById(activity.getProductId());
            if (product != null) {
                activity.setProductName(product.getName());
                activity.setProductImage(product.getImage());
            }
        }

        // 填充商户名称
        if (activity.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(activity.getMerchantId());
            if (merchant != null) {
                activity.setMerchantName(merchant.getName());
            }
        }

        // 填充状态名称
        if (activity.getStatus() != null && activity.getStatus() >= 0 && activity.getStatus() < ACTIVITY_STATUS_NAMES.length) {
            activity.setStatusName(ACTIVITY_STATUS_NAMES[activity.getStatus()]);
        }
    }

    private void fillRecordInfo(GroupRecord record) {
        // 填充活动信息
        if (record.getActivityId() != null) {
            GroupActivity activity = groupActivityService.getById(record.getActivityId());
            if (activity != null) {
                record.setActivityName(activity.getName());
                record.setProductId(activity.getProductId());

                if (activity.getProductId() != null) {
                    Product product = productService.getById(activity.getProductId());
                    if (product != null) {
                        record.setProductName(product.getName());
                        record.setProductImage(product.getImage());
                    }
                }
            }
        }

        // 填充团长信息
        if (record.getLeaderId() != null) {
            User leader = userService.getById(record.getLeaderId());
            if (leader != null) {
                record.setLeaderNickname(leader.getNickname());
                record.setLeaderAvatar(leader.getAvatar());
            }
        }

        // 填充状态名称
        if (record.getStatus() != null && record.getStatus() >= 0 && record.getStatus() < RECORD_STATUS_NAMES.length) {
            record.setStatusName(RECORD_STATUS_NAMES[record.getStatus()]);
        }

        // 获取成员列表
        List<GroupMember> members = groupMemberService.list(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, record.getId())
                        .orderByAsc(GroupMember::getJoinTime)
        );

        // 填充成员用户信息
        for (GroupMember member : members) {
            if (member.getUserId() != null) {
                User user = userService.getById(member.getUserId());
                if (user != null) {
                    member.setNickname(user.getNickname());
                    member.setAvatar(user.getAvatar());
                }
            }
        }

        record.setMembers(members);
    }
}
