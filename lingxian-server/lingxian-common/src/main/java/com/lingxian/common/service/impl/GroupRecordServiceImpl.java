package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.GroupRecord;
import com.lingxian.common.mapper.GroupRecordMapper;
import com.lingxian.common.service.GroupRecordService;
import org.springframework.stereotype.Service;

@Service
public class GroupRecordServiceImpl extends ServiceImpl<GroupRecordMapper, GroupRecord> implements GroupRecordService {
}
