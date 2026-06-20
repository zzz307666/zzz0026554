package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.SysSequence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 自增序列Mapper
 */
@Mapper
public interface SysSequenceMapper extends BaseMapper<SysSequence> {
    /**
     * 调用数据库函数获取按日期生成的序列编号（工号/学号）
     * @param seqCode 序列编码：TEACHER/STUDENT
     * @return 拼接后的编号（如T20260221001、S20260221001）
     */
    @Select("SELECT get_next_seq_by_date(#{seqCode})")
    String getNextSeqByDate(@Param("seqCode") String seqCode);
}