package com.taoshao.taodada.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoshao.taodada.model.dto.statistic.AppAnswerCountDTO;
import com.taoshao.taodada.model.dto.statistic.AppAnswerResultCountDTO;
import com.taoshao.taodada.model.entity.UserAnswer;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Entity com.taoshao.taodada.model.entity.UserAnswer
 */
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    @Select("select appId, count(userId) as answerCount from user_answer\n" +
            "group by appId order by answerCount desc limit 10; ")
    List<AppAnswerCountDTO> doAppAnswerCount();

    @Select("select resultName, count(resultName) as resultCount from user_answer\n" +
            "where appId = #{appId}\n" +
            "group by resultName order by resultCount desc;")
    List<AppAnswerResultCountDTO> doAppAnswerResultCount(Long appId);
}




