package com.starfinder.mapper;

import com.starfinder.entity.TextTutorial;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TextTutorialMapper {

    @Insert("INSERT INTO text_tutorials (user_id, title, category, content, author_tag, created_at) " +
            "VALUES (#{userId}, #{title}, #{category}, #{content}, #{authorTag}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TextTutorial tutorial);

    @Select("SELECT * FROM text_tutorials ORDER BY created_at DESC")
    List<TextTutorial> findAll();

    @Select("SELECT * FROM text_tutorials WHERE category = #{category} ORDER BY created_at DESC")
    List<TextTutorial> findByCategory(@Param("category") String category);

    @Select("SELECT DISTINCT category FROM text_tutorials WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> findDistinctCategories();

    @Select("SELECT * FROM text_tutorials WHERE id = #{id}")
    TextTutorial findById(@Param("id") Long id);

    @Update("UPDATE text_tutorials SET title=#{title}, category=#{category}, content=#{content}, author_tag=#{authorTag} WHERE id=#{id}")
    void update(TextTutorial tutorial);

    @Delete("DELETE FROM text_tutorials WHERE id = #{id}")
    void deleteById(Long id);
}
