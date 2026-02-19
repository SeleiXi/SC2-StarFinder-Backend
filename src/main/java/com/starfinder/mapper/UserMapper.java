package com.starfinder.mapper;

import com.starfinder.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (name, battle_tag, character_id, race, mmr, phone_number, password, qq, stream_url, signature, region) "
            +
            "VALUES (#{name}, #{battleTag}, #{characterId}, #{race}, #{mmr}, #{phoneNumber}, #{password}, #{qq}, #{streamUrl}, #{signature}, #{region})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE phone_number = #{phoneNumber}")
    User findByPhoneNumber(String phoneNumber);

    @Select("SELECT * FROM users WHERE battle_tag = #{battleTag}")
    User findByBattleTag(String battleTag);

    @Select("SELECT * FROM users WHERE mmr BETWEEN #{minMmr} AND #{maxMmr}")
    List<User> findByMmrRange(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr);

    @Select("SELECT * FROM users WHERE mmr BETWEEN #{minMmr} AND #{maxMmr} AND race = #{race}")
    List<User> findByMmrRangeAndRace(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr,
            @Param("race") String race);

    @Update("UPDATE users SET name=#{name}, battle_tag=#{battleTag}, character_id=#{characterId}, race=#{race}, " +
            "mmr=#{mmr}, qq=#{qq}, stream_url=#{streamUrl}, signature=#{signature}, region=#{region} WHERE id=#{id}")
    void update(User user);

    @Select("SELECT * FROM users")
    List<User> findAll();
}