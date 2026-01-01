package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.PointsRecord;
import com.lingxian.common.mapper.PointsRecordMapper;
import com.lingxian.common.service.PointsRecordService;
import org.springframework.stereotype.Service;

@Service
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements PointsRecordService {
}
