# Docker Services

## Servicios

- **PostgreSQL**: Base de datos relacional en puerto 5432
- **RabbitMQ**: Message broker en puerto 5672, con interfaz de gestión en puerto 15672

## Uso

### Iniciar servicios
```bash
docker-compose up -d
```

### Detener servicios
```bash
docker-compose down
```

### Ver logs
```bash
docker-compose logs -f
```

## Credenciales

### PostgreSQL
- **Database**: rms_db
- **User**: rms_user
- **Password**: rms_password
- **Port**: 5432

### RabbitMQ
- **User**: rms_user
- **Password**: rms_password
- **AMQP Port**: 5672
- **Management UI**: http://localhost:15672
