package com.wen.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wen.pojo.entity.MqMessage;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MqMessageMapper extends BaseMapper<MqMessage> {

}
