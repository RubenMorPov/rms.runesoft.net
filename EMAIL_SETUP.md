# Configuración de Email - Guía Detallada

## Configurar Gmail para Notificaciones

### Paso 1: Habilitar verificación en dos pasos
1. Ve a tu cuenta de Google: https://myaccount.google.com/security
2. En "Inicio de sesión en Google", selecciona "Verificación en 2 pasos"
3. Sigue los pasos para habilitarlo

### Paso 2: Generar App Password
1. Ve a https://myaccount.google.com/apppasswords
2. Nombre de la app: "RMS Runesoft"
3. Copia el password de 16 caracteres generado

### Paso 3: Configurar variables de entorno

#### Opción A: Variables de entorno del sistema (Windows PowerShell)
```powershell
$env:MAIL_USERNAME="tu-email@gmail.com"
$env:MAIL_PASSWORD="xxxx xxxx xxxx xxxx"
$env:NOTIFICATION_EMAIL="destino@example.com"
```

#### Opción B: Archivo .env (requiere plugin adicional)
Crea un archivo `.env` en la raíz del proyecto:
```properties
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=xxxxxxxxxxxxxxxx
NOTIFICATION_EMAIL=destino@example.com
```

#### Opción C: Modificar application.yml directamente (NO RECOMENDADO para producción)
```yaml
spring:
  mail:
    username: tu-email@gmail.com
    password: tu-app-password
    
email:
  notification:
    recipient: destino@example.com
```

## Configurar otros proveedores de email

### Outlook/Hotmail
```yaml
spring:
  mail:
    host: smtp-mail.outlook.com
    port: 587
    username: tu-email@outlook.com
    password: tu-password
```

### Yahoo Mail
```yaml
spring:
  mail:
    host: smtp.mail.yahoo.com
    port: 587
    username: tu-email@yahoo.com
    password: tu-app-password
```

### SMTP Personalizado
```yaml
spring:
  mail:
    host: smtp.tu-dominio.com
    port: 587
    username: tu-usuario
    password: tu-password
```

## Deshabilitar Notificaciones por Email

Si no quieres usar notificaciones por email, simplemente configura:

```yaml
email:
  notification:
    enabled: false
```

O no configures las variables de entorno. La aplicación funcionará normalmente sin enviar emails.

## Formato del Email de Notificación

Cuando se recibe un mensaje, se enviará un email con el siguiente formato:

```
═══════════════════════════════════════════════════
NUEVO MENSAJE RECIBIDO EN RMS
═══════════════════════════════════════════════════

Remitente: usuario@example.com
Fecha: 24/11/2025 19:30:45

Contenido del mensaje:
───────────────────────────────────────────────────
Este es el contenido del mensaje que fue recibido
através de RabbitMQ.
───────────────────────────────────────────────────

Este mensaje ha sido procesado y almacenado en la base de datos.

Saludos,
Sistema RMS - Runesoft
```

## Troubleshooting

### Error: Authentication failed
- Verifica que hayas generado una App Password (no uses tu contraseña normal de Gmail)
- Asegúrate de que la verificación en 2 pasos esté habilitada

### Error: Connection timeout
- Verifica tu conexión a internet
- Asegúrate de que el puerto 587 no esté bloqueado por tu firewall

### No se envían emails pero no hay errores
- Verifica que `email.notification.enabled=true` esté configurado
- Revisa los logs de la aplicación para ver si hay errores silenciados
- Verifica que las variables de entorno estén correctamente configuradas

## Testing

Para probar el envío de emails, simplemente envía un mensaje usando el endpoint:

```bash
POST http://localhost:8080/api/messages/send
Content-Type: application/json

{
  "content": "Prueba de notificación por email",
  "sender": "test@example.com"
}
```

Deberías recibir un email en la dirección configurada en `NOTIFICATION_EMAIL`.
