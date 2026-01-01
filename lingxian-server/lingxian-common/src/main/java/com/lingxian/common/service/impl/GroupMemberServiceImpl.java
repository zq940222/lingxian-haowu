package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.GroupMember;
import com.lingxian.common.mapper.GroupMemberMapper;
import com.lingxian.common.service.GroupMemberService;
import org.springframework.stereotype.Service;

@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements GroupMemberService {
}
