package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录实体
 */
@Data
@TableName("t_points_record")
public class PointsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type;
    private Integer points;
    private Integer balance;
    private String description;
    private Long orderId;
    private LocalDateTime createTime;
}
