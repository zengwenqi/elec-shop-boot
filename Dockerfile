# 使用轻量级JRE作为基础镜像
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 添加应用用户，避免使用root用户运行
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 复制已打包好的jar文件到容器中
# 假设你的jar包名称是elec-shop-boot-1.0.jar，如有不同请修改
COPY elec-shop-boot-1.0.jar app.jar

# 更改文件权限
RUN chown -R appuser:appgroup /app

# 切换到非root用户
USER appuser

# 暴露端口（Spring Boot默认端口）
EXPOSE 9090

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
