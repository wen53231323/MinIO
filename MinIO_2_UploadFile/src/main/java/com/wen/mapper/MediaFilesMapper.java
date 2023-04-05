package com.wen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wen.pojo.entity.MediaFiles;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 媒资信息(MediaFiles)表数据库访问层
 *
 * @author wen
 * @since 2023-03-10 23:40:55
 */
@Mapper
@Repository
public interface MediaFilesMapper extends BaseMapper<MediaFiles> {

    @Select("SELECT * FROM media_files WHERE file_md5 = #{fileMd5}")
    MediaFiles selectByMd5(@Param("fileMd5") String fileMd5);
}

