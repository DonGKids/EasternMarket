# =============================================================================
# Eastern Market 微服务多阶段构建 Dockerfile
# 用法：docker build --build-arg SERVICE=shop-user -t easternmarket/shop-user .
# =============================================================================

# ---- 阶段一：Maven 编译打包 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 配置阿里云 Maven 镜像源（加速国内依赖下载）
RUN mkdir -p /root/.m2 && cat > /root/.m2/settings.xml <<'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF

# 先复制 pom 文件，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
COPY shop-backend/pom.xml shop-backend/
COPY shop-backend/shop-common/pom.xml shop-backend/shop-common/
COPY shop-backend/shop-entity/pom.xml shop-backend/shop-entity/
COPY shop-backend/shop-parent/pom.xml shop-backend/shop-parent/
COPY shop-backend/shop-parent/shop-gateway/pom.xml shop-backend/shop-parent/shop-gateway/
COPY shop-backend/shop-parent/shop-user/pom.xml shop-backend/shop-parent/shop-user/
COPY shop-backend/shop-parent/shop-coupon/pom.xml shop-backend/shop-parent/shop-coupon/
COPY shop-backend/shop-parent/shop-product/pom.xml shop-backend/shop-parent/shop-product/
COPY shop-backend/shop-parent/shop-order/pom.xml shop-backend/shop-parent/shop-order/
COPY shop-backend/shop-parent/shop-cart/pom.xml shop-backend/shop-parent/shop-cart/

# 下载依赖（仅 pom 变化时才重新执行）
RUN mvn dependency:go-offline -B -s /root/.m2/settings.xml || true

# 复制全部源码并编译
COPY shop-backend shop-backend
RUN mvn clean package -DskipTests -B -s /root/.m2/settings.xml

# ---- 阶段二：运行时镜像（仅 JRE，体积小）----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 通过构建参数指定要运行的服务（shop-user / shop-product / ...）
ARG SERVICE

# 从构建阶段复制对应服务的可执行 jar
COPY --from=build /build/shop-backend/shop-parent/${SERVICE}/target/*.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai

# JVM 参数可通过环境变量 JAVA_OPTS 覆盖
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
