package com.taoshao.taodada.model.dto.statistic;

import lombok.Data;

/**
 * App 用户提交答案数统计
 *
 * @Author taoshao
 * @Date 2024/7/28
 */
@Data
public class AppAnswerCountDTO {

    private Long appId;

    /**
     * 用户提交答案数
     */
    private Long answerCount;
}
