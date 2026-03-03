package com.starfinder.mapper;

import com.starfinder.entity.Feedback;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Insert("INSERT INTO feedbacks (user_id, content, author_tag, status, created_at) " +
            "VALUES (#{userId}, #{content}, #{authorTag}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Feedback feedback);

    @Select("SELECT * FROM feedbacks ORDER BY created_at DESC")
    List<Feedback> findAll();

    @Select("SELECT * FROM feedbacks WHERE id = #{id}")
    Feedback findById(@Param("id") Long id);

    @Select("SELECT * FROM feedbacks WHERE status = #{status} ORDER BY created_at DESC")
    List<Feedback> findByStatus(@Param("status") String status);

    @Update("UPDATE feedbacks SET status=#{status}, admin_reply=#{adminReply} WHERE id=#{id}")
    void update(Feedback feedback);

    @Delete("DELETE FROM feedbacks WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM feedbacks WHERE status = 'pending'")
    int countPending();
}
