package cn.scutbot.teamcitylark;


import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class LarkNotifierEnabled implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        //return TeamCityProperties.getBooleanOrTrue(LarkNotifierProperties.EntryEnabled);
        return true;
    }
}
