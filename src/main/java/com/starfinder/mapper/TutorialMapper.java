package com.starfinder.mapper;

import com.starfinder.entity.Tutorial;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TutorialMapper {

    @Insert("INSERT INTO tutorials (title, url, category, description, author, status) " +
            "VALUES (#{title}, #{url}, #{category}, #{description}, #{author}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Tutorial tutorial);

    @Select("SELECT * FROM tutorials ORDER BY id DESC")
    List<Tutorial> findAll();

    @Select("SELECT * FROM tutorials WHERE category = #{category} ORDER BY id DESC")
    List<Tutorial> findByCategory(String category);

    @Select("SELECT * FROM tutorials WHERE id = #{id}")
    Tutorial findById(Long id);

    @Update("UPDATE tutorials SET title=#{title}, url=#{url}, category=#{category}, description=#{description}, author=#{author}, status=#{status} WHERE id=#{id}")
    void update(Tutorial tutorial);

    @Delete("DELETE FROM tutorials WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT DISTINCT category FROM tutorials WHERE category IS NOT NULL AND category != '' ORDER BY category")
    List<String> findDistinctCategories();

    @Select("SELECT * FROM tutorials WHERE status = 'approved' ORDER BY id DESC")
    List<Tutorial> findAllApproved();

    @Select("SELECT * FROM tutorials WHERE status = 'approved' AND category = #{category} ORDER BY id DESC")
    List<Tutorial> findByCategoryApproved(String category);

    @Select("SELECT COUNT(*) FROM tutorials WHERE status = 'pending'")
    int countPending();

    @Update("UPDATE tutorials SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}
