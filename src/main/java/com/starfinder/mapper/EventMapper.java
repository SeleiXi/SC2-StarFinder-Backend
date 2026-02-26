package com.starfinder.mapper;

import com.starfinder.entity.Event;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EventMapper {

    @Insert("INSERT INTO events (title, description, rules, rewards, contact_link, group_link, submitted_by, status, region, start_time) "
            +
            "VALUES (#{title}, #{description}, #{rules}, #{rewards}, #{contactLink}, #{groupLink}, #{submittedBy}, #{status}, #{region}, #{startTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Event event);

    @Select("SELECT * FROM events WHERE status = 'approved' ORDER BY id DESC")
    List<Event> findAllApproved();

    @Select("SELECT * FROM events ORDER BY id DESC")
    List<Event> findAll();

    @Select("SELECT * FROM events WHERE id = #{id}")
    Event findById(Long id);

    @Update("UPDATE events SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE events SET title=#{title}, description=#{description}, rules=#{rules}, rewards=#{rewards}, contact_link=#{contactLink}, group_link=#{groupLink}, status=#{status}, region=#{region}, start_time=#{startTime} WHERE id=#{id}")
    void update(Event event);

    @Delete("DELETE FROM events WHERE id = #{id}")
    void deleteById(Long id);
}
