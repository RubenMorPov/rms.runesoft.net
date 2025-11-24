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
│       │   │   ├── MessageController.java
│       │   │   └── UptimeKumaController.java
│       │   ├── entity/
│       │   │   ├── Message.java
│       │   │   └── UptimeAlert.java
│       │   ├── model/
│       │   │   ├── MessageDTO.java
│       │   │   └── UptimeKumaWebhookDTO.java
│       │   ├── repository/
│       │   │   ├── MessageRepository.java
│       │   │   └── UptimeAlertRepository.java
│       │   └── service/
│       │       ├── EmailService.java
│       │       ├── MessageService.java
│       │       └── UptimeKumaService.java
│       └── resources/
│           └── application.yml
├── pom.xml
├── EMAIL_SETUP.md
└── UPTIME_KUMA_SETUP.md
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

#### Mensajería

**Enviar un mensaje a RabbitMQ:**
```bash
POST http://localhost:8080/api/messages/send
Content-Type: application/json

{
  "id": 1,
  "content": "Mensaje de prueba",
  "sender": "usuario@example.com"
}
```

**Obtener todos los mensajes:**
```bash
GET http://localhost:8080/api/messages
```

**Obtener mensajes por remitente:**
```bash
GET http://localhost:8080/api/messages/sender/usuario@example.com
```

#### Uptime Kuma Integration

**Recibir webhook de Uptime Kuma:**
```bash
POST http://localhost:8080/api/uptime/webhook
Content-Type: application/json

{
  "heartbeat": "{...}",
  "monitor": "{...}",
  "msg": "Monitor is down",
  "monitorName": "API Production",
  "monitorUrl": "https://api.example.com",
  "status": "down",
  "ping": "5000",
  "time": "2024-01-15T10:30:00"
}
```

**Obtener todas las alertas:**
```bash
GET http://localhost:8080/api/uptime/alerts
```

**Obtener las 10 alertas más recientes:**
```bash
GET http://localhost:8080/api/uptime/alerts/recent
```

**Obtener alertas de un monitor específico:**
```bash
GET http://localhost:8080/api/uptime/alerts/monitor/{monitorName}
```

**Obtener alertas por estado (up/down/pending):**
```bash
GET http://localhost:8080/api/uptime/alerts/status/{status}
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

### Mensajería RabbitMQ

1. El cliente envía un mensaje a través del endpoint `/api/messages/send`
2. El mensaje se publica en RabbitMQ (exchange: `rms.exchange`, routing key: `rms.routing.key`)
3. El `MessageConsumer` escucha la cola `rms.queue` y recibe el mensaje
4. El mensaje se procesa y se guarda en PostgreSQL
5. **[NUEVO]** Se envía una notificación por email con el contenido del mensaje (si está habilitado)
6. Los mensajes guardados pueden consultarse mediante los endpoints GET

### Uptime Kuma Integration

1. Uptime Kuma detecta un cambio de estado en un monitor
2. Envía un webhook HTTP POST a `/api/uptime/webhook`
3. La aplicación recibe el webhook y procesa los datos
4. La alerta se guarda en PostgreSQL (tabla `uptime_alerts`)
5. Se publica un mensaje en RabbitMQ con los detalles de la alerta
6. Si el estado es "down", se envía un email de notificación (si está habilitado)
7. Las alertas pueden consultarse mediante los endpoints GET

Para más detalles sobre cómo configurar Uptime Kuma, consulta [UPTIME_KUMA_SETUP.md](UPTIME_KUMA_SETUP.md).

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
