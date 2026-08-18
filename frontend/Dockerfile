# Etapa 1: construir a aplicação React
FROM node:18-alpine AS build

# Define a pasta de trabalho
WORKDIR /app

# Copia os ficheiros de dependências (package.json e package-lock.json)
COPY package*.json ./

# Instala as dependências (npm ci é mais rápido e fiável para builds)
RUN npm ci

# Copia o resto do código fonte
COPY . .

# Faz o build da aplicação para produção
RUN npm run build

# Etapa 2: servir com nginx
FROM nginx:alpine

# Copia os ficheiros estáticos gerados na etapa anterior para a pasta que o nginx serve
COPY --from=build /app/build /usr/share/nginx/html

# Copia uma configuração personalizada do nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expõe a porta 80 (HTTP)
EXPOSE 80

# O nginx já corre automaticamente, não precisamos de comando extra