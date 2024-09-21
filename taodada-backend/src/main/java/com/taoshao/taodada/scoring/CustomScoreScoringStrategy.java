package com.taoshao.taodada.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.taoshao.taodada.model.dto.question.QuestionContentDTO;
import com.taoshao.taodada.model.entity.App;
import com.taoshao.taodada.model.entity.Question;
import com.taoshao.taodada.model.entity.ScoringResult;
import com.taoshao.taodada.model.entity.UserAnswer;
import com.taoshao.taodada.model.vo.QuestionVO;
import com.taoshao.taodada.service.QuestionService;
import com.taoshao.taodada.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 自定义得分类应用评分策略
 *
 * @Author taoshao
 * @Date 2024/9/7
 */
@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy {
    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        // 1.根据 id 查询到题目和题目结果信息（按分数降序排序）
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
                        .orderByDesc(ScoringResult::getResultScoreRange)
        );

        // 2.统计用户的总得分
        int totalScore = 0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 先遍历用户的答案
        int choiceIndex = 0;
        for (QuestionContentDTO questionContentDTO : questionContent) {
            // 检查是否有更多的答案
            if (choiceIndex >= choices.size()) {
                break;
            }

            String answer = choices.get(choiceIndex);
            // 遍历题目中的选项
            for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                // 如果答案和选项的 key 匹配
                if (option.getKey().equals(answer)) {
                    // 获取选项的 score 属性
                    int score = Optional.of(option.getScore()).orElse(0);
                    totalScore += score;
                    break; // 找到匹配的选项后，跳出循环
                }
            }
            choiceIndex++; // 移动到下一个答案
        }

        ScoringResult maxScoringResult = scoringResultList.get(0);

        for (ScoringResult scoringResult : scoringResultList) {
            if (totalScore >= scoringResult.getResultScoreRange()){
                maxScoringResult = scoringResult;
                break;
            }
        }
        // 4.构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}
