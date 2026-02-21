package com.starfinder.mapper;

import com.starfinder.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (name, battle_tag, battle_tag_cn, battle_tag_us, battle_tag_eu, battle_tag_kr, character_id, race, commander, mmr, mmr_2v2, mmr_3v3, mmr_4v4, email, password, qq, stream_url, signature, region, role) "
            +
            "VALUES (#{name}, #{battleTag}, #{battleTagCN}, #{battleTagUS}, #{battleTagEU}, #{battleTagKR}, #{characterId}, #{race}, #{commander}, #{mmr}, #{mmr2v2}, #{mmr3v3}, #{mmr4v4}, #{email}, #{password}, #{qq}, #{streamUrl}, #{signature}, #{region}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM users WHERE name = #{name}")
    User findByName(String name);

    @Select("SELECT * FROM users WHERE battle_tag = #{battleTag}")
    User findByBattleTag(String battleTag);

    @Select("SELECT * FROM users WHERE commander = #{commander}")
    List<User> findByCommander(@Param("commander") String commander);

    @Select("SELECT * FROM users WHERE commander IS NOT NULL AND commander != ''")
    List<User> findAllWithCommander();

    @Select("SELECT * FROM users WHERE mmr BETWEEN #{minMmr} AND #{maxMmr}")
    List<User> findByMmrRange(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr);

    @Select("SELECT * FROM users WHERE mmr_2v2 BETWEEN #{minMmr} AND #{maxMmr}")
    List<User> findByMmr2v2Range(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr);

    @Select("SELECT * FROM users WHERE mmr_3v3 BETWEEN #{minMmr} AND #{maxMmr}")
    List<User> findByMmr3v3Range(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr);

    @Select("SELECT * FROM users WHERE mmr_4v4 BETWEEN #{minMmr} AND #{maxMmr}")
    List<User> findByMmr4v4Range(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr);

    @Select("SELECT * FROM users WHERE mmr BETWEEN #{minMmr} AND #{maxMmr} AND race = #{race}")
    List<User> findByMmrRangeAndRace(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr,
            @Param("race") String race);

    @Select("SELECT * FROM users WHERE mmr_2v2 BETWEEN #{minMmr} AND #{maxMmr} AND race = #{race}")
    List<User> findByMmr2v2RangeAndRace(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr,
            @Param("race") String race);

    @Select("SELECT * FROM users WHERE mmr_3v3 BETWEEN #{minMmr} AND #{maxMmr} AND race = #{race}")
    List<User> findByMmr3v3RangeAndRace(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr,
            @Param("race") String race);

    @Select("SELECT * FROM users WHERE mmr_4v4 BETWEEN #{minMmr} AND #{maxMmr} AND race = #{race}")
    List<User> findByMmr4v4RangeAndRace(@Param("minMmr") int minMmr, @Param("maxMmr") int maxMmr,
            @Param("race") String race);

    @Update("UPDATE users SET battle_tag=#{battleTag}, battle_tag_cn=#{battleTagCN}, battle_tag_us=#{battleTagUS}, battle_tag_eu=#{battleTagEU}, battle_tag_kr=#{battleTagKR}, character_id=#{characterId}, race=#{race}, commander=#{commander}, " +
            "mmr=#{mmr}, mmr_2v2=#{mmr2v2}, mmr_3v3=#{mmr3v3}, mmr_4v4=#{mmr4v4}, qq=#{qq}, stream_url=#{streamUrl}, signature=#{signature}, region=#{region}, role=#{role} WHERE id=#{id}")
    void update(User user);

    @Select("SELECT * FROM users WHERE race = #{race}")
    List<User> findByRaceOnly(@Param("race") String race);

    @Update("UPDATE users SET password=#{password} WHERE email=#{email}")
    void updatePassword(@Param("email") String email, @Param("password") String password);

    @Select("SELECT * FROM users")
    List<User> findAll();

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(Long id);
}