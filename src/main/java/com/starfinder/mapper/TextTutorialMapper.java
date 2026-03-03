package com.starfinder.mapper;

import com.starfinder.entity.TextTutorial;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TextTutorialMapper {

    @Insert("INSERT INTO text_tutorials (user_id, title, category, content, author_tag, status, created_at) " +
            "VALUES (#{userId}, #{title}, #{category}, #{content}, #{authorTag}, #{status}, NOW())")
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

    @Update("UPDATE text_tutorials SET title=#{title}, category=#{category}, content=#{content}, author_tag=#{authorTag}, status=#{status} WHERE id=#{id}")
    void update(TextTutorial tutorial);

    @Delete("DELETE FROM text_tutorials WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM text_tutorials WHERE status = 'approved' ORDER BY created_at DESC")
    List<TextTutorial> findAllApproved();

    @Select("SELECT * FROM text_tutorials WHERE status = 'approved' AND category = #{category} ORDER BY created_at DESC")
    List<TextTutorial> findByCategoryApproved(@Param("category") String category);

    @Select("SELECT COUNT(*) FROM text_tutorials WHERE status = 'pending'")
    int countPending();

    @Update("UPDATE text_tutorials SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
