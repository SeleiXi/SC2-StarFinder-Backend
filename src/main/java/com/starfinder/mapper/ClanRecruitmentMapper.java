package com.starfinder.mapper;

import com.starfinder.entity.ClanRecruitment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClanRecruitmentMapper {

    @Insert("INSERT INTO clan_recruitments (user_id, clan_name, clan_tag, region, min_mmr, max_mmr, description, contact, author_tag, created_at) " +
            "VALUES (#{userId}, #{clanName}, #{clanTag}, #{region}, #{minMmr}, #{maxMmr}, #{description}, #{contact}, #{authorTag}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ClanRecruitment recruitment);

    @Select("SELECT * FROM clan_recruitments ORDER BY created_at DESC")
    List<ClanRecruitment> findAll();

    @Delete("DELETE FROM clan_recruitments WHERE id = #{id}")
    void deleteById(Long id);
}
