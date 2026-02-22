# SC2 StarFinder Backend

StarFinder 是一个星际争霸2社区平台的后端服务，提供玩家约战匹配、MMR查询、合作模式匹配、直播列表、教学资源、赛事宣传等功能。

## 技术栈

- **框架**: Spring Boot 3.2.6
- **语言**: Java 21
- **数据库**: MySQL 8.x
- **ORM**: MyBatis + JPA
- **缓存**: Redis
- **邮件**: Spring Mail (Gmail SMTP)

## 功能模块

| 模块 | 描述 | 端点前缀 |
|------|------|---------|
| 用户系统 | 注册、登录、MMR管理、个人资料 | `/api/user` |
| 约战匹配 | 1v1/2v2/3v3/4v4/合作模式匹配 | `/api/user/match` |
| SC2 Pulse | 玩家搜索、MMR查询、直播列表 | `/api/sc2` |
| 外挂图鉴 | 外挂举报与查询 | `/api/cheater` |
| 挂人区 | 社区举报板 | `/api/public-report` |
| 赛事宣传 | 赛事发布与审核 | `/api/event` |
| 教学视频 | 视频教学管理 | `/api/tutorial` |
| 陪玩陪练 | 陪玩信息发布 | `/api/coaching` |
| 文字教学 | 文字教学内容 | `/api/text-tutorial` |
| Replay文件 | SC2Replay上传与下载 | `/api/replay` |
| 直播列表 | 直播信息管理 | `/api/stream` |
| 管理后台 | 用户/内容管理 | `/api/admin` |

## 快速启动

### 环境要求
- Java 21+
- MySQL 8.x（端口 33069）
- Redis（端口 6379）

### 使用部署脚本（推荐）

```bash
cd /root/coding/starfinder
bash deploy.sh
```

### 手动启动

```bash
cd SC2-StarFinder-Backend

# 设置环境变量
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=33069
export MYSQL_DB=sc2_starfinder

# 编译
./mvnw clean package -DskipTests

# 启动
java -Xmx256m -Xms128m -XX:+UseSerialGC \
    -Dspring.profiles.active=mysql \
    -jar target/star-finder-0.0.1-SNAPSHOT.jar
```

服务启动后监听端口 **8089**。

## 配置文件

- `src/main/resources/application.yaml` - 主配置（文件上传限制等）
- `src/main/resources/application-mysql.yaml` - MySQL 连接配置

## 安全特性

- 文件上传限制：单文件最大 20MB，每用户最多 100MB
- 每用户最多 20 个 Replay 文件
- 文件类型验证（仅允许 .SC2Replay）
- 路径遍历防护
- 内容长度验证
- 外挂举报审核机制

## 数据库表

| 表名 | 描述 |
|------|------|
| `users` | 用户信息、MMR、指挥官、合作模式等级 |
| `cheaters` | 外挂举报（需审核） |
| `public_reports` | 挂人区记录 |
| `events` | 赛事信息 |
| `tutorials` | 教学视频 |
| `coaching_posts` | 陪玩陪练信息 |
| `text_tutorials` | 文字教学内容 |
| `replay_files` | Replay 文件记录 |
| `streams` | 直播列表 |

## API 数据来源

MMR 数据由 [SC2 Pulse](https://sc2pulse.nephest.com/) 提供，感谢其开放 API。

## 许可证

本项目为开源项目，欢迎 Star 和 Fork：[GitHub](https://github.com/SeleiXi/SC2-StarFinder-Backend)
