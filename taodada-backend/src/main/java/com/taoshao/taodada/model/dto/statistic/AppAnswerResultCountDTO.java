package com.taoshao.taodada.model.dto.statistic;

import lombok.Data;

/**
 * App 答案结果统计
 *
 * @Author taoshao
 * @Date 2024/7/28
 */
@Data
public class AppAnswerResultCountDTO {

    private String resultName;

    private Long resultCount;
}
