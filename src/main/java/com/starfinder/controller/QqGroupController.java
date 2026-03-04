package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.QqGroup;
import com.starfinder.entity.User;
import com.starfinder.mapper.QqGroupMapper;
import com.starfinder.mapper.UserMapper;
import com.starfinder.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qq-group")
public class QqGroupController {

    @Autowired
    private QqGroupMapper qqGroupMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public Result<QqGroup> create(@RequestBody Map<String, Object> body) {
        Long userId = AuthContext.getUserId();
        if (userId == null) return Result.BadRequest("需要登录");

        String groupName = (String) body.get("groupName");
        String groupNumber = (String) body.get("groupNumber");
        String description = (String) body.get("description");
        String contactInfo = (String) body.get("contactInfo");

        if (groupName == null || groupName.trim().isEmpty()) return Result.BadRequest("群名称不能为空");
        if (groupNumber == null || groupNumber.trim().isEmpty()) return Result.BadRequest("群号不能为空");

        User user = userMapper.findById(userId);
        if (user == null) return Result.BadRequest("用户不存在");

        QqGroup qqGroup = new QqGroup();
        qqGroup.setGroupName(groupName.trim());
        qqGroup.setGroupNumber(groupNumber.trim());
        qqGroup.setDescription(description != null ? description.trim() : "");
        qqGroup.setContactInfo(contactInfo != null ? contactInfo.trim() : "");
        qqGroup.setUserId(userId);
        qqGroup.setAuthorTag(user.getNickname() != null ? user.getNickname() : user.getEmail());
        qqGroup.setStatus("approved"); // auto-approve for simplicity

        qqGroupMapper.insert(qqGroup);
        return Result.success(qqGroup);
    }

    @GetMapping("/list")
    public Result<List<QqGroup>> list() {
        List<QqGroup> groups = qqGroupMapper.findApproved();
        return Result.success(groups);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = AuthContext.getUserId();
        if (userId == null) return Result.BadRequest("需要登录");

        QqGroup group = qqGroupMapper.findById(id);
        if (group == null) return Result.BadRequest("记录不存在");
        if (!group.getUserId().equals(userId)) return Result.BadRequest("无权限删除");

        qqGroupMapper.deleteById(id);
        return Result.success(null);
    }
}
