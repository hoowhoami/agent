# 智能AI爬虫系统

基于 **LangChain4j** 和自定义 **StateGraph** 工作流引擎的高级智能爬虫系统。

## 🌟 核心特性

### 1. StateGraph 工作流引擎
类似 LangGraph 的状态图工作流系统，专为 Java 设计：
- **State** - 状态管理，支持状态快照和历史记录
- **StateNode** - 节点接口，每个节点执行特定任务
- **Transition** - 条件转换，支持复杂的分支逻辑
- **CompiledGraph** - 可执行的图，支持流式执行

### 2. 智能代理系统
基于 LLM 的智能代理，提供强大的 AI 能力：
- **PlannerAgent** - 分析页面并制定爬取计划
- **FetcherAgent** - 智能页面获取（支持 JavaScript 渲染）
- **ExtractorAgent** - LLM 驱动的内容提取
- **DecisionAgent** - 多步推理和决策
- **ValidatorAgent** - 验证提取结果

### 3. 自适应爬取策略
自动识别页面类型和分页策略：
- 页面类型识别（列表页、详情页、搜索结果等）
- 分页类型检测（数字分页、无限滚动、加载更多等）
- 智能链接提取和过滤
- 动态调整爬取策略

### 4. 智能反爬虫对抗
模拟真实用户行为，绕过常见反爬机制：
- User-Agent 轮换
- 智能请求头生成
- 随机延迟和行为模拟
- Cookie 和 Session 管理
- 代理 IP 管理（支持轮询、随机、最少使用策略）

### 5. 高级内容提取
LLM 驱动的多种提取方式：
- 结构化数据提取（JSON Schema）
- 实体识别和关系抽取
- 内容摘要和分类
- 问答式提取
- 批量提取

## 🚀 快速开始

### 1. 基础智能爬取

```bash
curl -X POST http://localhost:8080/api/smart-spider/basic \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com",
    "prompt": "提取文章标题、作者和发布时间",
    "llmProvider": "openai",
    "modelName": "gpt-4"
  }'
```

**工作流程：**
```
PlannerAgent → FetcherAgent → ExtractorAgent → ValidatorAgent
```

### 2. 高级智能爬取（带决策和分页）

```bash
curl -X POST http://localhost:8080/api/smart-spider/advanced \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/list",
    "prompt": "提取所有产品的名称、价格和描述",
    "maxPages": 5,
    "enableJavaScript": true,
    "llmProvider": "openai",
    "modelName": "gpt-4"
  }'
```

**工作流程：**
```
InitNode → PlannerAgent → FetcherAgent → ExtractorAgent → DecisionAgent
                                                              ↓
                                                         (条件分支)
                                                              ↓
                                    ┌─────────────────────────┴─────────────────────────┐
                                    ↓                         ↓                         ↓
                              FetcherAgent              ExtractorAgent            ValidatorAgent
                              (下一页)                   (重新提取)                  (验证)
                                                                                        ↓
                                                                                  FinalizeNode
```

### 3. 深度爬取（跟随链接）

```bash
curl -X POST http://localhost:8080/api/smart-spider/deep-crawl \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/category",
    "prompt": "提取每个产品详情页的完整信息",
    "maxDepth": 3,
    "maxPages": 20,
    "llmProvider": "openai",
    "modelName": "gpt-4"
  }'
```

**工作流程：**
```
InitNode → PlannerAgent → FetcherAgent → AnalyzerNode
                                              ↓
                                         (页面类型判断)
                                              ↓
                              ┌───────────────┴───────────────┐
                              ↓                               ↓
                        LinkFollowerNode                ExtractorAgent
                        (列表页，提取链接)                (详情页，提取内容)
                              ↓                               ↓
                        FetcherAgent                    FinalizeNode
                        (访问详情页)
```

## 📦 项目结构

```
src/main/java/com/java/spider/
├── core/
│   ├── stategraph/              # StateGraph 核心框架
│   │   ├── State.java           # 状态管理
│   │   ├── StateNode.java       # 节点接口
│   │   ├── Transition.java      # 转换接口
│   │   ├── StateGraph.java      # 图定义
│   │   └── CompiledGraph.java   # 可执行图
│   │
│   ├── agent/                   # 智能代理
│   │   ├── BaseAgent.java       # 代理基类
│   │   ├── PlannerAgent.java    # 规划代理
│   │   ├── FetcherAgent.java    # 获取代理
│   │   ├── ExtractorAgent.java  # 提取代理
│   │   ├── DecisionAgent.java   # 决策代理
│   │   └── ValidatorAgent.java  # 验证代理
│   │
│   ├── workflow/                # 工作流
│   │   └── SmartSpiderWorkflow.java  # 智能爬虫工作流
│   │
│   ├── strategy/                # 自适应策略
│   │   └── AdaptiveStrategy.java     # 自适应爬取策略
│   │
│   ├── antibot/                 # 反爬虫对抗
│   │   ├── AntiBot.java         # 反爬虫工具
│   │   ├── SessionManager.java  # 会话管理
│   │   └── ProxyManager.java    # 代理管理
│   │
│   ├── extractor/               # 高级提取器
│   │   └── AdvancedExtractor.java    # 高级内容提取器
│   │
│   └── fetcher/                 # 页面获取
│       ├── HtmlPageFetcher.java      # HTML 获取
│       └── JavaScriptPageFetcher.java # JS 渲染获取
│
├── controller/
│   └── SmartSpiderController.java    # REST API
│
└── service/
    └── SmartSpiderService.java       # 业务逻辑
```

## 🔧 配置

### LLM 配置

在 `application.yml` 中配置 LLM 提供商：

```yaml
llm:
  openai:
    api-key: ${OPENAI_API_KEY}
    model-name: gpt-4
    base-url: https://api.openai.com/v1
    temperature: 0.7
    max-tokens: 2000
    timeout: 60
    max-retries: 3

  ollama:
    base-url: http://localhost:11434
    model-name: llama2
    timeout: 120

  anthropic:
    api-key: ${ANTHROPIC_API_KEY}
    model-name: claude-3-sonnet-20240229
```

## 🎯 使用示例

### 示例 1：提取新闻文章

```json
{
  "url": "https://news.example.com/article/123",
  "prompt": "提取以下信息：\n- 标题\n- 作者\n- 发布时间\n- 正文内容\n- 标签\n返回 JSON 格式",
  "llmProvider": "openai",
  "modelName": "gpt-4"
}
```

**响应：**
```json
{
  "success": true,
  "workflowType": "basic",
  "data": {
    "title": "AI 技术的最新进展",
    "author": "张三",
    "publishTime": "2024-01-20",
    "content": "...",
    "tags": ["AI", "技术", "创新"]
  },
  "metadata": {
    "pageType": "ARTICLE",
    "isValid": true,
    "validationScore": 0.95
  },
  "executionPath": ["PlannerAgent", "FetcherAgent", "ExtractorAgent", "ValidatorAgent"]
}
```

### 示例 2：爬取电商产品列表

```json
{
  "url": "https://shop.example.com/products",
  "prompt": "提取所有产品的：名称、价格、评分、库存状态",
  "maxPages": 5,
  "enableJavaScript": true,
  "llmProvider": "openai",
  "modelName": "gpt-4"
}
```

### 示例 3：深度爬取博客

```json
{
  "url": "https://blog.example.com",
  "prompt": "提取每篇文章的标题、摘要、作者和阅读量",
  "maxDepth": 2,
  "maxPages": 10,
  "llmProvider": "openai",
  "modelName": "gpt-4"
}
```

## 🧪 高级功能

### 1. 自定义工作流

```java
StateGraph graph = new StateGraph("CustomWorkflow");

// 添加自定义节点
graph.addNode("custom", state -> {
    // 自定义逻辑
    return state;
});

// 添加条件边
graph.addConditionalEdge("custom", state -> {
    if (state.get("condition")) {
        return "nextNode";
    }
    return null; // 结束
});

CompiledGraph compiled = graph.compile();
State result = compiled.execute(initialState);
```

### 2. 使用高级提取器

```java
AdvancedExtractor extractor = new AdvancedExtractor(llm);

// 结构化提取
JsonNode data = extractor.extractStructured(
    content,
    jsonSchema,
    "提取产品信息"
);

// 实体识别
List<Entity> entities = extractor.extractEntities(content);

// 内容摘要
String summary = extractor.summarize(content, 200);

// 关系抽取
List<Relation> relations = extractor.extractRelations(content);
```

### 3. 代理管理

```java
ProxyManager proxyManager = new ProxyManager();
proxyManager.addProxy("proxy1.example.com", 8080);
proxyManager.addProxy("proxy2.example.com", 8080, "user", "pass");
proxyManager.setStrategy(RotationStrategy.RANDOM);

ProxyInfo proxy = proxyManager.getNextProxy();
```

## 📊 性能优化

1. **并发控制** - 使用 Virtual Threads (Java 21)
2. **缓存策略** - 页面内容缓存
3. **连接池** - HTTP 连接复用
4. **批量处理** - 批量提取多个页面
5. **增量爬取** - 只爬取更新的内容

## 🔒 安全性

1. **请求限流** - 防止过度请求
2. **User-Agent 轮换** - 避免被识别为爬虫
3. **代理支持** - 分散请求来源
4. **Cookie 管理** - 维持会话状态
5. **错误重试** - 自动重试失败的请求

## 🐛 故障排查

### 问题 1：LLM 调用失败
- 检查 API Key 是否正确
- 检查网络连接
- 检查 LLM 服务是否可用

### 问题 2：页面获取失败
- 检查 URL 是否正确
- 尝试启用 JavaScript 渲染
- 检查是否被反爬虫拦截

### 问题 3：提取结果不准确
- 优化 prompt 描述
- 尝试使用更强大的模型（如 GPT-4）
- 检查页面内容是否完整

## 📝 开发计划

- [ ] 支持更多 LLM 提供商
- [ ] 添加图像识别能力
- [ ] 支持验证码自动识别
- [ ] 添加分布式爬取支持
- [ ] 提供可视化工作流编辑器
- [ ] 支持实时流式输出

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题，请联系：whoami@example.com
