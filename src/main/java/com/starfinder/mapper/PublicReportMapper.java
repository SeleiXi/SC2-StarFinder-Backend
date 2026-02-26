package com.starfinder.mapper;

import com.starfinder.entity.PublicReport;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PublicReportMapper {

    @Insert("INSERT INTO public_reports (game_id, mmr_min, mmr_max, description, reported_by_id, created_at) " +
            "VALUES (#{gameId}, #{mmrMin}, #{mmrMax}, #{description}, #{reportedById}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PublicReport report);

    @Select("SELECT * FROM public_reports ORDER BY created_at DESC")
    List<PublicReport> findAll();

    @Select("SELECT * FROM public_reports WHERE LOWER(game_id) LIKE CONCAT('%', LOWER(#{gameId}), '%') ORDER BY created_at DESC")
    List<PublicReport> searchByGameId(@Param("gameId") String gameId);

    @Select("SELECT * FROM public_reports WHERE id = #{id}")
    PublicReport findById(@Param("id") Long id);

    @Update("UPDATE public_reports SET game_id=#{gameId}, mmr_min=#{mmrMin}, mmr_max=#{mmrMax}, description=#{description} WHERE id=#{id}")
    void update(PublicReport report);

    @Delete("DELETE FROM public_reports WHERE id = #{id}")
    void deleteById(Long id);
}
