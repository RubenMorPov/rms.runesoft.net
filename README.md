# RMS - Repository Management System

Sistema de gestión de repositorios para Runesoft utilizando Spring Boot, PostgreSQL y RabbitMQ.

## Requisitos

- Java 21
- Maven 3.8+
- Docker y Docker Compose

## Estructura del Proyecto

```
rms.runesoft.net/
├── Docker/
│   ├── docker-compose.yml
│   └── README.md
├── src/
│   └── main/
│       ├── java/net/runesoft/rms/
│       │   ├── RmsApplication.java
│       │   ├── config/
│       │   │   └── RabbitMQConfig.java
│       │   ├── consumer/
│       │   │   └── MessageConsumer.java
│       │   ├── controller/
│       │   │   └── MessageController.java
│       │   ├── entity/
│       │   │   └── Message.java
│       │   ├── model/
│       │   │   └── MessageDTO.java
│       │   ├── repository/
│       │   │   └── MessageRepository.java
│       │   └── service/
│       │       └── MessageService.java
│       └── resources/
│           └── application.yml
└── pom.xml
```

## Inicio Rápido

### 1. Iniciar los servicios de Docker

```bash
cd Docker
docker-compose up -d
```

Esto iniciará:
- PostgreSQL en `localhost:5432`
- RabbitMQ en `localhost:5672` (Management UI: `localhost:15672`)

### 2. Compilar y ejecutar la aplicación

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## Funcionalidades

### Endpoints API

#### Enviar un mensaje a RabbitMQ
```bash
POST http://localhost:8080/api/messages/send
Content-Type: application/json

{
  "id": 1,
  "content": "Mensaje de prueba",
  "sender": "usuario@example.com"
}
```

#### Obtener todos los mensajes
```bash
GET http://localhost:8080/api/messages
```

#### Obtener mensajes por remitente
```bash
GET http://localhost:8080/api/messages/sender/usuario@example.com
```

## Configuración

### Base de Datos (PostgreSQL)
- **Host**: localhost:5432
- **Database**: rms_db
- **Usuario**: rms_user
- **Contraseña**: rms_password

### RabbitMQ
- **Host**: localhost:5672
- **Usuario**: rms_user
- **Contraseña**: rms_password
- **Management UI**: http://localhost:15672

## Flujo de Trabajo

1. El cliente envía un mensaje a través del endpoint `/api/messages/send`
2. El mensaje se publica en RabbitMQ (exchange: `rms.exchange`, routing key: `rms.routing.key`)
3. El `MessageConsumer` escucha la cola `rms.queue` y recibe el mensaje
4. El mensaje se procesa y se guarda en PostgreSQL
5. Los mensajes guardados pueden consultarse mediante los endpoints GET

## Tecnologías

- **Spring Boot 3.3.5**
- **Java 21**
- **PostgreSQL 16**
- **RabbitMQ 3.13**
- **Spring AMQP**
- **Spring Data JPA**
- **Lombok**

## Desarrollo

Para desarrollo local, asegúrate de que los servicios de Docker estén ejecutándose antes de iniciar la aplicación Spring Boot.

## Licencia

Privado - Runesoft
