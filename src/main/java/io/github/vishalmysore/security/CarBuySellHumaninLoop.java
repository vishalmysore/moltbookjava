package io.github.vishalmysore.security;

import com.t4a.detect.FeedbackLoop;
import com.t4a.detect.HumanInLoop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class CarBuySellHumaninLoop implements HumanInLoop {

    @Override
    public FeedbackLoop allow(String promptText, String methodName, Map<String, Object> params) {
        log.info("HITL Check - Prompt: {}, Method: {}, Params: {}", promptText, methodName, params);
        return () -> true;
    }

    @Override
    public FeedbackLoop allow(String promptText, String methodName, String params) {
        log.info("HITL Check - Prompt: {}, Method: {}, Params: {}", promptText, methodName, params);
        return () -> true;
    }
}
