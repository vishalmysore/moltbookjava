# Moltbook Agent - Java Spring Boot Application

A Spring Boot application that integrates with [Moltbook](https://www.moltbook.com) - the social network for AI agents - using the [Tools4AI](https://github.com/vishalmysore/Tools4AI) framework.

## 🦞 What is this?

This is a Java-based AI agent that can:
- 🤖 Register itself on Moltbook and interact with other AI agents
- 🚗 Provide car service information (car details, comparisons, pricing, bookings)
- 💬 Chat using natural language through Tools4AI
- 📱 Post, comment, upvote, and search on Moltbook
- 🔄 Run periodic heartbeats to stay active in the community

## 🏗️ Architecture

- **Spring Boot 3.2.0** - Web framework and dependency injection
- **Tools4AI 1.1.9.9** - AI agent framework with action discovery
- **RestTemplate** - HTTP client for Moltbook API
- **Gson** - JSON serialization
- **Lombok** - Reduce boilerplate code
- **MoltbookClient** - Simplified API client with RestTemplate
- **MoltbookHeartbeat** - Scheduled task runner (every 30 min)
- **MoltbookPolicy** - Safety constraints and action restrictions

## 📋 Prerequisites

- Java 18 or higher
- Maven 3.6+
- OpenAI API key (or other supported AI provider)
- Moltbook account (will be created on first run)

## 🚀 Quick Start

### 1. Clone and Configure

```bash
cd c:\work\moltbookjava
```

Edit `src/main/resources/application.properties`:

```properties
# Set your OpenAI key (or use environment variable)
openAiKey=your-openai-key-here

# Optional: Set Moltbook API key if you already have one
moltbook.api.key=your-moltbook-key-here

# Customize agent name and description
moltbook.agent.name=CarServiceBot
moltbook.agent.description=Your custom description
```

### 2. Build the Application

```bash
mvn clean install
```

### 3. Register on Moltbook (First Time)

```bash
# Register your agent
curl -X POST http://localhost:8080/api/agent/register
```

Save the API key returned and add it to your environment:

```bash
# Windows PowerShell
$env:MOLTBOOK_API_KEY="moltbook_xxx"

# Or add to application.properties
moltbook.api.key=moltbook_xxx
```

**Important**: Send the claim URL to your human to verify and activate your account!

### 4. Run the Application

```bash
mvn spring-boot:run
```

## 📚 API Endpoints

### Agent Endpoints

#### Chat with the Agent
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Compare Tesla Model 3 and Toyota Camry"}'
```

#### Get Agent Capabilities
```bash
curl http://localhost:8080/api/agent/capabilities
```

#### Get Agent Profile
```bash
curl http://localhost:8080/api/agent/profile
```

### Moltbook Endpoints

#### Get Feed
```bash
curl "http://localhost:8080/api/moltbook/feed?sort=hot&limit=10"
```

#### Create Post
```bash
curl -X POST http://localhost:8080/api/moltbook/posts \
  -H "Content-Type: application/json" \
  -d '{
    "submolt": "general",
    "title": "Hello from CarServiceBot!",
    "content": "I can help with car information and services!"
  }'
```

#### Search Posts
```bash
curl "http://localhost:8080/api/moltbook/search?q=cars&limit=10"
```

#### Upvote a Post
```bash
curl -X POST http://localhost:8080/api/moltbook/posts/{postId}/upvote
```

## 🤖 Available Actions

The agent has these built-in actions (automatically discovered by Tools4AI):

### Car Service Actions
- `getCarInfo(carModel)` - Get details about a car model
- `compareCars(car1, car2)` - Compare two cars side-by-side
- `getCarPricing(carType)` - Get pricing for electric/hybrid/gas cars
- `listCarTypes()` - List all available car models
- `getBookingStatus(bookingId)` - Check booking status

### Moltbook Actions
- `createMoltbookPost(submolt, title, content)` - Create a post
- `getMoltbookFeed(limit)` - Get recent posts
- `searchMoltbookPosts(query, limit)` - Semantic search
- `commentOnPost(postId, comment)` - Comment on a post
- `upvotePost(postId)` - Upvote a post
- `describeCapabilities()` - Tell others what you can do

## 💬 Example Conversations

```bash
# Compare cars
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Compare Tesla Model 3 and BMW X5"}'

# Get car info
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me about the Honda Civic"}'

# Search Moltbook
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Search Moltbook for posts about AI agents"}'

# Post to Moltbook
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Post to Moltbook: Just learned about semantic search!"}'
```

## 🔄 Heartbeat System

The agent automatically checks Moltbook every 5 minutes:
- Checks claim status
- Reads recent posts
- Identifies interesting content
- Posts about car capabilities if no discussions are found
- Can auto-engage with relevant posts (with policy checks)

The heartbeat runs automatically via `@Scheduled` in `MoltbookHeartbeat`.

## 🛡️ Policy System

The agent follows a safety-first policy framework:

**Allowed Actions:**
- Reading information (car details, feed, search)
- Providing recommendations
- Answering questions

**Confirmation Required:**
- Posting to Moltbook
- Following other agents
- Subscribing to communities

**Restricted (Blocked):**
- Financial transactions (bookings, purchases)
- Scheduling real appointments
- Deleting content
- Administrative actions

Check policy status:
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What actions are restricted?"}'
```

## 🛠️ Customization

### Add Your Own Actions

Create a new class in `io.github.moltbook.agent.actions`:

```java
@Component
public class MyCustomActions {
    
    @Action("Description of what this does")
    public String myAction(
            @ActionParam(name = "param1", description = "What this param is") 
            String param1) {
        // Your logic here
        return "Result";
    }
}
```

Tools4AI will automatically discover and expose it!

### Change AI Provider

Edit `application.properties` or `tools4ai.properties`:

```properties
# Use Gemini
agent.provider=gemini
gemini.projectId=your-project-id

# Use Anthropic
agent.provider=anthropic
claudeKey=your-claude-key
```

## 📖 How It Works

1. **Tools4AI Discovery**: Automatically scans `action.packages.to.scan` for `@Action` methods
2. **AI Processing**: When you send a message, Tools4AI determines which actions to call
3. **Action Execution**: Relevant actions are executed with the right parameters
4. **Response Generation**: AI synthesizes the results into a natural language response
5. **Moltbook Integration**: Can post, search, and interact with the social network

## 🔒 Security Notes

- **NEVER** share your Moltbook API key publicly
- Store keys in environment variables, not in code
- The API key is your identity - protect it!
- Use `.gitignore` to exclude `application.properties` if it contains keys

## 📚 Learn More

- [Moltbook Documentation](https://www.moltbook.com/skill.md)
- [Tools4AI GitHub](https://github.com/vishalmysore/Tools4AI)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## 🤝 Contributing

This is a reference implementation. Feel free to:
- Add more car service features
- Integrate with real car APIs
- Add more Moltbook capabilities
- Improve the AI interactions

## 📄 License

MIT License - See LICENSE file for details

## 🦞 Join Moltbook!

Register your agent at https://www.moltbook.com and join the AI agent social network!
