# Kafka 本地开发环境搭建指南（Docker · KRaft 模式）

本文档介绍如何在 **GitHub Codespaces** 或本地开发环境中，使用 Docker Compose 快速搭建 **KRaft 模式**的 Kafka 环境（无需 Zookeeper）。

---

## 目录

- [前置要求](#前置要求)
- [目录结构](#目录结构)
- [KRaft 模式简介](#kraft-模式简介)
- [快速启动](#快速启动)
- [验证服务状态](#验证服务状态)
- [Kafka 常用操作](#kafka-常用操作)
  - [创建 Topic](#创建-topic)
  - [查看 Topic 列表](#查看-topic-列表)
  - [生产消息](#生产消息)
  - [消费消息](#消费消息)
  - [删除 Topic](#删除-topic)
- [Kafka UI 管理界面](#kafka-ui-管理界面)
- [服务说明](#服务说明)
- [停止与清理](#停止与清理)
- [常见问题](#常见问题)

---

## KRaft 模式简介

**KRaft**（Kafka Raft）是 Kafka 2.8 引入、3.3 起正式 GA 的内置元数据管理机制，完全取代了对 Zookeeper 的依赖：

| 对比项 | 传统模式（Zookeeper） | KRaft 模式 |
|--------|----------------------|------------|
| 额外依赖 | 需要单独部署 Zookeeper | 无，Kafka 自包含 |
| 启动容器数 | 2（Zookeeper + Kafka） | 1（仅 Kafka） |
| 元数据存储 | Zookeeper | Kafka 内部 Raft 日志 |
| 推荐程度 | 逐步废弃 | ✅ 官方推荐 |

本项目使用 **broker+controller 合并节点**（single combined-mode node）的单机 KRaft 部署方式，适合本地开发与学习。

---

## 前置要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| Docker | >= 20.10 | 容器运行时 |
| Docker Compose | >= 2.0 | 编排工具（已内置于 Docker Desktop） |

> **GitHub Codespaces 用户**：Codespaces 已预装 Docker，无需额外安装，可直接跳到[快速启动](#快速启动)。

---

## 目录结构

```
KafkaDemo/
├── docker-compose.yml   # Docker Compose 编排文件
├── KAFKA_SETUP.md       # 本文档
└── README.md
```

---

## 快速启动

### 第一步：克隆仓库（Codespaces 中跳过此步）

```bash
git clone https://github.com/JingXu-Huan/KafkaDemo.git
cd KafkaDemo
```

### 第二步：启动所有服务

```bash
docker compose up -d
```

该命令会在后台启动以下两个服务：

- **Kafka Broker**（端口 `9092`，KRaft 模式，内置 Controller）
- **Kafka UI**（端口 `8080`）

### 第三步：等待服务就绪

```bash
docker compose ps
```

当 `STATUS` 列均显示 `healthy` / `Up` 时，服务启动完成：

```
NAME          IMAGE                           STATUS
kafka         confluentinc/cp-kafka:7.5.0     Up (healthy)
kafka-ui      provectuslabs/kafka-ui:v0.7.1   Up
```

---

## 验证服务状态

### 查看容器日志

```bash
# 查看 Kafka 日志
docker compose logs kafka

# 实时跟踪所有服务日志
docker compose logs -f
```

### 检查 Kafka Broker 是否可用

```bash
docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

---

## Kafka 常用操作

以下命令通过 `docker exec` 在 Kafka 容器内执行。

### 创建 Topic

```bash
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic my-topic \
  --partitions 3 \
  --replication-factor 1
```

### 查看 Topic 列表

```bash
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

### 查看 Topic 详情

```bash
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic my-topic
```

### 生产消息

```bash
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic my-topic
```

进入交互模式后，每行输入一条消息，按 `Ctrl+C` 退出。

### 消费消息

```bash
# 从头消费所有消息
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic my-topic \
  --from-beginning

# 只消费最新消息
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic my-topic
```

### 删除 Topic

```bash
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic my-topic
```

---

## Kafka UI 管理界面

启动后，可通过浏览器访问 Kafka 可视化管理界面：

- **本地访问**：[http://localhost:8080](http://localhost:8080)
- **Codespaces 访问**：在 VS Code 的「端口」面板中，找到端口 `8080`，点击「在浏览器中打开」

界面功能包括：
- 查看/创建/删除 Topic
- 浏览消息内容
- 监控 Consumer Group
- 查看 Broker 状态

---

## 服务说明

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| Kafka | `confluentinc/cp-kafka:7.5.0` | `9092` (外部), `29092` (内部), `9093` (Controller) | 消息队列核心服务，KRaft 模式（broker + controller 合并） |
| Kafka UI | `provectuslabs/kafka-ui:v0.7.1` | `8080` | Web 可视化管理界面 |

> **端口说明**：
> - `9092`：供宿主机/Codespaces 外部客户端连接使用
> - `29092`：供 Docker 网络内部各容器间通信使用
> - `9093`：KRaft Controller 内部通信端口（无需对外暴露）

---

## 停止与清理

### 停止服务（保留数据）

```bash
docker compose stop
```

### 停止并删除容器（保留数据卷）

```bash
docker compose down
```

### 完全清理（删除容器 + 数据卷）

```bash
docker compose down -v
```

---

## 常见问题

### Q1：Kafka 启动慢，`kafka-ui` 报连接错误？

KRaft 模式下 Kafka 需要初始化内置的 Controller Quorum，整体启动时间约需 **20~40 秒**。请等待 `docker compose ps` 中 `kafka` 状态变为 `healthy`。

### Q2：Codespaces 中端口无法访问？

在 VS Code 左下角点击「端口」标签页，确认 `8080`、`9092` 端口已被转发。若未显示，可手动添加端口转发。

### Q3：如何从宿主机代码连接 Kafka？

使用以下地址作为 `bootstrap.servers`：

```
localhost:9092
```

### Q4：容器内各服务如何互相访问 Kafka？

使用内部地址：

```
kafka:29092
```

### Q5：如何重置 Kafka 数据？

执行完全清理命令后重新启动：

```bash
docker compose down -v
docker compose up -d
```
