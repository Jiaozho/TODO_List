# TODO List（Web 前后端一体）

## 1. 技术选型
- **编程语言**：Java 8（Temurin 1.8），与后端简历一致，环境普遍可用
- **后端框架**：Spring Boot 2.7.x（REST API + 静态资源托管），快速搭建、生态完善
- **前端**：原生 HTML/CSS/JS（无构建工具），降低运行门槛，直接对接后端 API
- **存储**：MySQL（JDBC），运行环境贴近真实业务场景

## 2. 项目结构设计
```
src/
  main/
    java/com/iftech/todo/
      api/              # Controller + DTO
      domain/           # 领域模型
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
- **完成态**：提供 `PATCH /api/todos/{id}/toggle` 切换完成/未完成
- **列表排序**：默认按 `createdAt` 倒序返回（新建任务优先展示）
- **持久化方式**：默认使用 MySQL 表 `todo_item`，通过 `JdbcTemplate` 直接读写

## 4. API 说明
- `GET /api/todos`：查询列表
- `POST /api/todos`：新增待办
  - body：`{ "title": "xxx", "description": "xxx(可选)" }`
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

### 5.1 MySQL 准备
- 默认连接配置（可在 `src/main/resources/application.yml` 修改）：
  - 地址：`localhost:3306`
  - 库：`todo_list`
  - 账号/密码：`root/root`
- 建库建表脚本：`src/main/resources/schema.sql`

### 5.2 测试说明（重要）
- 当前测试也使用 MySQL（配置见 `src/test/resources/application.yml`）
- 测试用例会在每次运行前执行 `DELETE FROM todo_item` 清空表数据，请确保该库为测试/开发库

## 6. AI 使用说明
- 使用 AI 协助生成基础脚手架代码、接口设计草案与前端交互逻辑，并根据 Java 8/Spring Boot 2.7 的兼容性要求做了调整与修正。
