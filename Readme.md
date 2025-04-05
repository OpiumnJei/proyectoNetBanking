# 🏦 NetBanking REST API - Documentación Técnica

API REST para sistema de bancario en línea con autenticación JWT, seguridad por roles (Admin/Cliente), y gestión completa de productos financieros.

## 🌟 Características Principales
- **Autenticación JWT** con Spring Security
- **Roles de usuario**: Administrador y Cliente
- **Gestión de productos financieros**:
    - Cuentas de ahorro (principal/secundarias)
    - Tarjetas de crédito (con límites y avances)
    - Préstamos
- **Operaciones bancarias**:
    - Transferencias entre cuentas
    - Pagos (expresos, a beneficiarios, tarjetas, préstamos)
    - Avances de efectivo (con interés del 6.25%)
- **Indicadores administrativos** (dashboard)
- **Validaciones financieras** (saldo suficiente, límites, etc.)

---

## 🔧 Stack Tecnológico
| **Categoría**     | **Tecnologías**                                                   |     |
| ----------------- | ----------------------------------------------------------------- | --- |
| **Backend**       | Java 17, Spring Boot 3.x, Spring Security, JWT, Hibernate, Flyway |     |
| **Base de Datos** | MySQL/PostgreSQL (según configuración)                            |     |
| **Validación**    | Bean Validation (`@Valid`), DTOs personalizados                   |     |
| **Seguridad**     | Roles (`@PreAuthorize`), Protección CSRF, CORS configurado        |     |
| **Documentación** | Swagger/OpenAPI (integrado)                                       |     |

---

## 📚 Estructura de Endpoints

### 1. **Autenticación**
- **`POST /netbanking/usuarios/login`**  
  Autentica usuarios y devuelve token JWT.
  ```json 
  // Request
  {
    "cedula": "string", 
    "password": "string"
  }
  // Response
  {
    "token": "jwt.token.here",
    "tipoUsuario": "ADMIN|CLIENTE"
  }
  ```

---

### 2. **Administrador** (Prefijo: `/netbanking/admin`)
#### **Gestión de Usuarios**
| Método | Endpoint                          | Descripción                                  |  
|--------|----------------------------------|--------------------------------------------|  
| POST   | `/nuevo-usuario`                | Crea usuarios (clientes/administradores)   |  
| GET    | `/listar`                       | Lista usuarios paginados                   |  
| PUT    | `/activar-usuario/{usuarioId}`  | Activa/inactiva usuarios                   |  
| PUT    | `/actualizar/clientes/{id}`     | Actualiza datos de cliente                 |  

#### **Gestión de Productos**
| Método | Endpoint                          | Descripción                                  |  
|--------|----------------------------------|--------------------------------------------|  
| POST   | `/crear-cuenta-ahorro`          | Crea cuenta de ahorro secundaria           |  
| POST   | `/crear-tarjeta-credito/{id}`   | Asigna tarjeta de crédito a cliente        |  
| POST   | `/crear-prestamo/{id}`          | Asigna préstamo a cliente                  |  

#### **Dashboard** (Prefijo: `/netbanking/admin/indicadores`)
- **`GET /transacciones`**: Estadísticas de transacciones.
- **`GET /pagos`**: Resumen de pagos realizados.
- **`GET /clientes`**: Cantidad de clientes activos/inactivos.

---

### 3. **Cliente** (Prefijo: `/netbanking/cliente`)
#### **Productos**
- **`GET /productos/{clienteId}/lista-productos`**  
  Lista cuentas, tarjetas y préstamos del cliente.

#### **Transferencias**
- **`POST /realizar-transferencia`**
  ```json 
  {
    "cuentaOrigenId": 1,
    "cuentaDestinoId": 2,
    "monto": 500.00
  }
  ```

#### **Pagos**
| Tipo                | Endpoint                                      | Body Example                               |  
|---------------------|---------------------------------------------|------------------------------------------|  
| **Expreso**         | `POST /pagos/realizar-pago-expreso`         | Número cuenta destino + monto            |  
| **Tarjeta**         | `POST /pagos/realizar-pago-tarjeta/{id}`    | Cuenta origen + monto                    |  
| **Préstamo**        | `POST /pagos/realizar-pago-prestamo/{id}`   | Cuenta origen + monto                    |  
| **Beneficiario**    | `POST /pagos/realizar-pago-beneficiario/{id}` | Cuenta origen + monto (validación automática) |  

#### **Beneficiarios**
- **`POST /beneficiarios/agregar-beneficiario/{usuarioId}`**: Vincula cuentas externas.
- **`DELETE /beneficiarios/eliminar-beneficiario/{id}`**: Elimina beneficiarios.

#### **Avances de Efectivo**
- **`POST /realizar-avance-efectivo`**  
  Aplica interés del 6.25% sobre el monto.

---

## 🚀 Configuración

### 1. **Variables de Entorno (application.properties)**
Crear/actualizar el archivo `src/main/resources/application.properties` con:

```properties
# Configuración de Base de Datos (MySQL/PostgreSQL)
spring.datasource.url=jdbc:mysql://localhost:3306/netbanking_db
spring.datasource.username=admin
spring.datasource.password=password

# Configuración de JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Configuración de JWT (¡Cambiar en producción!)
jwt.secret=miClaveSecretaJwt

# Flyway (opcional, si se usa)
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### 2. **Ejecución**
```bash 
# Con Maven
mvn spring-boot:run

# O con Gradle
./gradlew bootRun
```


---

## 📊 Ejemplo de Flujo (Cliente)
1. **Login** → Obtener token JWT.
2. **Listar productos** → Ver cuentas/tarjetas.
3. **Transferencia** → Mover dinero entre cuentas.
4. **Pago a beneficiario** → Usar cuenta vinculada.

---

**Créditos**  

_Jerlinson G. Seberino, OpiumJei._