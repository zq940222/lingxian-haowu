package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user/groups")
@RequiredArgsConstructor
@Tag(name = "用户端-拼团", description = "拼团相关接口")
public class UserGroupController {

    private final GroupActivityService groupActivityService;
    private final GroupRecordService groupRecordService;
    private final GroupMemberService groupMemberService;
    private final ProductService productService;
    private final UserService userService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping("/activities")
    @Operation(summary = "获取拼团活动列表")
    public Result<Map<String, Object>> getActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取拼团活动列表: page={}, pageSize={}", page, pageSize);

        LocalDateTime now = LocalDateTime.now();
        Page<GroupActivity> pageResult = groupActivityService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<GroupActivity>()
                        .eq(GroupActivity::getStatus, 1) // 进行中
                        .le(GroupActivity::getStartTime, now)
                        .ge(GroupActivity::getEndTime, now)
                        .orderByDesc(GroupActivity::getCreateTime)
        );

        // 填充商品信息
        for (GroupActivity activity : pageResult.getRecords()) {
            fillActivityProductInfo(activity);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "获取拼团活动详情")
    public Result<Map<String, Object>> getActivityDetail(@PathVariable Long id) {
        log.info("获取拼团活动详情: id={}", id);

        GroupActivity activity = groupActivityService.getById(id);
        if (activity == null) {
            return Result.failed("拼团活动不存在");
        }

        // 获取商品信息
        Product product = productService.getById(activity.getProductId());

        Map<String, Object> result = new HashMap<>();
        result.put("id", activity.getId());
        result.put("name", activity.getName());
        result.put("productId", activity.getProductId());
        result.put("productName", product != null ? product.getName() : "");
        result.put("productDesc", product != null ? product.getDescription() : "");
        result.put("productImage", product != null ? imageUrlUtil.generateUrl(product.getImage()) : "");
        result.put("productImages", product != null ? imageUrlUtil.generateUrlsFromJson(product.getImages()) : "[]");
        result.put("originalPrice", activity.getOriginalPrice());
        result.put("groupPrice", activity.getGroupPrice());
        result.put("groupSize", activity.getGroupSize());
        result.put("timeLimit", activity.getExpireHours());
        result.put("soldCount", activity.getSoldCount());
        result.put("stock", activity.getStock());
        result.put("status", activity.getStatus());
        result.put("startTime", activity.getStartTime());
        result.put("endTime", activity.getEndTime());

        return Result.success(result);
    }

    @GetMapping("/activities/{activityId}/records")
    @Operation(summary = "获取活动的进行中拼团")
    public Result<List<Map<String, Object>>> getOngoingGroups(@PathVariable Long activityId) {
        log.info("获取进行中的拼团: activityId={}", activityId);

        LocalDateTime now = LocalDateTime.now();
        List<GroupRecord> records = groupRecordService.list(
                new LambdaQueryWrapper<GroupRecord>()
                        .eq(GroupRecord::getActivityId, activityId)
                        .eq(GroupRecord::getStatus, 0) // 拼团中
                        .gt(GroupRecord::getExpireTime, now)
                        .orderByAsc(GroupRecord::getExpireTime)
                        .last("LIMIT 10")
        );

        List<Map<String, Object>> result = records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("groupNo", record.getGroupNo());
            map.put("currentSize", record.getCurrentSize());
            map.put("groupSize", record.getGroupSize());
            map.put("needCount", record.getGroupSize() - record.getCurrentSize());

            // 计算剩余时间
            long remainSeconds = java.time.Duration.between(now, record.getExpireTime()).getSeconds();
            if (remainSeconds > 3600) {
                map.put("remainTime", (remainSeconds / 3600) + "小时");
            } else if (remainSeconds > 60) {
                map.put("remainTime", (remainSeconds / 60) + "分钟");
            } else {
                map.put("remainTime", remainSeconds + "秒");
            }

            // 团长信息
            User leader = userService.getById(record.getLeaderId());
            if (leader != null) {
                map.put("leaderId", leader.getId());
                map.put("leaderNickname", leader.getNickname());
                map.put("leaderAvatar", imageUrlUtil.generateUrl(leader.getAvatar()));
            }

            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的拼团记录")
    public Result<Map<String, Object>> getMyGroups(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        log.info("获取我的拼团记录: userId={}, page={}, pageSize={}, status={}", userId, page, pageSize, status);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        // 查询我参与的拼团
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getUserId, userId);
        List<GroupMember> myMembers = groupMemberService.list(memberWrapper);
        List<Long> groupIds = myMembers.stream().map(GroupMember::getGroupId).collect(Collectors.toList());

        if (groupIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", List.of());
            result.put("total", 0);
            return Result.success(result);
        }

        LambdaQueryWrapper<GroupRecord> wrapper = new LambdaQueryWrapper<GroupRecord>()
                .in(GroupRecord::getId, groupIds);
        if (status != null) {
            wrapper.eq(GroupRecord::getStatus, status);
        }
        wrapper.orderByDesc(GroupRecord::getCreateTime);

        Page<GroupRecord> pageResult = groupRecordService.page(new Page<>(page, pageSize), wrapper);

        // 填充信息
        for (GroupRecord record : pageResult.getRecords()) {
            fillRecordInfo(record);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());

        return Result.success(result);
    }

    @PostMapping("/start")
    @Operation(summary = "发起拼团")
    @Transactional
    public Result<Map<String, Object>> startGroup(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        Long activityId = Long.valueOf(body.get("activityId").toString());
        log.info("发起拼团: userId={}, activityId={}", userId, activityId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        // 校验活动
        GroupActivity activity = groupActivityService.getById(activityId);
        if (activity == null) {
            return Result.failed("拼团活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (activity.getStatus() != 1) {
            return Result.failed("活动未在进行中");
        }
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            return Result.failed("活动不在有效期内");
        }
        if (activity.getStock() <= 0) {
            return Result.failed("库存不足");
        }

        // 检查用户是否已有进行中的拼团（同一活动）
        long existingCount = groupRecordService.count(new LambdaQueryWrapper<GroupRecord>()
                .eq(GroupRecord::getActivityId, activityId)
                .eq(GroupRecord::getLeaderId, userId)
                .eq(GroupRecord::getStatus, 0));
        if (existingCount > 0) {
            return Result.failed("您已有进行中的拼团");
        }

        // 创建拼团记录
        GroupRecord record = new GroupRecord();
        record.setGroupNo(generateGroupNo());
        record.setActivityId(activityId);
        record.setLeaderId(userId);
        record.setGroupSize(activity.getGroupSize());
        record.setCurrentSize(1);
        record.setGroupPrice(activity.getGroupPrice());
        record.setStatus(0); // 拼团中
        record.setExpireTime(now.plusHours(activity.getExpireHours() != null ? activity.getExpireHours() : 24));
        record.setCreateTime(now);
        groupRecordService.save(record);

        // 添加团长为成员
        GroupMember member = new GroupMember();
        member.setGroupId(record.getId());
        member.setUserId(userId);
        member.setIsLeader(1);
        member.setJoinTime(now);
        groupMemberService.save(member);

        // 更新活动统计
        activity.setGroupCount((activity.getGroupCount() != null ? activity.getGroupCount() : 0) + 1);
        groupActivityService.updateById(activity);

        Map<String, Object> result = new HashMap<>();
        result.put("groupId", record.getId());
        result.put("groupNo", record.getGroupNo());
        // 实际项目中这里应该创建订单并返回支付参数
        // result.put("payParams", payParams);

        return Result.success(result);
    }

    @PostMapping("/join")
    @Operation(summary = "参与拼团")
    @Transactional
    public Result<Map<String, Object>> joinGroup(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        Long groupId = Long.valueOf(body.get("groupId").toString());
        log.info("参与拼团: userId={}, groupId={}", userId, groupId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        // 校验拼团记录
        GroupRecord record = groupRecordService.getById(groupId);
        if (record == null) {
            return Result.failed("拼团不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (record.getStatus() != 0) {
            return Result.failed("该拼团已结束");
        }
        if (now.isAfter(record.getExpireTime())) {
            return Result.failed("该拼团已过期");
        }
        if (record.getCurrentSize() >= record.getGroupSize()) {
            return Result.failed("该拼团人数已满");
        }

        // 检查用户是否已参与该拼团
        long memberCount = groupMemberService.count(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId));
        if (memberCount > 0) {
            return Result.failed("您已参与该拼团");
        }

        // 校验活动库存
        GroupActivity activity = groupActivityService.getById(record.getActivityId());
        if (activity == null || activity.getStock() <= 0) {
            return Result.failed("库存不足");
        }

        // 添加成员
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setIsLeader(0);
        member.setJoinTime(now);
        groupMemberService.save(member);

        // 更新拼团人数
        record.setCurrentSize(record.getCurrentSize() + 1);

        // 检查是否成团
        if (record.getCurrentSize() >= record.getGroupSize()) {
            record.setStatus(1); // 拼团成功
            record.setCompleteTime(now);

            // 更新活动统计
            activity.setSoldCount((activity.getSoldCount() != null ? activity.getSoldCount() : 0) + record.getGroupSize());
            activity.setStock(activity.getStock() - record.getGroupSize());
            groupActivityService.updateById(activity);
        }

        groupRecordService.updateById(record);

        Map<String, Object> result = new HashMap<>();
        result.put("groupId", record.getId());
        result.put("status", record.getStatus());
        result.put("currentSize", record.getCurrentSize());
        result.put("groupSize", record.getGroupSize());
        // 实际项目中这里应该创建订单并返回支付参数
        // result.put("payParams", payParams);

        return Result.success(result);
    }

    /**
     * 生成团号
     */
    private String generateGroupNo() {
        return "PT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "获取拼团记录详情")
    public Result<Map<String, Object>> getGroupDetail(@PathVariable Long id) {
        log.info("获取拼团记录详情: id={}", id);

        GroupRecord record = groupRecordService.getById(id);
        if (record == null) {
            return Result.failed("拼团记录不存在");
        }

        fillRecordInfo(record);

        // 获取成员列表
        List<GroupMember> members = groupMemberService.list(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, id)
                        .orderByAsc(GroupMember::getJoinTime)
        );

        for (GroupMember member : members) {
            User user = userService.getById(member.getUserId());
            if (user != null) {
                member.setNickname(user.getNickname());
                member.setAvatar(imageUrlUtil.generateUrl(user.getAvatar()));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("groupNo", record.getGroupNo());
        result.put("activityId", record.getActivityId());
        result.put("activityName", record.getActivityName());
        result.put("productId", record.getProductId());
        result.put("productName", record.getProductName());
        result.put("productImage", record.getProductImage());
        result.put("groupSize", record.getGroupSize());
        result.put("currentSize", record.getCurrentSize());
        result.put("groupPrice", record.getGroupPrice());
        result.put("status", record.getStatus());
        result.put("statusName", record.getStatusName());
        result.put("expireTime", record.getExpireTime());
        result.put("completeTime", record.getCompleteTime());
        result.put("createTime", record.getCreateTime());
        result.put("members", members);

        // 团长信息
        User leader = userService.getById(record.getLeaderId());
        if (leader != null) {
            result.put("leaderNickname", leader.getNickname());
            result.put("leaderAvatar", imageUrlUtil.generateUrl(leader.getAvatar()));
        }

        return Result.success(result);
    }

    /**
     * 填充活动的商品信息
     */
    private void fillActivityProductInfo(GroupActivity activity) {
        if (activity.getProductId() != null) {
            Product product = productService.getById(activity.getProductId());
            if (product != null) {
                activity.setProductName(product.getName());
                activity.setProductImage(imageUrlUtil.generateUrl(product.getImage()));
            }
        }
    }

    /**
     * 填充拼团记录信息
     */
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
                        record.setProductImage(imageUrlUtil.generateUrl(product.getImage()));
                    }
                }
            }
        }

        // 状态名称
        String[] statusNames = {"拼团中", "拼团成功", "拼团失败"};
        if (record.getStatus() != null && record.getStatus() >= 0 && record.getStatus() < statusNames.length) {
            record.setStatusName(statusNames[record.getStatus()]);
        }
    }
}
