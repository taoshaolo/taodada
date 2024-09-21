package com.taoshao.taodada.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.taoshao.taodada.model.dto.question.QuestionContentDTO;
import com.taoshao.taodada.model.entity.App;
import com.taoshao.taodada.model.entity.Question;
import com.taoshao.taodada.model.entity.ScoringResult;
import com.taoshao.taodada.model.entity.UserAnswer;
import com.taoshao.taodada.model.vo.QuestionVO;
import com.taoshao.taodada.service.AppService;
import com.taoshao.taodada.service.QuestionService;
import com.taoshao.taodada.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 自定义测评类应用评分策略
 *
 * @Author taoshao
 * @Date 2024/9/7
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 0)
public class CustomTestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        // 1,根据 id 查询到题目和题目结果信息
        Long appId = app.getId();
        LambdaQueryWrapper<Question> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Question::getAppId, appId);
        Question question = questionService.getOne(lambdaQueryWrapper);

        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId)
        );

        // 2,统计用户每个选择对应的属性个数，如 I = 10 个，E = 5 个
        HashMap<String, Integer> optionCount = new HashMap<>();

        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        int choiceIndex = 0;
        // 遍历题目列表
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
                    // 获取选项的 result 属性
                    String result = option.getResult();
                    // 如果 result 属性不在 optionCount 中，初始化为 0
                    if (!optionCount.containsKey(result)) {
                        optionCount.put(result, 0);
                    }
                    // 在 optionCount 中增加计数
                    optionCount.put(result, optionCount.get(result) + 1);
                    break;// 找到匹配的选项后，跳出循环
                }
            }
            choiceIndex ++;
        }

        // 3,遍历每种评分结果，计算哪个结果的得分更高
        // 初始化 最高分数 和 最高分数对应的评分结果
        int maxScore = 0;
        ScoringResult maxScoringResult = scoringResultList.get(0);

        // 遍历评分结果列表
        for (ScoringResult scoringResult : scoringResultList) {
            List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
            // 计算当前评分结果的分数，[I, E] => [10, 5] => 15
            int score = resultProp.stream()
                    .mapToInt(prop -> optionCount.getOrDefault(prop, 0))
                    .sum();

            // 如果分数高于当前最高分数，更新最高分数和和最高分数对应的评分结果
            if (score > maxScore) {
                maxScore = score;
                maxScoringResult = scoringResult;
            }
        }

        // 4,构造返回值，填充答案对象的属性
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());

        return userAnswer;
    }

}
