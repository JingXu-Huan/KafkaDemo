# KafkaDemo

学习 Kafka 的仓库。

## 快速开始

📖 **[Kafka Docker 环境搭建指南](./KAFKA_SETUP.md)**

使用 Docker Compose 一键启动 Kafka（KRaft 模式，无需 Zookeeper）+ Kafka UI，支持 GitHub Codespaces。

```bash
docker compose up -d
```

详细说明请参阅 [KAFKA_SETUP.md](./KAFKA_SETUP.md)。

## Spring Boot 项目

仓库已初始化为 **Spring Boot + Spring Kafka** 项目（Maven，Java 17）。

### 启动应用

```bash
mvn spring-boot:run
```

应用默认端口为 `8081`，Kafka 地址为 `localhost:9092`。

### 发送测试消息

```bash
curl -X POST http://localhost:8081/messages \
	-H "Content-Type: application/json" \
	-d '{"message":"hello kafka"}'
```

消息会发送到 `demo-topic`，并由内置消费者打印日志。
