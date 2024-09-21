package com.taoshao.taodada.scoring;

import com.taoshao.taodada.common.ErrorCode;
import com.taoshao.taodada.exception.BusinessException;
import com.taoshao.taodada.model.entity.App;
import com.taoshao.taodada.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author taoshao
 */
@Service
public class ScoringStrategyExecutor {

    /**
     * 策略列表
     */
    @Resource
    private List<ScoringStrategy> scoringStrategyList;


    /**
     * 评分
     *
     * @param choiceList
     * @param app
     * @return
     * @throws Exception
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        Integer appType = app.getAppType();
        Integer appScoringStrategy = app.getScoringStrategy();
        if (appType == null || appScoringStrategy == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
        }
        // 根据注解获取策略
        for (ScoringStrategy strategy : scoringStrategyList) {
            // 检查当前遍历到的strategy对象的类是否包含ScoringStrategyConfig注解
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                // 如果存在ScoringStrategyConfig注解，则获取这个注解的实例。
                ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (scoringStrategyConfig.appType() == appType && scoringStrategyConfig.scoringStrategy() == appScoringStrategy) {
                    return strategy.doScore(choiceList, app);
                }
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
    }
}