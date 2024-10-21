FROM openjdk
LABEL authors="patrice"
WORKDIR /app
COPY ./out/artifacts/ranking_jar/ranking.jar /app/ranking.jar

ENTRYPOINT ["java", "-jar", "ranking.jar"]