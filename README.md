# TODO List（Web 前后端一体）

## 1. 技术选型
- **编程语言**：Java 8（Temurin 1.8），生态成熟、快速开发、环境普遍可用
- **后端框架**：Spring Boot 2.7.x（REST API + 静态资源托管），快速搭建、生态完善
- **前端**：原生 HTML/CSS/JS（无构建工具），降低运行门槛，直接对接后端 API
- **存储**：MySQL（JDBC），开源，运行环境贴近真实业务场景

## 2. 项目结构设计
```
src/
  main/
    java/com/iftech/todo/
      api/              # Controller（REST 接口）+ DTO（请求/响应对象）
      domain/           # 业务对象
      service/          # 业务逻辑
      storage/          # JDBC 存储实现（默认），文件存储仅作备选（profile=file）
    resources/
      static/           # 前端静态页面（index.html / app.js / styles.css）
      application.yml   # 配置项
      schema.sql        # MySQL 建表脚本（启动时可自动执行）
  test/
    java/...            # 接口级测试（MockMvc）
```

## 3. 需求细节与决策
- **标题必填**：创建接口 `title` 必填且去除首尾空格；更新时如果传了 `title` 也要求非空
- **描述可选**：空串/全空白会被归一化为 `null`
- **任务分类**：支持为任务设置 `category`，并支持按分类过滤与拉取分类列表
- **任务排序**：支持按创建时间/优先级/截止日期排序；默认按 `createdAt` 倒序返回（新建任务优先展示）
- **优先级**：`priority` 取值 1..3（1 低 / 2 中 / 3 高），不传则默认 2
- **截止时间必填**：`dueDate` 必填，格式为 `yyyy-MM-ddTHH:mm`（例如 `2026-01-02T10:30`）
- **提醒/通知**：页面会在截止时间前 10 分钟触发提醒（浏览器通知；不支持/未授权时回退为弹窗）
- **完成态**：提供 `PATCH /api/todos/{id}/toggle` 切换完成/未完成
- **持久化方式**：默认使用 MySQL 表 `todo_item`，通过 `JdbcTemplate` 直接读写

## 4. API 说明
- `GET /api/todos`：查询列表
  - query：`category`（可选，按分类过滤），`sort`（可选：`createdAt` / `priority` / `dueDate` / `due_date_desc`）
- `GET /api/todos/categories`：查询已有分类列表（去重、排序）
- `POST /api/todos`：新增待办
  - body：`{ "title": "xxx", "description": "xxx(可选)", "category": "学习(可选)", "priority": 1|2|3(可选), "dueDate": "2026-01-02T10:30(必填)" }`
- `PATCH /api/todos/{id}`：更新标题/描述/完成态（按需传字段）
- `PATCH /api/todos/{id}/toggle`：切换完成态
- `DELETE /api/todos/{id}`：删除

前端入口：`GET /`（静态页面由后端托管）。

## 5. 运行与测试方式
- Windows（推荐使用 Maven Wrapper）
  - 启动：`.\mvnw.cmd spring-boot:run`
  - 测试：`.\mvnw.cmd test`
  - 打包：`.\mvnw.cmd -DskipTests package`
- 启动后访问
  - 页面：`http://localhost:8080/`
  - 接口：`http://localhost:8080/api/todos`

### 5.1 已测试环境
- 操作系统：Windows 11
- Java：JDK 17（可运行）；项目编译目标为 Java 8（见 `pom.xml`）
- MySQL：8.x
- 浏览器：Chrome（通知功能依赖浏览器支持与权限）

### 5.2 MySQL 准备
- 默认连接配置（可在 `src/main/resources/application.yml` 修改）：
  - 地址：`localhost:3306`
  - 库：`todo_list`
  - 账号/密码：`root/root`
- 建库建表脚本：`src/main/resources/schema.sql`

### 5.3 测试说明
- 当前测试也使用 MySQL（配置见 `src/test/resources/application.yml`）
- 测试用例会在每次运行前执行 `DELETE FROM todo_item` 清空表数据，请确保该库为测试/开发库，避免误删生产数据

### 5.4 已知问题与不足
- 提醒/通知仅在页面打开时有效：当前实现基于浏览器定时器，不做服务端调度/推送
- 通知权限与兼容性差异：不同浏览器/系统对 `Notification` 支持与权限策略不同，未授权时会回退为弹窗
- 时间精度与格式：`dueDate` 目前精确到分钟（`yyyy-MM-ddTHH:mm`），不包含秒与时区信息
- 测试依赖 MySQL：CI/本地需要可用的 MySQL 实例，否则测试无法运行


## 6. 总结与反思
- 如果有更多时间：实现服务端调度（定时扫描即将到期任务）并提供多渠道通知；把提醒状态持久化到服务端；
- 最大亮点：在不引入前端构建与额外基础设施的前提下，完成了可运行的端到端闭环，并用清晰分层与可回归测试支撑持续迭代，实现了待办事项管理基本功能

## 7. AI 使用说明
- 使用 AI 协助生成基础脚手架代码、接口设计草案与前端交互逻辑，并根据 Java 8/Spring Boot 2.7 的兼容性要求做了调整与修正。
- ai生成的初始方案中使用的持久化方案是使用json文件存储，我修改为使用mysql存储。
- 使用ai生成了项目的初始说明文档（TODO_Template.md），并根据项目实际情况做了调整与完善。
