package com.starfinder.mapper;

import com.starfinder.entity.QqGroup;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QqGroupMapper {

    @Insert("INSERT INTO qq_groups (group_name, group_number, description, contact_info, user_id, author_tag, status, created_at) " +
            "VALUES (#{groupName}, #{groupNumber}, #{description}, #{contactInfo}, #{userId}, #{authorTag}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(QqGroup qqGroup);

    @Select("SELECT * FROM qq_groups WHERE status = 'approved' ORDER BY created_at DESC")
    List<QqGroup> findApproved();

    @Select("SELECT * FROM qq_groups ORDER BY created_at DESC")
    List<QqGroup> findAll();

    @Select("SELECT * FROM qq_groups WHERE id = #{id}")
    QqGroup findById(@Param("id") Long id);

    @Update("UPDATE qq_groups SET group_name=#{groupName}, group_number=#{groupNumber}, description=#{description}, " +
            "contact_info=#{contactInfo}, status=#{status} WHERE id=#{id}")
    void update(QqGroup qqGroup);

    @Delete("DELETE FROM qq_groups WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM qq_groups WHERE status = 'pending'")
    int countPending();
}
