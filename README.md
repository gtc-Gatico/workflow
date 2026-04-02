# Workflow Engine

一个类似 n8n 的可视化工作流引擎，使用 Java 和 Spring Boot 构建。

## 功能特性

- 🔄 **可视化工作流设计**: 支持创建、编辑和管理自动化工作流
- 🔌 **节点系统**: 支持多种节点类型（HTTP 请求、代码执行、条件判断等）
- ⚡ **实时执行**: 支持工作流的即时执行和调度触发
- 📊 **执行记录**: 完整的工作流执行历史和日志追踪
- 🔐 **凭证管理**: 安全存储和管理 API 密钥等敏感信息

## 技术栈

- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: SQLite (通过 Hibernate ORM)
- **构建工具**: Maven

## 项目结构

```
workflow-engine/
├── src/main/java/com/workflow/
│   ├── WorkflowApplication.java      # Spring Boot 主应用
│   ├── controller/                   # REST API 控制器
│   │   └── WorkflowController.java
│   ├── engine/                       # 核心引擎组件
│   │   ├── expression/               # 表达式引擎
│   │   ├── trigger/                  # 触发器（Webhook、Cron）
│   │   └── version/                  # 版本管理
│   ├── model/                        # 数据模型
│   │   ├── Workflow.java            # 工作流实体
│   │   ├── WorkflowNode.java        # 节点实体
│   │   ├── WorkflowExecution.java   # 执行记录
│   │   ├── Credential.java          # 凭证实体
│   │   └── ExecutionLog.java        # 执行日志
│   ├── node/                         # 节点执行器
│   │   ├── NodeExecutor.java        # 执行器接口
│   │   ├── NodeExecutionContext.java # 执行上下文
│   │   ├── NodeExecutionResult.java  # 执行结果
│   │   ├── HttpRequestExecutor.java  # HTTP 请求节点
│   │   ├── CodeExecutor.java         # 代码执行节点
│   │   ├── FilterExecutor.java       # 过滤节点
│   │   ├── SwitchExecutor.java       # 分支节点
│   │   └── ...
│   ├── repository/                   # 数据访问层
│   ├── service/                      # 业务逻辑层
│   └── config/                       # 配置类
└── src/main/resources/
    ├── application.properties        # 应用配置
    └── static/                       # 前端静态资源
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+

### 构建和运行

```bash
cd workflow-engine
mvn clean package
java -jar target/workflow-engine-1.0.0-SNAPSHOT.jar
```

应用将在 `http://localhost:8080` 启动。

### API 端点

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/workflows` | 获取所有工作流 |
| GET | `/api/workflows/{id}` | 获取指定工作流 |
| POST | `/api/workflows` | 创建新工作流 |
| PUT | `/api/workflows/{id}` | 更新工作流 |
| DELETE | `/api/workflows/{id}` | 删除工作流 |
| POST | `/api/workflows/{id}/execute` | 执行工作流 |
| GET | `/api/workflows/{id}/executions` | 获取执行历史 |

## 节点类型

### 内置节点

1. **Trigger Nodes** (触发器)
   - `webhook`: Webhook 触发
   - `cron`: 定时任务触发

2. **Action Nodes** (动作)
   - `http_request`: HTTP 请求
   - `code`: JavaScript 代码执行
   - `set`: 设置变量

3. **Control Nodes** (控制)
   - `switch`: 条件分支
   - `filter`: 数据过滤
   - `merge`: 合并数据
   - `splitInBatches`: 分批处理

## 开发指南

### 添加新节点类型

1. 实现 `NodeExecutor` 接口：

```java
@Component
public class MyCustomExecutor implements NodeExecutor {
    
    @Override
    public String getNodeType() {
        return "my_custom_node";
    }
    
    @Override
    public NodeExecutionResult execute(NodeExecutionContext context) throws Exception {
        // 实现节点逻辑
        Map<String, Object> config = context.getNodeConfig();
        // ...
        return NodeExecutionResult.success(outputData);
    }
}
```

2. Spring 会自动注册该执行器

## 配置说明

在 `application.properties` 中配置：

```properties
server.port=8080
spring.datasource.url=jdbc:sqlite:workflow.db
spring.jpa.hibernate.ddl-auto=update
logging.level.com.workflow=DEBUG
```

## 许可证

MIT License