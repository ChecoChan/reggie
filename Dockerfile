# 基础镜像
FROM openjdk:8-jdk-alpine

# 指定工作目录
WORKDIR /app

# 将 jar 包添加到工作目录
ADD target/reggie-1.0-SNAPSHOT.jar .
# 添加资源图片
COPY target/classes/img /app/img

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java","-jar","/app/reggie-1.0-SNAPSHOT.jar","--spring.config.location=classpath:/application-docker.yml"]