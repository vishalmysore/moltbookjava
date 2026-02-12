# Moltbook Agent Architecture

## Pull-Based Design (No Web Server!)

This agent uses a **pull-based architecture** - it only makes outbound REST calls to Moltbook. No inbound requests, no REST endpoints to expose.

```
┌─────────────────────────────────────┐
│   Moltbook Agent (Java Process)     │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   @Scheduled Heartbeat       │  │
│  │   (runs every 5 minutes)     │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  1. Pull Feed (GET /feed)    │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  2. Analyze for Cars         │  │
│  │     (FeedAnalyzer)           │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  3. Tools4AI Actions         │  │
│  │     (Generate Response)      │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  4. Post Action              │  │
│  │     (POST /upvote, /comment) │  │
│  └──────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
                │
                │ All outbound HTTP
                ▼
    ┌───────────────────────┐
    │  Moltbook REST API    │
    │  (www.moltbook.com)   │
    └───────────────────────┘
```

## Key Points

### 1. No Web Server
```java
@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
spring.main.web-application-type=none
```

### 2. Pull Feed Example
```java
// Pull feed from Moltbook
List<FeedItem> feed = moltbookClient.getFeed(50);

// Analyze for car-related content
for (FeedItem item : feed) {
    String text = item.getContent();
    
    // NLP / intent detection
    if (looksCarRelated(text)) {
        // Use Tools4AI action
        String response = aiAgent.chat("Answer this: " + text);
        moltbookClient.createComment(item.getId(), response);
    }
}
```

### 3. Semantic Search
```java
// Find car discussions even if not in your feed
String searchJson = moltbookClient.semanticSearch(
    "car service recommendations", 
    "posts", 
    10
);
```

### 4. Simple REST Client Pattern
```java
@Component
public class MoltbookClient {
    private final RestTemplate restTemplate;
    private final String apiKey; // From env var
    
    public String getFeed(int limit) {
        return get("/feed?sort=new&limit=" + limit);
    }
    
    private String get(String path) {
        HttpHeaders headers = authHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            BASE_URL + path,
            HttpMethod.GET,
            entity,
            String.class
        ).getBody();
    }
}
```

## What You DON'T Need

❌ **REST Controllers** - No one calls your agent  
❌ **Web Server** - No inbound HTTP  
❌ **@GetMapping/@PostMapping** - Not a service  
❌ **Server Port** - Nothing to expose  

## What You DO Need

✅ **MoltbookClient** - Makes outbound calls  
✅ **@Scheduled Heartbeat** - Pull loop  
✅ **Tools4AI @Actions** - Local methods  
✅ **FeedAnalyzer** - Parse and filter  
✅ **AIAgent** - Generate responses  

## API Endpoints Used (All Outbound)

```bash
# Check status
GET /api/v1/agents/status

# Get feed
GET /api/v1/feed?sort=new&limit=50

# Semantic search
GET /api/v1/search?q=car+service&type=posts

# Engage
POST /api/v1/posts/{id}/upvote
POST /api/v1/posts/{id}/comments
```

## Run Instructions

```bash
# Set environment variable
export MOLTBOOK_API_KEY="your_key_here"
export OPENAI_API_KEY="your_openai_key"

# Run (no web server starts!)
mvn spring-boot:run

# You'll see:
# 🦞 Moltbook heartbeat starting...
# Retrieved 50 items from feed
# 🚗 Found 3 car-related items
# Processing: "Which Tesla model is best?"
# 💬 Commenting on post abc123
# ✅ Heartbeat completed successfully
```

## Architecture Benefits

1. **Simple** - No web complexity
2. **Secure** - No exposed ports
3. **Pull-Based** - You control when to check
4. **Rate-Limit Safe** - Sleep between actions
5. **Tools4AI Native** - Actions run locally

## Other Agents Can't Call You Directly

They interact via Moltbook content:
- Read your posts
- Comment on your posts
- Mention you in discussions
- Upvote your content

Everything goes through Moltbook - no direct agent-to-agent calls!
