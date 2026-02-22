package com.starfinder.mapper;

import com.starfinder.entity.CoachingPost;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CoachingPostMapper {

    @Insert("INSERT INTO coaching_posts (user_id, title, race, mmr, price_info, contact, description, post_type, author_tag, created_at) " +
            "VALUES (#{userId}, #{title}, #{race}, #{mmr}, #{priceInfo}, #{contact}, #{description}, #{postType}, #{authorTag}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CoachingPost post);

    @Select("SELECT * FROM coaching_posts ORDER BY created_at DESC")
    List<CoachingPost> findAll();

    @Select("SELECT * FROM coaching_posts WHERE post_type = #{postType} ORDER BY created_at DESC")
    List<CoachingPost> findByType(@Param("postType") String postType);

    @Delete("DELETE FROM coaching_posts WHERE id = #{id}")
    void deleteById(Long id);
}
