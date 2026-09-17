# =============================================================================
# Etapa de build
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# =============================================================================
# Etapa de execucao
# JRE Alpine: ~100MB vs ~400MB do JDK completo.
# Imagem menor = pull mais rapido no cold start do Render.
# =============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Flags JVM para container com pouca RAM (Render Free ~512MB):
#
#   -XX:+UseSerialGC
#     GC simples e de baixo overhead. Adequado para app de baixo throughput
#     com uma unica instancia. G1GC (default) e otimizado para alta concorrencia
#     e desperdicaria memoria no Render Free.
#
#   -Xms64m -Xmx256m
#     Limita o heap. Sem limite, a JVM pode tentar alocar mais do que o Render
#     oferece e ser morta pelo OOM killer do container.
#
#   -XX:MaxMetaspaceSize=128m
#     Limita o Metaspace (onde ficam as classes carregadas).
#     Spring Boot carrega muitas classes via reflection. Sem limite, o Metaspace
#     pode crescer indefinidamente e consumir RAM do container.
#
#   -XX:+OptimizeStringConcat
#     Otimizacao de concatenacao de strings, util para serialization JSON.
#
#   REMOVIDO: -XX:TieredStopAtLevel=1
#     Essa flag desativa o JIT C2, o que parece acelerar o startup mas na
#     pratica AUMENTA o tempo em apps Spring Boot porque o Spring faz reflection
#     massiva no startup e sem JIT cada operacao e muito mais lenta.
#     Com lazy-initialization=true no profile de producao, o JIT completo
#     e mais eficiente porque compila apenas os beans realmente usados.
CMD ["java", "-XX:+UseSerialGC", "-Xms64m", "-Xmx256m", "-XX:MaxMetaspaceSize=128m", "-XX:+OptimizeStringConcat", "-Dspring.profiles.active=production", "-jar", "app.jar"]
