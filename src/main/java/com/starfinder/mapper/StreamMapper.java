package com.starfinder.mapper;

import com.starfinder.entity.Stream;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StreamMapper {
    @Insert("INSERT INTO streams (name, battle_tag, battle_tag_cn, battle_tag_us, battle_tag_eu, battle_tag_kr, stream_url, description, mmr, race, platform, user_id) " +
            "VALUES (#{name}, #{battleTag}, #{battleTagCN}, #{battleTagUS}, #{battleTagEU}, #{battleTagKR}, #{streamUrl}, #{description}, #{mmr}, #{race}, #{platform}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Stream stream);

    @Select("SELECT * FROM streams ORDER BY created_at DESC")
    List<Stream> findAll();

    @Delete("DELETE FROM streams WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM streams WHERE battle_tag = #{battleTag}")
    Stream findByBattleTag(String battleTag);
}
