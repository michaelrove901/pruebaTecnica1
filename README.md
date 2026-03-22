# Investment Funds API

API backend para gestión de fondos de inversión construida con Java 21, Spring Boot 3, DynamoDB y arquitectura hexagonal.

El sistema permite que un cliente consulte fondos disponibles, se suscriba a un fondo, cancele una suscripción activa, consulte su portafolio y revise su historial de transacciones. Además, genera una notificación al suscribirse según la preferencia del cliente (`EMAIL` o `SMS`).

## Objetivo del Proyecto

Esta API resuelve un flujo básico de administración de fondos de inversión con reglas de negocio explícitas:

- El cliente inicia con saldo de `500000 COP`.
- Cada fondo tiene un monto mínimo de vinculación.
- Al suscribirse, el saldo se descuenta.
- Al cancelar una suscripción, el saldo se reintegra.
- Si el saldo no es suficiente, se responde con el mensaje exacto requerido por el negocio.

El proyecto está planteado como una solución simple, mantenible y preparada para evolucionar a un despliegue real en AWS.

## Arquitectura

Se implementó **arquitectura hexagonal** para separar la lógica de negocio de frameworks, transporte HTTP y persistencia.

### Capas

- `domain`
  - Entidades, value objects, enums y excepciones de negocio.
  - Puertos de entrada y salida.
  - No depende de Spring ni de AWS.

- `application`
  - Implementación de casos de uso.
  - Orquestación de reglas de negocio.
  - DTOs de aplicación y mappers de dominio.

- `infrastructure`
  - Adaptadores REST.
  - Seguridad JWT.
  - Adaptadores DynamoDB.
  - Configuración Spring y manejo global de errores.

### Decisiones Técnicas

- **Hexagonal architecture** para aislar dominio y facilitar testing.
- **DynamoDB** por alineación con AWS y modelo de acceso simple por claves e índices.
- **JWT** para autenticación básica stateless.
- **Spring Boot** para acelerar bootstrap, seguridad y exposición REST.
- **Pruebas unitarias** sobre dominio y casos de uso, evitando tests frágiles de contexto completo.

## Tecnologías Usadas

- Java 21
- Spring Boot 3
- Spring Security
- Gradle
- AWS SDK v2
- DynamoDB
- JWT (`jjwt`)
- JUnit 5
- Mockito
- Docker / Docker Compose
- AWS CloudFormation

## Funcionalidades Principales

- Listar fondos disponibles
- Autenticarse con JWT
- Suscribirse a un fondo
- Cancelar suscripción activa
- Consultar historial de transacciones
- Consultar portafolio actual del cliente
- Simular envío de notificación por `EMAIL` o `SMS`

## Seguridad

La autenticación se implementa con **JWT simple** usando Spring Security.

### Flujo

1. El cliente envía credenciales a `POST /api/v1/auth/login`.
2. Si son válidas, la API genera un token JWT firmado.
3. El cliente envía ese token en `Authorization: Bearer <token>`.
4. Un filtro JWT valida firma, emisor y expiración.
5. Si el token es válido, el `clientId` del token se inyecta en el contexto de seguridad y se usa en los endpoints protegidos.

### Claims usados

- `sub`: `clientId`
- `email`
- `roles`
- `iss`
- `exp`

### Endpoints públicos

- `POST /api/v1/auth/login`
- `GET /actuator/health`

### Endpoints protegidos

- `GET /api/v1/funds`
- `GET /api/v1/clients/me/portfolio`
- `POST /api/v1/subscriptions`
- `DELETE /api/v1/subscriptions/{subscriptionId}`
- `GET /api/v1/transactions`

## Cómo Correr Local

### 1. Levantar DynamoDB Local

```powershell
docker compose up -d
```

Esto levanta DynamoDB Local en:

```text
http://localhost:8000
```

### 2. Levantar la aplicación

```powershell
.\gradlew.bat bootRun
```

La aplicación arranca con perfil `local` por defecto.

### 3. Bootstrap local automático

En perfil `local`, la aplicación:

- crea las tablas DynamoDB si no existen
- crea los índices secundarios globales requeridos
- carga los 5 fondos iniciales
- crea un cliente de prueba con contraseña en BCrypt

### Credenciales de prueba

- Email: `client.local@example.com`
- Password: `Password123!`

## Cómo Probar

El proyecto incluye un archivo HTTP listo para recorrer el flujo completo:

- [`requests/local-end-to-end.http`](/C:/Users/micha/OneDrive/Documentos/Prueba/requests/local-end-to-end.http)

### Flujo sugerido

1. Login
2. Obtener fondos
3. Suscribirse a un fondo
4. Ver portafolio
5. Ver transacciones
6. Cancelar suscripción
7. Verificar portafolio después de cancelar

### Endpoints principales

#### Login

`POST /api/v1/auth/login`

```json
{
  "email": "client.local@example.com",
  "password": "Password123!"
}
```

#### Obtener fondos

`GET /api/v1/funds`

#### Suscribirse

`POST /api/v1/subscriptions`

```json
{
  "fundId": "DEUDAPRIVADA",
  "notificationPreference": "EMAIL"
}
```

#### Ver portafolio

`GET /api/v1/clients/me/portfolio`

#### Ver transacciones

`GET /api/v1/transactions`

Opcional:

`GET /api/v1/transactions?type=SUBSCRIPTION`

#### Cancelar suscripción

`DELETE /api/v1/subscriptions/{subscriptionId}`

## Manejo de Errores

La API expone un formato estándar:

```json
{
  "code": "INSUFFICIENT_BALANCE",
  "message": "No tiene saldo disponible para vincularse al fondo FPV_BTG_PACTUAL_ECOPETROL",
  "details": [],
  "path": "/api/v1/subscriptions",
  "timestamp": "2026-03-22T16:00:00-05:00"
}
```

Ejemplos de códigos:

- `VALIDATION_ERROR`
- `CLIENT_NOT_FOUND`
- `FUND_NOT_FOUND`
- `FUND_INACTIVE`
- `INSUFFICIENT_BALANCE`
- `ACTIVE_SUBSCRIPTION_ALREADY_EXISTS`
- `SUBSCRIPTION_NOT_FOUND`
- `SUBSCRIPTION_ALREADY_CANCELLED`
- `UNAUTHORIZED_ACCESS`
- `INVALID_CREDENTIALS`
- `UNAUTHORIZED`
- `INTERNAL_SERVER_ERROR`

## DynamoDB

### Tablas

- `clients`
- `funds`
- `subscriptions`
- `transactions`

### Índices usados

- `clients`
  - `email-index`

- `subscriptions`
  - `subscription-id-index`
  - `client-status-index`
  - `client-fund-status-index`

- `transactions`
  - `client-type-created-at-index`

El diseño está optimizado para:

- consultar por `clientId`
- obtener historial ordenado por fecha
- consultar suscripciones activas
- validar existencia de suscripción activa por cliente y fondo

## Despliegue

Se incluye una plantilla CloudFormation:

- [`infra/cloudformation/investment-funds-api.yml`](/C:/Users/micha/OneDrive/Documentos/Prueba/infra/cloudformation/investment-funds-api.yml)

### Qué crea

- tablas DynamoDB
- GSIs requeridos por la aplicación
- política IAM mínima para acceso a DynamoDB
- outputs útiles para configurar la app en producción

### Ejemplo de despliegue

```powershell
aws cloudformation deploy `
  --stack-name investment-funds-api-prod `
  --template-file infra/cloudformation/investment-funds-api.yml `
  --capabilities CAPABILITY_NAMED_IAM `
  --parameter-overrides EnvironmentName=prod JwtIssuer=investment-funds-api JwtExpirationMinutes=60
```

### Variables de entorno requeridas en producción

- `AWS_REGION`
- `DYNAMODB_CLIENTS_TABLE`
- `DYNAMODB_FUNDS_TABLE`
- `DYNAMODB_SUBSCRIPTIONS_TABLE`
- `DYNAMODB_TRANSACTIONS_TABLE`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_EXPIRATION_MINUTES`

## Estructura del Proyecto

```text
src/
├─ main/
│  ├─ java/com/example/funds/
│  │  ├─ domain/
│  │  ├─ application/
│  │  └─ infrastructure/
│  └─ resources/
└─ test/
   └─ java/com/example/funds/
```

### Estructura por responsabilidad

- `domain/model`
  - entidades y value objects

- `domain/port/in`
  - contratos de casos de uso

- `domain/port/out`
  - contratos de persistencia y notificación

- `application/usecase`
  - implementación de casos de uso

- `infrastructure/entrypoints/rest`
  - controladores, requests y responses

- `infrastructure/adapters/persistence/dynamodb`
  - repositorios y mappers DynamoDB

- `infrastructure/security`
  - JWT, filtros y configuración Spring Security

- `infrastructure/config`
  - configuración base y bootstrap local

## Calidad y Testing

El proyecto prioriza pruebas unitarias sobre lógica de negocio:

- dominio
- casos de uso
- flujos de error
- validación de reglas críticas

Los tests evitan depender del contexto completo de Spring cuando no es necesario.

## Mejoras Futuras

- Reemplazar el adaptador de notificación local por integración real con AWS SES/SNS
- Usar `TransactWriteItems` en suscripción/cancelación para consistencia fuerte en DynamoDB
- Agregar pruebas de integración REST
- Incorporar documentación OpenAPI/Swagger
- Gestionar `JWT_SECRET` con AWS Secrets Manager
- Desplegar la aplicación en ECS Fargate o Elastic Beanstalk
- Añadir observabilidad con métricas y trazas
- Agregar CI/CD

## Estado Actual

- Arquitectura hexagonal implementada
- Seguridad JWT operativa
- Persistencia DynamoDB operativa
- Entorno local reproducible
- Plantilla CloudFormation base disponible
- Tests unitarios pasando

Este proyecto está diseñado para ser entendible, defendible técnicamente y suficientemente simple para una prueba técnica, sin perder estructura profesional.
