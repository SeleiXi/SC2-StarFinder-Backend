package com.starfinder.mapper;

import com.starfinder.entity.Cheater;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CheaterMapper {

    @Insert("INSERT INTO cheaters (battle_tag, cheat_type, description, reported_by, status, mmr, race) " +
            "VALUES (#{battleTag}, #{cheatType}, #{description}, #{reportedBy}, #{status}, #{mmr}, #{race})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Cheater cheater);

    @Select("SELECT * FROM cheaters WHERE status = 'approved' ORDER BY id DESC")
    List<Cheater> findAllApproved();

    @Select("SELECT * FROM cheaters ORDER BY id DESC")
    List<Cheater> findAll();

    @Select("SELECT * FROM cheaters WHERE battle_tag LIKE CONCAT('%', #{battleTag}, '%') AND status = 'approved'")
    List<Cheater> searchByBattleTag(String battleTag);

    @Select("SELECT * FROM cheaters WHERE id = #{id}")
    Cheater findById(Long id);

    @Update("UPDATE cheaters SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM cheaters WHERE id = #{id}")
    void deleteById(Long id);
}
