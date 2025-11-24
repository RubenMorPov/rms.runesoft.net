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

### Email (Opcional)
Para habilitar notificaciones por email cuando se recibe un mensaje:

1. Copia el archivo de ejemplo:
   ```bash
   cp .env.example .env
   ```

2. Configura las variables de entorno en el archivo `.env`:
   - `MAIL_USERNAME`: Tu email (ej: Gmail)
   - `MAIL_PASSWORD`: App password de tu cuenta de email
   - `NOTIFICATION_EMAIL`: Email donde recibirás las notificaciones

3. **Para Gmail**: Genera una App Password en https://myaccount.google.com/apppasswords

4. Ejecuta la aplicación con las variables de entorno:
   ```bash
   # Windows PowerShell
   $env:MAIL_USERNAME="tu-email@gmail.com"
   $env:MAIL_PASSWORD="tu-app-password"
   $env:NOTIFICATION_EMAIL="destino@example.com"
   mvn spring-boot:run
   ```

5. Para deshabilitar las notificaciones por email, configura en `application.yml`:
   ```yaml
   email:
     notification:
       enabled: false
   ```

## Flujo de Trabajo

1. El cliente envía un mensaje a través del endpoint `/api/messages/send`
2. El mensaje se publica en RabbitMQ (exchange: `rms.exchange`, routing key: `rms.routing.key`)
3. El `MessageConsumer` escucha la cola `rms.queue` y recibe el mensaje
4. El mensaje se procesa y se guarda en PostgreSQL
5. **[NUEVO]** Se envía una notificación por email con el contenido del mensaje (si está habilitado)
6. Los mensajes guardados pueden consultarse mediante los endpoints GET

## Tecnologías

- **Spring Boot 3.4.0**
- **Java 21**
- **PostgreSQL 16**
- **RabbitMQ 3.13**
- **Spring AMQP**
- **Spring Data JPA**
- **Spring Mail** (notificaciones por email)
- **Lombok**

## Desarrollo

Para desarrollo local, asegúrate de que los servicios de Docker estén ejecutándose antes de iniciar la aplicación Spring Boot.

## Licencia

Privado - Runesoft
