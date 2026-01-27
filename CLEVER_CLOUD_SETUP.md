# CÓMO HACER FUNCIONAR CLEVER CLOUD

## 🔴 PROBLEMA IDENTIFICADO

En `application.properties` estaba configurado:
```properties
server.port=3306  ❌ INCORRECTO (Puerto de MySQL)
```

Debería ser:
```properties
server.port=8080  ✅ CORRECTO (Puerto de la aplicación)
```

---

## ✅ CAMBIOS APLICADOS

### 1. **Puerto del servidor corregido**
```properties
server.port=8080  ← Era 3306, ahora es 8080
```

### 2. **URL de conexión mejorada**
Agregadas opciones para mejor compatibilidad:
```properties
spring.datasource.url=jdbc:mysql://...?
  useSSL=true                      ← Conexión segura (IMPORTANTE para Clever Cloud)
  &serverTimezone=UTC              ← Zona horaria
  &allowPublicKeyRetrieval=true    ← Permite autenticación RSA
  &useUnicode=true                 ← Soporte UTF-8
  &characterEncoding=UTF-8         ← Encoding
```

### 3. **Configuración de Pool de Conexiones (HikariCP)**
```properties
spring.datasource.hikari.maximum-pool-size=5      ← Max conexiones
spring.datasource.hikari.minimum-idle=2           ← Min conexiones inactivas
spring.datasource.hikari.connection-timeout=20000 ← Timeout 20s (importante!)
spring.datasource.hikari.idle-timeout=300000      ← Inactivo 5 min
spring.datasource.hikari.max-lifetime=1200000     ← Vida máxima 20 min
```

### 4. **Optimización de Hibernate**
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=10
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

---

## 🚀 PRÓXIMO PASO: EJECUTAR

Intenta ejecutar ahora:

```bash
cd d:\courrer_backend\backend
.\mvnw.cmd spring-boot:run
```

---

## 🔍 SI SIGUE FALLANDO, VERIFICA:

### **1. ¿Clever Cloud está disponible?**

Abre PowerShell y ejecuta:
```powershell
Test-NetConnection -ComputerName bpvjetbcnrfligd5cfor-mysql.services.clever-cloud.com -Port 3306 -InformationLevel Detailed
```

Debería mostrar: `TcpTestSucceeded : True`

### **2. ¿Las credenciales en Clever Cloud?**

Ve a https://console.clever-cloud.com y verifica:
- Base de datos: `bpvjetbcnrfligd5cfor`
- Usuario: `u2xziqtytlardi7k`
- Contraseña: `CsN5UdYy442WmvFoexPJ`

### **3. ¿Tienes conexión a internet?**

```powershell
Test-NetConnection -ComputerName google.com -Port 443
```

Debería mostrar: `TcpTestSucceeded : True`

---

## ⚠️ POSIBLES ERRORES Y SOLUCIONES

| Error | Causa | Solución |
|-------|-------|----------|
| `UnknownHostException` | DNS no resuelve Clever Cloud | ✅ Ya arreglado con timeouts |
| `Access denied for user` | Credenciales incorrectas | Verifica credenciales en Clever Cloud |
| `Communications link failure` | Firewall bloqueando | Verifica si puerto 3306 está abierto |
| `Can't create pool of type class com.zaxxer.hikari.HikariPool` | Pool de conexiones no se puede crear | ✅ Ya optimizado |
| `Can't connect to MySQL server` | MySQL servidor no responde | Verifica en consola de Clever Cloud |

---

## 📝 CONFIGURACIÓN FINAL

Tu `application.properties` ahora tiene:

```properties
# === CONFIGURACIÓN DEL SERVIDOR ===
spring.application.name=CurrierBackend
server.port=8080

# === CONEXIÓN BASE DE DATOS CLEVER CLOUD (Tus Credenciales) ===
spring.datasource.url=jdbc:mysql://bpvjetbcnrfligd5cfor-mysql.services.clever-cloud.com:3306/bpvjetbcnrfligd5cfor?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=u2xziqtytlardi7k
spring.datasource.password=CsN5UdYy442WmvFoexPJ
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# === CONFIGURACIÓN DE POOL DE CONEXIONES ===
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000

# === CONFIGURACIÓN DE JPA (Para crear tablas automático) ===
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.jdbc.batch_size=10
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.use_scrollable_resultset=true
```

---

## ✅ SI FUNCIONA

Deberías ver en los logs:
```
2026-01-24... INFO : HikariPool-1 - Starting...
2026-01-24... INFO : HikariPool-1 - Start completed
2026-01-24... INFO : Started BackendApplication in ... seconds
✅ DATOS DE PRUEBA CARGADOS EXITOSAMENTE
```

Y la aplicación disponible en: `http://localhost:8080/api/usuarios`

