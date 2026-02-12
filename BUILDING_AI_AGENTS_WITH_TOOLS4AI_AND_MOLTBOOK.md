# AI Agent for Moltbook in Java: From Zero to Hero

## Introduction

Building AI agents that can interact with social platforms, execute tasks, and learn from their environment has traditionally required complex orchestration, API management, and careful state handling. This article demonstrates how **Tools4AI** and **Moltbook** work together to simplify AI agent development to just a few annotations.

By the end of this article, you'll understand how to create a fully functional AI agent that:
- Automatically discovers and exposes its capabilities
- Interacts with the Moltbook social platform
- Uses AI to generate intelligent responses
- Handles verification challenges autonomously
- Tracks its own activities

## Architecture Overview

### The Three-Layer Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Moltbook Platform                        │
│  (Social network for AI agents - moltbook.com)             │
└─────────────────────────────────────────────────────────────┘
                           ↑ REST API
                           │
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Application Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Heartbeat   │  │   Activity   │  │  Dashboard   │     │
│  │   Service    │  │   Tracking   │  │  Controller  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                           ↑
                           │ Uses
                           │
┌─────────────────────────────────────────────────────────────┐
│                    Tools4AI Framework                        │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Action Discovery (Scans @Predict annotations)     │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  AI Processor (Integrates OpenAI/Gemini/Claude)    │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  Function Calling (Converts methods → JSON-RPC)    │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                           ↑
                           │
┌─────────────────────────────────────────────────────────────┐
│              Your Agent Actions (@Predict)                   │
│  - getCarInfo()     - compareCars()                         │
│  - getCarPricing()  - getBookingStatus()                    │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

1. **Tools4AI Framework**: Discovers capabilities, handles AI interactions
2. **Spring Boot Layer**: Orchestrates heartbeat, tracks activities, serves dashboard
3. **Moltbook Platform**: Social environment where agents interact
4. **Your Actions**: Domain-specific functionality with simple annotations

## Step 1: Creating Agent Actions with @Predict

The magic starts with Tools4AI's `@Predict` annotation. You simply annotate methods, and they automatically become AI-accessible functions.

### Example: Car Service Agent

```java
package io.github.vishalmysore.agent.actions;

import com.t4a.annotations.Predict;
import org.springframework.stereotype.Component;

/**
 * Car service actions - automatically discovered by Tools4AI
 * Each @Predict method becomes an AI function
 */
@Component
public class CarServiceActions {

    /**
     * Get detailed information about a car model
     */
    @Predict(
            name = "get_car_info",
            description = "Get detailed information about a specific car model including specs, features, and availability"
    )
    public String getCarInfo(String carModel) {
        // Your business logic here
        return String.format(
                "Car: %s\nEngine: 2.0L Turbo\nHorsepower: 250hp\nPrice: $35,000\nAvailability: In stock",
                carModel
        );
    }

    /**
     * Get pricing for different car types
     */
    @Predict(
            name = "get_car_pricing",
            description = "Get current pricing information for a car type (sedan, suv, sports, electric)"
    )
    public String getCarPricing(String carType) {
        return switch (carType.toLowerCase()) {
            case "sedan" -> "Sedans: $25,000 - $45,000";
            case "suv" -> "SUVs: $35,000 - $75,000";
            case "sports" -> "Sports Cars: $50,000 - $150,000";
            case "electric" -> "Electric: $40,000 - $100,000";
            default -> "Unknown car type. Available: sedan, suv, sports, electric";
        };
    }

    /**
     * Compare two car models
     */
    @Predict(
            name = "compare_cars",
            description = "Compare specifications and features between two car models"
    )
    public String compareCars(String car1, String car2) {
        return String.format("""
                Comparison: %s vs %s
                
                %s:
                - Engine: 2.0L, 250hp
                - 0-60mph: 6.2s
                - Price: $35,000
                
                %s:
                - Engine: 3.0L, 350hp
                - 0-60mph: 4.8s
                - Price: $52,000
                
                Recommendation: %s offers better performance, %s is more economical.
                """, car1, car2, car1, car2, car2, car1);
    }

    /**
     * List available car types
     */
    @Predict(
            name = "list_car_types",
            description = "List all available car types in the inventory"
    )
    public String listCarTypes() {
        return """
                Available Car Types:
                1. Sedan - Comfortable daily drivers
                2. SUV - Spacious family vehicles
                3. Sports - High-performance vehicles
                4. Electric - Eco-friendly options
                5. Truck - Utility and cargo
                """;
    }

    /**
     * Check booking status
     */
    @Predict(
            name = "get_booking_status",
            description = "Check the status of a car booking using the booking ID"
    )
    public String getBookingStatus(String bookingId) {
        return String.format(
                "Booking ID: %s\nStatus: Confirmed\nPickup Date: 2026-02-15\nReturn Date: 2026-02-20\nVehicle: Tesla Model 3",
                bookingId
        );
    }
}
```

### What Just Happened?

Tools4AI automatically:
1. **Scans** your classpath for `@Predict` annotations
2. **Generates** JSON-RPC function schemas
3. **Registers** methods with the AI model
4. **Handles** parameter marshalling and result formatting

### Configuration

```properties
# tools4ai.properties
packagesToScan=io.github.moltbook.agent.actions
openAiKey=your-openai-api-key
# Or use Gemini, Claude, Anthropic, etc.
```

## Step 2: Integrating with Moltbook

Moltbook is a social platform for AI agents. Your agent can post, comment, search, and interact with other agents.

### The Moltbook Client

```java
package io.github.vishalmysore.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MoltbookClient {

    private static final String BASE_URL = "https://www.moltbook.com/api/v1";
    private final RestTemplate restTemplate;
    private String apiKey;

    public MoltbookClient(@Value("${moltbook.api.key:}") String configuredApiKey) {
        this.restTemplate = new RestTemplate();

        // Try multiple sources for API key
        String systemPropKey = System.getProperty("MOLTBOOK_API_KEY");
        String envKey = System.getenv("MOLTBOOK_API_KEY");

        if (systemPropKey != null && !systemPropKey.isEmpty()) {
            this.apiKey = systemPropKey;
        } else if (envKey != null && !envKey.isEmpty()) {
            this.apiKey = envKey;
        } else {
            this.apiKey = configuredApiKey;
        }
    }

    /**
     * Create a post on Moltbook
     */
    public String createPost(String submolt, String title, String content) {
        String requestBody = String.format(
                "{\"submolt\":\"%s\",\"title\":\"%s\",\"content\":\"%s\"}",
                submolt, escapeJson(title), escapeJson(content)
        );
        return post("/posts", requestBody);
    }

    /**
     * Create a comment on a post
     */
    public String createComment(String postId, String content) {
        String requestBody = String.format(
                "{\"content\":\"%s\"}",
                escapeJson(content)
        );
        return post("/posts/" + postId + "/comments", requestBody);
    }

    /**
     * Get agent feed
     */
    public String getFeed(int limit) {
        return get("/feed?sort=new&limit=" + limit);
    }

    /**
     * Get global posts (fallback when no subscriptions)
     */
    public String getPosts(String sort, int limit) {
        return get("/posts?sort=" + sort + "&limit=" + limit);
    }

    /**
     * Semantic search for relevant discussions
     */
    public String semanticSearch(String query, String contentType, int limit) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);
        return get("/search/semantic?q=" + encodedQuery +
                "&content_type=" + contentType + "&limit=" + limit);
    }

    /**
     * Upvote a post
     */
    public void upvote(String postId) {
        post("/posts/" + postId + "/upvote", null);
    }

    /**
     * Get agent profile (includes pending posts)
     */
    public String getProfile() {
        return get("/agents/me");
    }

    /**
     * Submit verification for a post
     */
    public String verifyPost(String verificationCode, String answer) {
        String requestBody = String.format(
                "{\"code\":\"%s\",\"answer\":\"%s\"}",
                verificationCode, answer
        );
        return post("/verify", requestBody);
    }

    // Helper methods
    private String get(String path) {
        HttpHeaders headers = authHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + path, HttpMethod.GET, entity, String.class
        );
        return response.getBody();
    }

    private String post(String path, Object body) {
        HttpHeaders headers = authHeaders();
        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + path, entity, String.class
        );
        return response.getBody();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.setBearerAuth(apiKey);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
```

## Step 3: The Heartbeat - Pull-Based Architecture

Unlike traditional webhook-based agents, this uses a **pull-based** architecture:

```java
package io.github.vishalmysore.client;

import com.t4a.predict.PredictionLoader;
import com.t4a.predict.Tools4AI;
import com.t4a.processor.AIProcessor;
import io.github.vishalmysore.analyzer.FeedAnalyzer;
import io.github.vishalmysore.model.FeedItem;
import io.github.vishalmysore.service.ActivityTrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Moltbook heartbeat - Pull-based architecture
 *
 * NO inbound requests - everything is outbound!
 * Agent pulls feed, analyzes content, and responds intelligently
 */
@Component
@Slf4j
public class MoltbookHeartbeat {

    private final MoltbookClient moltbookClient;
    private final FeedAnalyzer feedAnalyzer;
    private final ActivityTrackingService activityTrackingService;
    private final AIProcessor processor;
    private final String agentPrompt;

    private Instant lastCheck;
    private Instant lastPostTime = null;
    private int postCooldownMinutes = 120; // 2 hours for new agents

    public MoltbookHeartbeat(
            MoltbookClient moltbookClient,
            FeedAnalyzer feedAnalyzer,
            ActivityTrackingService activityTrackingService) {

        this.moltbookClient = moltbookClient;
        this.feedAnalyzer = feedAnalyzer;
        this.activityTrackingService = activityTrackingService;
        this.processor = PredictionLoader.getInstance().createOrGetAIProcessor();

        // Get agent capabilities from Tools4AI
        String mySkills = Tools4AI.getActionListAsJSONRPC();

        // Create dynamic prompt based on discovered capabilities
        this.agentPrompt = String.format("""
                These are my skills: %s
                
                Create a fun and engaging Moltbook post (max 500 chars) that:
                1. Makes a witty joke about topics derived from my skills
                2. Introduces my capabilities naturally
                3. Asks other agents to share questions about my skills
                4. Be friendly, casual, and use 1-2 emojis
                5. End with a question to encourage engagement
                
                Make it sound natural, not like an advertisement. Be creative!
                """, mySkills);
    }

    /**
     * Heartbeat runs every 5 minutes - main pull loop
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void runHeartbeat() {
        log.info("🦞 Moltbook heartbeat starting...");

        try {
            // 1️⃣ Check if agent is claimed
            String statusResponse = moltbookClient.getAgentStatus();
            if (!statusResponse.contains("\"status\":\"claimed\"")) {
                log.warn("⏳ Agent not claimed yet - waiting");
                return;
            }

            // 2️⃣ Pull feed (with fallback to global posts)
            log.info("📥 Pulling feed...");
            String feedJson;
            try {
                feedJson = moltbookClient.getFeed(50);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("401") ||
                        e.getMessage().contains("Authentication required")) {
                    log.warn("Feed requires subscriptions - using global posts");
                    feedJson = moltbookClient.getPosts("new", 50);
                } else {
                    throw e;
                }
            }

            List<FeedItem> feed = feedAnalyzer.parseFeed(feedJson);
            log.info("Retrieved {} items from feed", feed.size());

            // 3️⃣ Analyze for relevant content
            List<FeedItem> relevantItems = feedAnalyzer.findRelevantItems(feed);
            log.info("🔍 Found {} relevant items", relevantItems.size());

            // 4️⃣ Process relevant items with AI
            for (FeedItem item : relevantItems) {
                processRelevantItem(item);
            }

            // 5️⃣ Semantic search for discussions
            searchForRelevantDiscussions();

            // 6️⃣ Check for pending posts needing verification
            checkPendingPosts();

            // 7️⃣ Post about capabilities if no discussions found
            if (shouldPost()) {
                postAboutCapabilities();
            }

            lastCheck = Instant.now();
            log.info("✅ Heartbeat completed successfully");

        } catch (Exception e) {
            log.error("❌ Heartbeat failed", e);
            lastCheck = Instant.now(); // Avoid spam on errors
        }
    }

    /**
     * Process a relevant feed item using AI
     */
    private void processRelevantItem(FeedItem item) {
        try {
            String text = item.getFullText();
            String author = item.getAuthor().getName();

            // Build AI prompt with context
            String prompt = String.format(
                    "@%s asked: \"%s\"\n\nProvide helpful information using your available capabilities.",
                    author, text
            );

            // AI generates intelligent response
            String response = processor.query(prompt);

            log.info("💬 Commenting on post {}", item.getId());

            try {
                moltbookClient.createComment(item.getId(), response);
                moltbookClient.upvote(item.getId());

                // Track successful comment (green light on dashboard)
                activityTrackingService.trackComment(
                        item.getId(),
                        item.getTitle(),
                        response,
                        true  // SUCCESS
                );
            } catch (RuntimeException e) {
                // Track failed comment (red light on dashboard)
                activityTrackingService.trackComment(
                        item.getId(),
                        item.getTitle(),
                        response,
                        false  // FAILED
                );
            }

            Thread.sleep(2000); // Rate limit protection

        } catch (Exception e) {
            log.error("Failed to process item: {}", item.getId(), e);
        }
    }

    /**
     * Check for posts requiring verification
     */
    private void checkPendingPosts() {
        try {
            log.info("🔍 Checking for pending posts requiring verification...");

            String profileResponse = moltbookClient.getProfile();
            if (!profileResponse.contains("pending_posts")) {
                return;
            }

            JsonObject profile = new Gson().fromJson(profileResponse, JsonObject.class);
            JsonArray pendingPosts = profile.getAsJsonObject("agent")
                    .getAsJsonArray("pending_posts");

            if (pendingPosts.size() == 0) {
                return;
            }

            log.info("📝 Found {} pending post(s) requiring verification", pendingPosts.size());

            // Verify each pending post
            for (JsonElement postElement : pendingPosts) {
                JsonObject post = postElement.getAsJsonObject();
                JsonObject verification = post.getAsJsonObject("verification");

                String verificationCode = verification.get("code").getAsString();
                String challenge = verification.get("challenge").getAsString();

                log.info("🧩 Challenge: {}", challenge);

                // Use AI to solve obfuscated math challenge
                String solvePrompt = String.format("""
                        Solve this math problem. The text is intentionally obfuscated with random characters and case changes.
                        Extract the math problem and solve it.
                        Return ONLY the numeric answer with 2 decimal places (e.g., '525.00').
                        
                        Challenge: %s
                        """, challenge);

                String answer = processor.query(solvePrompt).trim()
                        .replaceAll("[^0-9.]", "");

                // Ensure 2 decimal places
                if (!answer.contains(".")) {
                    answer = answer + ".00";
                }

                log.info("💡 Computed answer: {}", answer);

                // Submit verification
                String verifyResponse = moltbookClient.verifyPost(verificationCode, answer);
                log.info("✅ Verified post: {}", verifyResponse);

                activityTrackingService.trackPost(
                        post.get("id").getAsString(),
                        "Verified pending post",
                        "Successfully verified",
                        true
                );

                Thread.sleep(2000); // Rate limit
            }

        } catch (Exception e) {
            log.error("❌ Failed to check pending posts", e);
        }
    }

    /**
     * Post about agent capabilities using AI
     */
    private void postAboutCapabilities() {
        try {
            if (!canPost()) {
                return;
            }

            log.info("📝 Posting about capabilities...");

            // AI generates engaging post content
            String postContent = processor.query(agentPrompt).trim();

            // Clean up formatting
            if (postContent.startsWith("\"") && postContent.endsWith("\"")) {
                postContent = postContent.substring(1, postContent.length() - 1);
            }

            log.info("📝 Content: {}", postContent.substring(0, Math.min(100, postContent.length())));

            try {
                String response = moltbookClient.createPost(
                        "general",
                        "🤖 Your AI Assistant is Here!",
                        postContent
                );

                lastPostTime = Instant.now();

                // Check if verification is required
                if (response.contains("verification_required")) {
                    log.info("🔐 Post requires verification - attempting to solve...");
                    handleVerification(response);
                }

                activityTrackingService.trackPost(
                        "new-post",
                        "🤖 Your AI Assistant is Here!",
                        postContent,
                        true  // SUCCESS
                );

            } catch (RuntimeException e) {
                activityTrackingService.trackPost(
                        "error",
                        "🤖 Your AI Assistant is Here!",
                        postContent,
                        false  // FAILED
                );
            }

            Thread.sleep(3000);

        } catch (Exception e) {
            log.error("Failed to post about capabilities", e);
        }
    }

    private boolean canPost() {
        if (lastPostTime == null) {
            return true;
        }
        Duration timeSincePost = Duration.between(lastPostTime, Instant.now());
        return timeSincePost.toMinutes() >= postCooldownMinutes;
    }

    private boolean shouldPost() {
        // Add your logic here
        return true;
    }

    private void searchForRelevantDiscussions() {
        // Implementation details...
    }

    private void handleVerification(String response) {
        // Implementation details...
    }
}
```

## Step 4: Activity Tracking Dashboard

Track what your agent is doing with a real-time dashboard:

```java
package io.github.vishalmysore.service;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ActivityTrackingService {

    private final List<Activity> activities = new CopyOnWriteArrayList<>();
    private static final int MAX_ACTIVITIES = 100;

    @Data
    public static class Activity {
        private final String type; // POST, COMMENT, OBSERVATION, ERROR
        private final LocalDateTime timestamp;
        private final String postId;
        private final String title;
        private final String content;
        private final String status; // SUCCESS, FAILED

        public Activity(String type, String postId, String title, String content, boolean success) {
            this.type = type;
            this.timestamp = LocalDateTime.now();
            this.postId = postId;
            this.title = title;
            this.content = content;
            this.status = success ? "SUCCESS" : "FAILED";
        }
    }

    public void trackPost(String postId, String title, String content, boolean success) {
        addActivity(new Activity("POST", postId, title, content, success));
    }

    public void trackComment(String postId, String title, String comment, boolean success) {
        addActivity(new Activity("COMMENT", postId, title, comment, success));
    }

    public void trackObservation(String postId, String title) {
        addActivity(new Activity("OBSERVATION", postId, title, "", true));
    }

    public void trackError(String message) {
        addActivity(new Activity("ERROR", "N/A", "Error", message, false));
    }

    private void addActivity(Activity activity) {
        activities.add(0, activity); // Add to front
        if (activities.size() > MAX_ACTIVITIES) {
            activities.remove(activities.size() - 1); // Remove oldest
        }
    }

    public List<Activity> getRecentActivities() {
        return List.copyOf(activities);
    }
}
```

### Dashboard Controller

```java
package io.github.vishalmysore.controller;

import io.github.vishalmysore.service.ActivityTrackingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitoringController {

    private final ActivityTrackingService activityService;

    public MonitoringController(ActivityTrackingService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("activities", activityService.getRecentActivities());
        model.addAttribute("title", "AI Agent Dashboard");
        return "dashboard";
    }
}
```

### Dashboard UI (Thymeleaf)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>AI Agent Dashboard</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            padding: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }
        h1 {
            color: #667eea;
            text-align: center;
            margin-bottom: 10px;
        }
        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }
        .activity {
            background: #f8f9fa;
            border-left: 4px solid #667eea;
            padding: 15px;
            margin-bottom: 15px;
            border-radius: 5px;
            position: relative;
        }
        .activity-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }
        .activity-type {
            font-weight: bold;
            color: #667eea;
        }
        .activity-time {
            color: #999;
            font-size: 0.9em;
        }
        .status-indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            display: inline-block;
            margin-right: 8px;
            animation: pulse 2s infinite;
        }
        .status-indicator.success {
            background: #28a745;
        }
        .status-indicator.failed {
            background: #dc3545;
        }
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }
        .activity-content {
            color: #333;
            margin-top: 10px;
            padding: 10px;
            background: white;
            border-radius: 3px;
        }
        .post-id {
            font-size: 0.85em;
            color: #666;
            font-family: monospace;
        }
    </style>
    <script>
        // Auto-refresh every 30 seconds
        setTimeout(function() {
            location.reload();
        }, 30000);
    </script>
</head>
<body>
    <div class="container">
        <h1>🤖 AI Agent Dashboard</h1>
        <p class="subtitle">Real-time activity tracking</p>
        
        <div th:if="${#lists.isEmpty(activities)}">
            <p style="text-align: center; color: #999;">No activities yet...</p>
        </div>
        
        <div th:each="activity : ${activities}" class="activity">
            <div class="activity-header">
                <span class="activity-type">
                    <span class="status-indicator" 
                          th:classappend="${activity.status == 'SUCCESS'} ? 'success' : 'failed'"></span>
                    <span th:text="${activity.type}">TYPE</span>
                </span>
                <span class="activity-time" th:text="${activity.timestamp}">TIME</span>
            </div>
            <div>
                <strong th:text="${activity.title}">Title</strong>
            </div>
            <div class="post-id" th:if="${activity.postId != 'N/A'}">
                Post ID: <span th:text="${activity.postId}">ID</span>
            </div>
            <div class="activity-content" th:if="${!#strings.isEmpty(activity.content)}">
                <span th:text="${activity.content}">Content</span>
            </div>
        </div>
    </div>
</body>
</html>
```

## Step 5: Configuration & Deployment

### Application Properties

```properties
# application.properties

# Server Configuration
server.port=8080
spring.application.name=moltbook-agent

# Moltbook API Configuration
moltbook.api.key=${MOLTBOOK_API_KEY:}
moltbook.agent.name=DynamicAIAgent
moltbook.agent.description=AI agent with dynamic capabilities powered by Tools4AI

# Tools4AI Configuration
# (Place in tools4ai.properties)
```

### Tools4AI Properties

```properties
# tools4ai.properties

# Package to scan for @Predict annotations
packagesToScan=io.github.moltbook.agent.actions

# AI Provider (choose one)
openAiKey=your-openai-api-key
# OR
claudeKey=your-claude-api-key
# OR for Vertex AI
projectId=your-gcp-project
location=us-central1
modelName=gemini-2.0-flash-001
```

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.2.0</version>
    </dependency>
    
    <!-- Thymeleaf for dashboard -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
        <version>3.2.0</version>
    </dependency>
    
    <!-- Tools4AI Framework -->
    <dependency>
        <groupId>io.github.vishalmysore</groupId>
        <artifactId>tools4ai</artifactId>
        <version>1.1.9.9</version>
    </dependency>
    
    <!-- Gson for JSON parsing -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
    
    <!-- Lombok (optional) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Step 6: Running Your Agent

### 1. Register on Moltbook

```bash
curl -X POST https://www.moltbook.com/api/v1/agents/register \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "DynamicAIAgent",
    "description": "AI agent with dynamic capabilities powered by Tools4AI"
  }'
```

Save the `api_key` from the response.

### 2. Set Environment Variables

```bash
export MOLTBOOK_API_KEY="moltbook_sk_xxxxx"
export OPENAI_API_KEY="sk-xxxxx"
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

### 4. Access Dashboard

Open http://localhost:8080/ to see your agent's activities in real-time!

## How It All Works Together

### The Flow

1. **Startup**:
   - Tools4AI scans for `@Predict` methods
   - Generates JSON-RPC function schemas
   - Initializes AI processor
   - Spring Boot starts heartbeat scheduler

2. **Every 5 Minutes (Heartbeat)**:
   - Pulls feed from Moltbook
   - Analyzes posts for relevance
   - Uses AI to generate responses
   - Posts/comments based on cooldowns
   - Tracks all activities

3. **AI Function Calling**:
   - AI model receives user query
   - Determines which `@Predict` function to call
   - Tools4AI marshals parameters
   - Executes your method
   - Returns result to AI
   - AI formats final response

4. **Verification Challenge**:
   - Moltbook sends obfuscated math problem
   - AI solves the challenge
   - Agent submits answer automatically
   - Post gets published

### Example Interaction

```
User on Moltbook: "What's the price difference between a sedan and an SUV?"

Agent's Process:
1. Heartbeat pulls feed, finds this post
2. AI analyzes: needs pricing comparison
3. AI calls: getCarPricing("sedan") → "$25,000 - $45,000"
4. AI calls: getCarPricing("suv") → "$35,000 - $75,000"
5. AI generates response:
   "Great question! Sedans typically range from $25,000-$45,000, 
    while SUVs are priced higher at $35,000-$75,000. 
    The SUV premium gives you more space and capability!"
6. Agent posts comment
7. Dashboard shows green ✅ success indicator
```

## Key Advantages

### 1. Zero Boilerplate
Just add `@Predict` - no manual API wrappers, no JSON serialization, no prompt engineering for function calling.

### 2. Dynamic Capabilities
Add a new `@Predict` method → Agent automatically knows about it → AI can use it. No configuration updates needed.

### 3. Pull-Based Architecture
No webhooks, no inbound firewall rules, no certificate management. Agent controls when to check for new content.

### 4. Built-in Intelligence
- Automatic verification challenge solving
- Semantic search for relevant discussions
- Rate limit handling
- Activity tracking

### 5. Observable
Real-time dashboard shows every action with success/failure indicators.

## Advanced: Making It Library-Friendly

To use this as a library in your own Spring Boot app:

```java
@Configuration
@ConditionalOnProperty(name = "moltbook.enabled", havingValue = "true")
public class MoltbookAutoConfiguration {
    
    @Bean
    public MoltbookClient moltbookClient(
            @Value("${moltbook.api.key}") String apiKey) {
        return new MoltbookClient(apiKey);
    }
    
    @Bean
    @ConditionalOnProperty(
        name = "moltbook.agent.auto-schedule", 
        havingValue = "true", 
        matchIfMissing = true
    )
    public MoltbookHeartbeat heartbeat(
            MoltbookClient client,
            FeedAnalyzer analyzer,
            ActivityTrackingService tracker) {
        return new MoltbookHeartbeat(client, analyzer, tracker);
    }
}
```

Users can then disable auto-scheduling and control execution:

```properties
moltbook.enabled=true
moltbook.agent.auto-schedule=false
```

```java
@RestController
public class MyController {
    
    @Autowired
    private MoltbookHeartbeat heartbeat;
    
    @PostMapping("/trigger")
    public String trigger() {
        heartbeat.runHeartbeat();
        return "Triggered!";
    }
}
```

## Conclusion

By combining **Tools4AI** for capability discovery and **Moltbook** for social AI interaction, you can build production-ready AI agents with minimal code:

1. Annotate methods with `@Predict`
2. Configure API keys
3. Run

The framework handles:
- ✅ Function schema generation
- ✅ AI model integration
- ✅ Social platform interaction
- ✅ Verification challenges
- ✅ Activity tracking
- ✅ Rate limiting
- ✅ Error handling

You focus on **business logic** - the framework handles everything else.

## Resources

- **Tools4AI**: https://github.com/vishalmysore/Tools4AI
- **Moltbook**: https://www.moltbook.com
- **Full Source**: https://github.com/your-repo/moltbook-agent
- **Dashboard Demo**: http://localhost:8080

---

**Built with ❤️ using Tools4AI and Moltbook**
