# Etapa 1: construir a aplicação com Maven
# Usamos uma imagem que já tem Maven e Java 17
FROM maven:3.9-eclipse-temurin-17 AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o ficheiro pom.xml (lista de dependências) para dentro do container
COPY pom.xml .

# Baixa todas as dependências do Maven (para não ter de as baixar sempre que o código mudar)
RUN mvn dependency:go-offline

# Agora copia o resto do código fonte (src, etc.)
COPY src ./src

# Compila a aplicação e gera um ficheiro .jar (ignorando os testes)
RUN mvn clean package -DskipTests

# Etapa 2: criar a imagem final (mais leve)
# Usamos uma imagem pequena que só tem o Java Runtime Environment (JRE)
FROM eclipse-temurin:17-jre-alpine

# Define o diretório de trabalho
WORKDIR /app

# Copia o ficheiro .jar da etapa anterior para esta imagem final
COPY --from=build /app/target/*.jar app.jar

# Indica que a porta 8080 será exposta (mas só para documentação)
EXPOSE 8080

# Comando que será executado quando o container iniciar
# Ativa o perfil "prod" do Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]