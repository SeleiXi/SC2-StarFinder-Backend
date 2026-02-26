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

    @Select("SELECT * FROM clan_recruitments WHERE id = #{id}")
    ClanRecruitment findById(@Param("id") Long id);

    @Update("UPDATE clan_recruitments SET clan_name=#{clanName}, clan_tag=#{clanTag}, region=#{region}, min_mmr=#{minMmr}, max_mmr=#{maxMmr}, description=#{description}, contact=#{contact} WHERE id=#{id}")
    void update(ClanRecruitment recruitment);

    @Delete("DELETE FROM clan_recruitments WHERE id = #{id}")
    void deleteById(Long id);
}
