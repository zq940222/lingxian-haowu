package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group_record")
public class GroupRecord extends BaseEntity {

    private String groupNo;
    private Long activityId;
    private Long leaderId;
    private Integer groupSize;
    private Integer currentSize;
    private BigDecimal groupPrice;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime completeTime;

    // 非数据库字段
    @TableField(exist = false)
    private String activityName;
    @TableField(exist = false)
    private Long productId;
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productImage;
    @TableField(exist = false)
    private String leaderNickname;
    @TableField(exist = false)
    private String leaderAvatar;
    @TableField(exist = false)
    private String statusName;
    @TableField(exist = false)
    private List<GroupMember> members;
}
