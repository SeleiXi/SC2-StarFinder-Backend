package com.starfinder.mapper;

import com.starfinder.entity.ReplayFile;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ReplayFileMapper {

    @Insert("INSERT INTO replay_files (user_id, title, file_name, file_path, description, category, author_tag, file_size, created_at) " +
            "VALUES (#{userId}, #{title}, #{fileName}, #{filePath}, #{description}, #{category}, #{authorTag}, #{fileSize}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ReplayFile replay);

    @Select("SELECT * FROM replay_files ORDER BY created_at DESC")
    List<ReplayFile> findAll();

    @Select("SELECT * FROM replay_files WHERE category = #{category} ORDER BY created_at DESC")
    List<ReplayFile> findByCategory(@Param("category") String category);

    @Select("SELECT * FROM replay_files WHERE id = #{id}")
    ReplayFile findById(@Param("id") Long id);

    @Update("UPDATE replay_files SET title=#{title}, description=#{description}, category=#{category} WHERE id=#{id}")
    void update(ReplayFile replay);

    @Delete("DELETE FROM replay_files WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT SUM(file_size) FROM replay_files WHERE user_id = #{userId}")
    Long getTotalFileSizeByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM replay_files WHERE user_id = #{userId}")
    int getFileCountByUser(@Param("userId") Long userId);
}
