# 🔧 Configuración CORS - Backend Spring Boot

## ✅ Cambios Realizados

### 1. **Configuración Global de CORS** (NUEVO)
**Archivo:** `WebConfig.java`

Se creó una clase de configuración global que permite:
- ✅ Todos los métodos HTTP: GET, POST, PUT, DELETE, OPTIONS
- ✅ Todos los orígenes (temporalmente `*` para debugging)
- ✅ Todos los headers
- ✅ Timeout de 3600 segundos

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")  // Cambiar a tu dominio de Vercel cuando esté listo
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
```

### 2. **Controladores Actualizados**

#### `PaqueteController.java`
- ✅ `@CrossOrigin(origins = "*")` habilitado
- ✅ Nuevo endpoint: `GET /api/paquetes/track/{codigo}` (equivalente a `/rastreo/{tracking}`)
- ✅ Logs agregados en todos los métodos:
  - `listarPaquetes()` - logs de listado
  - `crearPaquete()` - logs de creación
  - `buscarPorTracking()` - logs de búsqueda
  - **NUEVO** `buscarPorCodigo()` - logs de búsqueda por código alternativo
  - `actualizarDetallesPaquete()` - logs de actualización

#### `UsuarioController.java`
- ✅ `@CrossOrigin(origins = "*")` confirmado
- ✅ Logs agregados en todos los métodos:
  - `listarUsuarios()` - logs de listado
  - `guardarUsuario()` - logs de guardado
  - `login()` - logs de login (con validaciones)
  - `registro()` - logs de registro

## 🔍 Logs de Debugging

Los logs ahora mostrarán en la consola de Railway/en tu terminal:

```
✅ CORS configurado globalmente para todos los endpoints /api/**
📦 [GET /api/paquetes] Listando todos los paquetes...
📝 [POST /api/paquetes] ✅ PETICIÓN RECIBIDA - Creando nuevo paquete...
🔍 [GET /api/paquetes/track/ABC123] ✅ PETICIÓN RECIBIDA - Buscando paquete por código: ABC123
🔐 [POST /api/usuarios/login] ✅ PETICIÓN RECIBIDA - Intentando login con: user@example.com
📝 [POST /api/usuarios/registro] ✅ PETICIÓN RECIBIDA - Registrando nuevo usuario: newuser@example.com
```

## 📍 Endpoints Disponibles

### Paquetes
- `GET /api/paquetes` - Listar todos
- `POST /api/paquetes` - Crear nuevo
- `GET /api/paquetes/track/{codigo}` - **NUEVO** - Buscar por código
- `GET /api/paquetes/rastreo/{tracking}` - Buscar por tracking (antiguo)
- `PUT /api/paquetes/{id}/detalles` - Actualizar

### Usuarios
- `GET /api/usuarios` - Listar todos
- `POST /api/usuarios` - Crear nuevo
- `POST /api/usuarios/login` - Login
- `POST /api/usuarios/registro` - Registrar nuevo

## 🚀 Próximos Pasos

### En desarrollo/testing (AHORA):
1. ✅ CORS permitiendo `*` (todos los orígenes)
2. ✅ Logs detallados en consola
3. ✅ Verificar que las peticiones desde Vercel llegan correctamente

### En producción (DESPUÉS):
1. **Cambiar `origins = "*"` a tu dominio específico de Vercel:**

**Editar `WebConfig.java`:**
```java
.allowedOrigins("https://v0-currier-tics-layout.vercel.app")
```

**O en los controladores:**
```java
@CrossOrigin(origins = "https://v0-currier-tics-layout.vercel.app")
```

## 🔗 Configuración en tu Frontend (React/Next.js)

Asegúrate de usar en tu `.env.local`:

```
NEXT_PUBLIC_API_URL=https://tu-backend-en-railway.com
```

Y en tu componente:

```javascript
const response = await fetch(
  `${process.env.NEXT_PUBLIC_API_URL}/api/paquetes/track/${codigo}`
);
```

## ✨ ¿Qué cambió?

| Recurso | Antes | Después |
|---------|-------|---------|
| Configuración CORS | Solo anotaciones | Global + Anotaciones |
| Logs | Ninguno | Detallados en cada endpoint |
| Endpoint de búsqueda | Solo `/rastreo/{tracking}` | Ambos: `/rastreo/{tracking}` y `/track/{codigo}` |
| Orígenes permitidos | "https://v0-currier-tics-layout.vercel.app" | "*" (debugging) |

---

**📝 Nota:** Recuerda cambiar `origins = "*"` a tu dominio específico una vez que todo funcione.
