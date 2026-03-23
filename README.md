# Investment Funds API

API backend para gestion de fondos de inversion construida con Java 21, Spring Boot 3, DynamoDB, JWT y arquitectura hexagonal.

La aplicacion permite que un cliente se autentique, consulte fondos disponibles, se suscriba a un fondo, cancele una suscripcion activa, revise su portafolio actual y consulte su historial de transacciones. El diseno prioriza separacion de responsabilidades, mantenibilidad y preparacion para despliegue en AWS como parte de una prueba tecnica backend.

## 1. Descripcion del Proyecto

### Proposito

Esta API resuelve un flujo simplificado de administracion de fondos de inversion en el que un cliente puede operar sobre un catalogo predefinido de fondos bajo reglas de negocio explicitas.

### Funcionalidades Principales

- Autenticacion con JWT
- Consulta de fondos disponibles
- Suscripcion a un fondo
- Cancelacion de una suscripcion activa
- Consulta del portafolio del cliente
- Consulta del historial de transacciones
- Emision de una notificacion al crear una suscripcion

### Reglas de Negocio Principales

- El cliente inicia con `500000 COP`
- Cada fondo tiene un monto minimo de vinculacion
- Al suscribirse a un fondo, el saldo disminuye
- Al cancelar una suscripcion, el saldo se reintegra
- Si el saldo no es suficiente, la API responde con el mensaje exacto requerido por el negocio

## 2. Arquitectura

El proyecto utiliza **arquitectura hexagonal** para aislar la logica de negocio de frameworks, persistencia y transporte HTTP.

### Capas

- `domain`
  - Entidades
  - Value objects
  - Excepciones de negocio
  - Puertos de entrada y salida
  - No depende de Spring ni de AWS

- `application`
  - Implementaciones de casos de uso
  - DTOs de aplicacion
  - Orquestacion del flujo de negocio

- `infrastructure`
  - Controladores REST
  - Seguridad y JWT
  - Adaptadores DynamoDB
  - Configuracion Spring
  - Manejo global de errores

### Decisiones Tecnicas Principales

- **Arquitectura hexagonal** para preservar el aislamiento del dominio y facilitar testing
- **DynamoDB** por alineacion con AWS y por un modelo de acceso basado en claves e indices
- **JWT stateless** para mantener la seguridad simple y lista para produccion
- **Spring Boot 3** para acelerar configuracion, seguridad y exposicion REST
- **CloudFormation** para infraestructura reproducible
- **Elastic Beanstalk** como opcion simple de despliegue administrado para la aplicacion

## 3. Tecnologias Usadas

- Java 21
- Spring Boot 3
- Gradle
- Spring Security
- DynamoDB
- AWS SDK v2
- JWT (`jjwt`)
- AWS CloudFormation
- AWS Elastic Beanstalk
- JUnit 5
- Mockito
- Docker Compose

## 4. Ejecucion Local

### Prerrequisitos

- Java 21
- Docker Desktop
- PowerShell

### Levantar DynamoDB Local

```powershell
docker compose up -d
```

Esto levanta DynamoDB Local en:

```text
http://localhost:8000
```

### Levantar la Aplicacion

```powershell
.\gradlew.bat bootRun
```

La aplicacion corre con perfil `local` por defecto.

### Que Ocurre Automaticamente en Local

Al arrancar, el inicializador local:

- crea las tablas DynamoDB si no existen
- crea los GSIs requeridos
- carga los 5 fondos del negocio
- crea un cliente demo con password BCrypt

### Credenciales Demo

- `clientId`: `client-local-001`
- `email`: `client.local@example.com`
- `password`: `Password123!`

### Como Probar Localmente

El repositorio incluye un archivo HTTP listo para recorrer el flujo completo:

- [`requests/local-end-to-end.http`](/C:/Users/micha/OneDrive/Documentos/Prueba/requests/local-end-to-end.http)

Orden sugerido:

1. Login
2. Listar fondos
3. Suscribirse
4. Ver portafolio
5. Ver transacciones
6. Cancelar suscripcion
7. Ver portafolio nuevamente

## 5. Despliegue en AWS

Existen dos templates CloudFormation:

- [`infra/cloudformation/investment-funds-api.yml`](/C:/Users/micha/OneDrive/Documentos/Prueba/infra/cloudformation/investment-funds-api.yml)
- [`infra/cloudformation/investment-funds-api-eb.yml`](/C:/Users/micha/OneDrive/Documentos/Prueba/infra/cloudformation/investment-funds-api-eb.yml)

### `investment-funds-api.yml`

Este es el **stack de datos y permisos**.

Crea:

- tablas DynamoDB
  - `clients`
  - `funds`
  - `subscriptions`
  - `transactions`
- GSIs usados por el codigo
- politica IAM minima para acceso a DynamoDB
- outputs utiles para configurar el runtime de la aplicacion

Este stack debe desplegarse primero.

No despliega la aplicacion Spring Boot. Su objetivo es provisionar la capa de persistencia y la politica IAM que luego usara el runtime.

Ejemplo:

```powershell
aws cloudformation deploy `
  --stack-name investment-funds-api-prod `
  --template-file infra/cloudformation/investment-funds-api.yml `
  --capabilities CAPABILITY_NAMED_IAM `
  --parameter-overrides EnvironmentName=prod JwtIssuer=investment-funds-api JwtExpirationMinutes=60 AppBootstrapDemoData=true
```

### `investment-funds-api-eb.yml`

Este es el **stack de despliegue de la aplicacion** sobre Elastic Beanstalk.

Crea:

- Elastic Beanstalk application
- application version a partir de un JAR subido a S3
- Elastic Beanstalk environment
- rol EC2
- instance profile
- service-linked role
- variables de entorno requeridas por la aplicacion

Este stack debe desplegarse despues del stack DynamoDB y despues de subir el JAR a S3.

Este stack es responsable del **runtime de la aplicacion**, no de crear las tablas DynamoDB.

### Generar el JAR

```powershell
.\gradlew.bat clean bootJar
```

El artefacto generado normalmente queda en:

```text
build/libs/investment-funds-api-0.0.1-SNAPSHOT.jar
```

Puedes validar que exista con:

```powershell
Get-ChildItem build\libs
```

### Subir el JAR a S3

Ejemplo:

```powershell
aws s3 cp `
  build/libs/investment-funds-api-0.0.1-SNAPSHOT.jar `
  s3://my-artifacts-bucket/investment-funds-api/investment-funds-api-0.0.1-SNAPSHOT.jar
```

### Desplegar o Actualizar el Stack de Elastic Beanstalk

Ejemplo:

```powershell
aws cloudformation deploy `
  --stack-name investment-funds-api-eb-prod `
  --template-file infra/cloudformation/investment-funds-api-eb.yml `
  --capabilities CAPABILITY_NAMED_IAM `
  --parameter-overrides `
    EnvironmentName=prod `
    ApplicationName=investment-funds-api `
    ArtifactBucket=my-artifacts-bucket `
    ArtifactKey=investment-funds-api/investment-funds-api-0.0.1-SNAPSHOT.jar `
    DynamoPolicyArn=<DYNAMO_POLICY_ARN> `
    AwsRegion=<AWS_REGION> `
    ClientsTableName=<CLIENTS_TABLE_NAME> `
    FundsTableName=<FUNDS_TABLE_NAME> `
    SubscriptionsTableName=<SUBSCRIPTIONS_TABLE_NAME> `
    TransactionsTableName=<TRANSACTIONS_TABLE_NAME> `
    JwtIssuer=investment-funds-api `
    JwtExpirationMinutes=60 `
    JwtSecret=<JWT_SECRET> `
    AppBootstrapDemoData=true
```

### Parametros que Debe Completar el Usuario

Para el stack DynamoDB:

- `EnvironmentName`
- `JwtIssuer`
- `JwtExpirationMinutes`
- `AppBootstrapDemoData`

Para el stack Elastic Beanstalk:

- `EnvironmentName`
- `ApplicationName`
- `ArtifactBucket`
- `ArtifactKey`
- `DynamoPolicyArn`
- `AwsRegion`
- `ClientsTableName`
- `FundsTableName`
- `SubscriptionsTableName`
- `TransactionsTableName`
- `JwtIssuer`
- `JwtExpirationMinutes`
- `JwtSecret`
- `AppBootstrapDemoData`

Fuente recomendada para varios de esos valores:

- `DynamoPolicyArn`: output `DynamoPolicyArn` del stack `investment-funds-api.yml`
- `ClientsTableName`: output `ClientsTableName`
- `FundsTableName`: output `FundsTableName`
- `SubscriptionsTableName`: output `SubscriptionsTableName`
- `TransactionsTableName`: output `TransactionsTableName`
- `AwsRegion`: output `AwsRegion`
- `JwtIssuer`: usar el mismo issuer definido en el stack de datos o en la configuracion de la aplicacion
- `JwtSecret`: suministrarlo manualmente o desde una fuente segura

### Validar que el Backend Quedo Arriba

Despues del despliegue, toma la URL del entorno Elastic Beanstalk desde el output del stack y ejecuta:

```powershell
curl http://<elastic-beanstalk-url>/actuator/health
```

Respuesta esperada:

```json
{"status":"UP"}
```

Luego valida login:

```powershell
curl -X POST http://<elastic-beanstalk-url>/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"client.local@example.com\",\"password\":\"Password123!\"}"
```

Si `APP_BOOTSTRAP_DEMO_DATA=false`, el login demo anterior no funcionara a menos que crees los datos del cliente por otro mecanismo.

## 6. Variables de Entorno

### `SPRING_PROFILES_ACTIVE`

Define el perfil Spring activo.

- Local: `local`
- AWS: `prod`

### `SERVER_PORT`

Puerto usado por el servidor de la aplicacion.

- En Elastic Beanstalk normalmente es `5000`

### `APP_BOOTSTRAP_DEMO_DATA`

Controla si se siembran datos demo al arrancar.

- `true`: crea fondos y cliente demo si no existen
- `false` o sin definir: no siembra nada

### `AWS_REGION`

Region AWS usada por el cliente DynamoDB en produccion.

Ejemplo:

```text
us-east-1
```

### `DYNAMODB_CLIENTS_TABLE`

Nombre fisico de la tabla de clientes.

### `DYNAMODB_FUNDS_TABLE`

Nombre fisico de la tabla de fondos.

### `DYNAMODB_SUBSCRIPTIONS_TABLE`

Nombre fisico de la tabla de suscripciones.

### `DYNAMODB_TRANSACTIONS_TABLE`

Nombre fisico de la tabla de transacciones.

### `JWT_ISSUER`

Issuer esperado para firmar y validar los JWT.

### `JWT_EXPIRATION_MINUTES`

Tiempo de expiracion del token en minutos.

### `JWT_SECRET`

Secreto HMAC usado para generar y validar JWT.

Este valor es obligatorio en produccion y no debe hardcodearse.

Intencionalmente no se expone como output de CloudFormation.

## 7. Bootstrap Demo

`APP_BOOTSTRAP_DEMO_DATA` existe para soportar **ambientes de validacion y demo**.

Cuando esta habilitada:

- crea los 5 fondos del negocio si no existen
- crea el cliente demo si no existe

Cliente demo:

- `clientId`: `client-local-001`
- `email`: `client.local@example.com`
- `password`: `Password123!`
- `role`: `CLIENT`

### Cuando Usarlo

- demos de prueba tecnica
- validacion funcional despues del despliegue en AWS
- smoke testing en ambientes no productivos

### Por que no para Produccion Real

- los datos productivos deben gestionarse de forma explicita
- no conviene dejar credenciales demo habilitadas en ambientes reales
- produccion debe usar provisioning controlado y manejo seguro de secretos

Valor recomendado en produccion real:

```text
APP_BOOTSTRAP_DEMO_DATA=false
```

## 8. Pruebas de API

### Login

`POST /api/v1/auth/login`

```json
{
  "email": "client.local@example.com",
  "password": "Password123!"
}
```

### Listar Fondos

`GET /api/v1/funds`

### Suscribirse

`POST /api/v1/subscriptions`

```json
{
  "fundId": "DEUDAPRIVADA",
  "notificationPreference": "EMAIL"
}
```

### Ver Portafolio

`GET /api/v1/clients/me/portfolio`

### Ver Transacciones

`GET /api/v1/transactions`

Filtro opcional:

`GET /api/v1/transactions?type=SUBSCRIPTION`

### Cancelar Suscripcion

`DELETE /api/v1/subscriptions/{subscriptionId}`

## 9. Estructura del Proyecto

```text
src/
|-- main/
|   |-- java/com/example/funds/
|   |   |-- domain/
|   |   |-- application/
|   |   `-- infrastructure/
|   `-- resources/
`-- test/
    `-- java/com/example/funds/

infra/
`-- cloudformation/
    |-- investment-funds-api.yml
    `-- investment-funds-api-eb.yml
```

### Responsabilidad por Paquetes

- `domain/model`
  - entidades, value objects y enums

- `domain/port/in`
  - contratos de casos de uso

- `domain/port/out`
  - contratos de persistencia y notificacion

- `application/usecase`
  - implementaciones del flujo de negocio

- `infrastructure/entrypoints/rest`
  - controladores, request DTOs y response DTOs

- `infrastructure/adapters/persistence/dynamodb`
  - adaptadores y mappers DynamoDB

- `infrastructure/security`
  - configuracion JWT, filtro y autenticacion

- `infrastructure/config`
  - bootstrap de aplicacion e inicializacion por entorno

## 10. Mejoras Futuras

- Mover secretos a AWS Secrets Manager o SSM Parameter Store
- Agregar pipeline CI/CD
- Implementar despliegue blue/green
- Agregar monitoreo y alertas
- Reemplazar el adaptador local de notificaciones por SES/SNS
- Usar `TransactWriteItems` en flujos multi-escritura para mayor consistencia
- Agregar pruebas de integracion REST
- Incorporar documentacion OpenAPI

## Notas para Entrevista

Este proyecto esta intencionalmente disenado para equilibrar:

- arquitectura limpia
- reglas de negocio explicitas
- alineacion con AWS
- simplicidad operativa

Es adecuado para una prueba tecnica backend porque demuestra:

- pensamiento orientado al dominio
- separacion clara de responsabilidades
- disciplina de testing
- preparacion para cloud
- seguridad basica sin sobredisenar

Tambien diferencia claramente entre:

- reproducibilidad local con DynamoDB Local
- infraestructura AWS con CloudFormation
- despliegue del runtime de la aplicacion con Elastic Beanstalk
