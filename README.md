# 🟦 Seidor Tokens & Users API

API REST desarrollada con **Spring Boot 3.5.7** y **Java 17** para gestionar:

- 🔑 **Tokens temporales de validación** (`USERS_TOKEN`)
- 👥 **Suscripciones de usuarios a newsletters** (`USER_SUBSCRIPTION`)

Compatible con **Azure SQL Database** o **SQL Server local**.  
Incluye documentación Swagger integrada.

---

## ⚙️ Tecnologías

- Java 17  
- Spring Boot 3.5.7  
- Spring Web  
- Spring Data JPA  
- SQL Server JDBC Driver  
- Lombok  
- Jakarta Validation (`@Valid`, `@NotBlank`)  
- Springdoc OpenAPI (Swagger UI)

---

## 🧱 Estructura de tablas (SQL Server / Azure SQL)

### 🧩 Tabla `USERS_TOKEN`
Guarda los tokens generados para validaciones o enlaces temporales.

```sql
CREATE TABLE USERS_TOKEN (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    mail_base64 NVARCHAR(512) NOT NULL,
    token NVARCHAR(256) NOT NULL UNIQUE,
    date_sent DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    category NVARCHAR(100) NOT NULL,
    subcategory NVARCHAR(100) NOT NULL
);
```

📌 **Notas**
- `mail_base64`: correo codificado en Base64  
- `token`: UUID generado automáticamente  
- `category` / `subcategory`: agrupan tipos de token (ej. `"user-validation"`, `"register"`)  
- `date_sent`: se actualiza automáticamente con `@PrePersist` / `@PreUpdate`

---

### 🧩 Tabla `USER_SUBSCRIPTION`
Guarda las suscripciones de usuarios a categorías de newsletters o campañas.

```sql
CREATE TABLE USER_SUBSCRIPTION (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    mail_base64 NVARCHAR(512) NOT NULL,
    category NVARCHAR(100) NOT NULL,
    subcategory NVARCHAR(100) NOT NULL,
    date_subscribed DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
```

📌 **Notas**
- Permite múltiples suscripciones por usuario (una por `category` + `subcategory`)
- `date_subscribed`: se autocompleta en inserción

---

## 🚀 Ejecución local

### 🖥️ Requisitos previos
- JDK 17+
- Maven 3.9+
- SQL Server (local o Azure SQL Database)
- Puerto 8080 libre

---

### 🧩 Crear base de datos (si no existe)
Ejecuta en SQL Server Management Studio o Azure Data Studio:

```sql
CREATE DATABASE tokensdb;
```

Luego, ejecuta los comandos de creación de tablas anteriores en esa base.

---

### ▶️ Ejecutar el proyecto

Desde la raíz del proyecto:

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:sqlserver://0.0.0.0:1433;databaseName=tokensdb;encrypt=false --spring.datasource.username=sa --spring.datasource.password=db_com --spring.jpa.hibernate.ddl-auto=update"
```

📦 Esto levantará el servidor en:

```
http://localhost:8080
```

---

## 📘 Swagger UI

Una vez iniciado, accede a la documentación interactiva:

👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🔗 Endpoints principales

### 🔑 Tokens (`/api/tokens`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/tokens` | Crear token nuevo |
| `GET` | `/api/tokens` | Buscar tokens por email, categoría y subcategoría |
| `POST` | `/api/tokens/refresh` | Refrescar token si han pasado más de 48h |
| `GET` | `/api/tokens/{token}` | Obtener token por valor |
| `DELETE` | `/api/tokens/{token}` | Eliminar token |
| `GET` | `/api/tokens/mails` | Obtener mails+tokens por categoría/subcategoría |

---

### 👥 Users (`/api/users`)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/users` | Crear suscripción de usuario |
| `GET` | `/api/users` | Listar todas las suscripciones |
| `GET` | `/api/users/mails` | Listar correos base64 por categoría/subcategoría |
| `DELETE` | `/api/users/{id}` | Eliminar suscripción |

---

## 📬 Ejemplo de uso (POST /api/users)

**Request**
```json
{
  "mail": "dgayala002@gmail.com",
  "category": "promos",
  "subcategory": "black-friday"
}
```

**Response**
```json
{
  "id": 1,
  "mailBase64": "ZGdheWFsYTAwMkBnbWFpbC5jb20=",
  "category": "promos",
  "subcategory": "black-friday",
  "dateSubscribed": "2025-11-01T18:45:12.221"
}
```

---