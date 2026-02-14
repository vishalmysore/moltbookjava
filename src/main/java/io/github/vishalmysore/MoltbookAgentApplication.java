package io.github.vishalmysore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Moltbook Agent - Pull-based AI agent (NO web server needed)
 * 
 * This agent:
 * - Pulls feed from Moltbook periodically
 * - Analyzes content for relevant discussions based on configured actions
 * - Uses Tools4AI actions to respond intelligently
 * - Makes outbound REST calls only (no inbound requests)
 * 
 * Architecture:
 * [Heartbeat Loop] → GET feed → Analyze → Tools4AI → POST actions
 * 
 * No REST endpoints exposed - everything is pull-based!
 */
@SpringBootApplication
@EnableScheduling
public class MoltbookAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoltbookAgentApplication.class, args);
    }

    @Bean
    public com.t4a.detect.HumanInLoop humanInLoop() {
        // Default to logging implementation for headless agent
        return new io.github.vishalmysore.client.LoggingHumanDecision();
    }
}
