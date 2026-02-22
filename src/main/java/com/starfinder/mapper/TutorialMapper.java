package com.starfinder.mapper;

import com.starfinder.entity.Tutorial;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TutorialMapper {

    @Insert("INSERT INTO tutorials (title, url, category, description, author) " +
            "VALUES (#{title}, #{url}, #{category}, #{description}, #{author})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Tutorial tutorial);

    @Select("SELECT * FROM tutorials ORDER BY id DESC")
    List<Tutorial> findAll();

    @Select("SELECT * FROM tutorials WHERE category = #{category} ORDER BY id DESC")
    List<Tutorial> findByCategory(String category);

    @Select("SELECT * FROM tutorials WHERE id = #{id}")
    Tutorial findById(Long id);

    @Delete("DELETE FROM tutorials WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT DISTINCT category FROM tutorials WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> findDistinctCategories();
}
