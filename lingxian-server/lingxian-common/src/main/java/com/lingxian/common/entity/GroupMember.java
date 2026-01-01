package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拼团成员实体
 */
@Data
@TableName("t_group_member")
public class GroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private Long orderId;
    private Integer isLeader;
    private LocalDateTime joinTime;

    // 非数据库字段
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
}
