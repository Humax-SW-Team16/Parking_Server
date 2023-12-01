# jdk17 Image Start - 도커 허브에서 베이스이미지인 openjdk:17을 가져옴
FROM openjdk:17

# 인자 설정 - JAR_File
ARG JAR_FILE=build/libs/*.jar

# jar 파일 복제
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]