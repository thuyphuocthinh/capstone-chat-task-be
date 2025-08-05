# ---------- build stage ----------
FROM maven:3.8.7-openjdk-18 AS build

WORKDIR /build

# copy pom để tận dụng cache dependency
COPY pom.xml .

# sửa lỗi chính tả và tải dependency trước
RUN mvn dependency:go-offline -B

# copy source và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- runtime stage ----------
FROM amazoncorretto:17

WORKDIR /app

# copy jar từ build stage; assume artifact name includes version, dùng wildcard
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# giới hạn heap để tiết kiệm RAM (tuỳ chỉnh nếu cần)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

CMD java $JAVA_OPTS -jar app.jar
