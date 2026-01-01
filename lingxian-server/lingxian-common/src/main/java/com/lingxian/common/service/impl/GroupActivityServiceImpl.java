package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.GroupActivity;
import com.lingxian.common.mapper.GroupActivityMapper;
import com.lingxian.common.service.GroupActivityService;
import org.springframework.stereotype.Service;

@Service
public class GroupActivityServiceImpl extends ServiceImpl<GroupActivityMapper, GroupActivity> implements GroupActivityService {
}
