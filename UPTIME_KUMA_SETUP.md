# Configuración de Uptime Kuma

Este documento explica cómo configurar Uptime Kuma para enviar alertas a esta aplicación mediante webhooks.

## 📋 Requisitos Previos

1. Tener Uptime Kuma instalado y en funcionamiento
2. Esta aplicación RMS desplegada y accesible desde el servidor de Uptime Kuma
3. Conocer la URL pública o IP del servidor donde corre esta aplicación

## 🔧 Configuración en Uptime Kuma

### Paso 1: Crear una Notificación Webhook

1. Accede al panel de administración de Uptime Kuma
2. Ve a **Settings** → **Notifications**
3. Haz clic en **Setup Notification**
4. Selecciona **Webhook** como tipo de notificación

### Paso 2: Configurar el Webhook

**Configuración básica:**

- **Friendly Name**: `RMS Application` (o el nombre que prefieras)
- **Notification Type**: `Webhook`
- **POST URL**: `http://TU_SERVIDOR:8080/api/uptime/webhook`
  - Reemplaza `TU_SERVIDOR` con la IP o dominio donde corre esta aplicación
  - Si usas HTTPS, cambia el protocolo: `https://TU_SERVIDOR:8080/api/uptime/webhook`
  - Si cambias el puerto en `application.yml`, usa ese puerto

**Content Type**: `application/json`

**Request Body** (copia y pega exactamente):

```json
{
  "heartbeat": "{heartbeat}",
  "monitor": "{monitor}",
  "msg": "{msg}",
  "monitorName": "{monitorName}",
  "monitorUrl": "{monitorUrl}",
  "status": "{status}",
  "ping": "{ping}",
  "time": "{time}",
  "localDateTime": "{localDateTime}",
  "localTime": "{localTime}",
  "utcDateTime": "{utcDateTime}",
  "utcTime": "{utcTime}"
}
```

### Paso 3: Probar el Webhook

1. Haz clic en **Test** en la configuración del webhook
2. Deberías ver un mensaje de éxito en Uptime Kuma
3. Verifica en los logs de esta aplicación que el webhook fue recibido
4. Guarda la notificación con **Save**

### Paso 4: Asociar el Webhook a Monitores

1. Ve a la lista de monitores en Uptime Kuma
2. Edita el monitor que deseas configurar
3. En la sección **Notifications**, selecciona la notificación webhook que creaste
4. Guarda los cambios

## 📊 ¿Qué Hace Esta Integración?

Cuando Uptime Kuma detecta un cambio de estado en un monitor:

1. **Recibe el webhook**: El endpoint `/api/uptime/webhook` recibe la notificación
2. **Guarda en PostgreSQL**: La alerta se almacena en la tabla `uptime_alerts`
3. **Publica a RabbitMQ**: El mensaje se envía a la cola `rms.queue`
4. **Envía Email**: Si está configurado, envía un correo cuando el estado es "down"

## 🔍 Endpoints Disponibles

### Recibir Webhook
```http
POST /api/uptime/webhook
Content-Type: application/json
```

### Consultar Alertas

**Obtener todas las alertas (ordenadas por fecha):**
```http
GET /api/uptime/alerts
```

**Obtener las 10 alertas más recientes:**
```http
GET /api/uptime/alerts/recent
```

**Obtener alertas de un monitor específico:**
```http
GET /api/uptime/alerts/monitor/{monitorName}
```

**Obtener alertas por estado:**
```http
GET /api/uptime/alerts/status/{status}
```
Estados posibles: `up`, `down`, `pending`

## 📧 Notificaciones por Email

Si tienes configurado el sistema de email (ver `EMAIL_SETUP.md`), recibirás automáticamente un correo cuando:

- Un monitor cambia a estado **DOWN** (❌)
- El email incluye:
  - Nombre del monitor
  - Estado actual
  - URL monitoreada
  - Tiempo de respuesta (ping)
  - Fecha y hora de la alerta

## 🗄️ Estructura de la Base de Datos

Las alertas se guardan en la tabla `uptime_alerts`:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | ID único (autoincremental) |
| monitor_name | VARCHAR(255) | Nombre del monitor |
| monitor_url | VARCHAR(500) | URL monitoreada |
| status | VARCHAR(50) | Estado: up, down, pending |
| ping | INTEGER | Tiempo de respuesta en ms |
| message | TEXT | Mensaje de la alerta |
| heartbeat_data | TEXT | JSON con datos del heartbeat |
| monitor_data | TEXT | JSON con datos del monitor |
| alert_time | TIMESTAMP | Fecha/hora de la alerta |
| created_at | TIMESTAMP | Fecha/hora de creación en BD |

## 🔐 Seguridad

### Recomendaciones

1. **Usa HTTPS**: En producción, siempre usa HTTPS para el webhook
2. **Firewall**: Configura el firewall para que solo el servidor de Uptime Kuma pueda acceder al endpoint
3. **Autenticación**: Considera agregar un token de autenticación en el webhook si es necesario

### Ejemplo con Token Básico (opcional)

Si quieres agregar un token de seguridad básico, modifica el **Request Body**:

```json
{
  "token": "TU_TOKEN_SECRETO",
  "heartbeat": "{heartbeat}",
  ...
}
```

Y modifica `UptimeKumaController.java` para validar el token.

## 🐛 Solución de Problemas

### El webhook no llega

1. Verifica que la aplicación esté corriendo: `curl http://localhost:8080/actuator/health`
2. Revisa los logs de la aplicación
3. Asegúrate que no hay firewall bloqueando el puerto 8080
4. Verifica que la URL del webhook sea accesible desde el servidor de Uptime Kuma

### Los emails no se envían

1. Verifica que tengas configurado `EMAIL_SETUP.md`
2. Revisa que `email.notification.enabled=true` en `application.yml`
3. Los emails **solo** se envían cuando el estado es **DOWN**

### No se guardan las alertas en la base de datos

1. Verifica que PostgreSQL esté corriendo: `docker ps`
2. Revisa la conexión en `application.yml`
3. Comprueba los logs de Hibernate

## 📝 Ejemplo de Payload Completo

Este es un ejemplo del JSON que Uptime Kuma enviará:

```json
{
  "heartbeat": "{\"monitorID\":1,\"status\":0,\"time\":\"2024-01-15 10:30:00\",\"msg\":\"HTTP Error 500\",\"ping\":5000,\"important\":true}",
  "monitor": "{\"id\":1,\"name\":\"API Production\",\"url\":\"https://api.example.com\",\"type\":\"http\"}",
  "msg": "🔴 API Production is down",
  "monitorName": "API Production",
  "monitorUrl": "https://api.example.com",
  "status": "down",
  "ping": "5000",
  "time": "2024-01-15 10:30:00",
  "localDateTime": "2024-01-15 10:30:00",
  "localTime": "10:30:00",
  "utcDateTime": "2024-01-15 09:30:00",
  "utcTime": "09:30:00"
}
```

## 🚀 Pruebas con Postman/cURL

Para probar el endpoint manualmente:

```bash
curl -X POST http://localhost:8080/api/uptime/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "heartbeat": "{\"monitorID\":1,\"status\":0,\"time\":\"2024-01-15 10:30:00\"}",
    "monitor": "{\"id\":1,\"name\":\"Test Monitor\",\"url\":\"https://example.com\"}",
    "msg": "Test alert",
    "monitorName": "Test Monitor",
    "monitorUrl": "https://example.com",
    "status": "down",
    "ping": "500",
    "time": "2024-01-15T10:30:00"
  }'
```

## 🎯 Casos de Uso

1. **Monitoreo de APIs**: Recibe alertas cuando tus APIs fallan
2. **Monitoreo de Sitios Web**: Notificaciones cuando tu sitio web está caído
3. **Historial de Incidencias**: Consulta todas las alertas históricas desde la base de datos
4. **Integración con Sistemas Internos**: El mensaje en RabbitMQ puede ser consumido por otros servicios
5. **Dashboards**: Usa los endpoints GET para crear dashboards personalizados

## 📚 Referencias

- [Documentación oficial de Uptime Kuma](https://github.com/louislam/uptime-kuma)
- [Uptime Kuma Webhook Configuration](https://github.com/louislam/uptime-kuma/wiki/Notification-Methods)
