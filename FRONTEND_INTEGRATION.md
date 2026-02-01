# 🎯 Guía de Integración para Frontend - Direcciones

## 1. Crear una Dirección

### Desde React/Next.js

```javascript
// Hook personalizado para crear direcciones
export function useDirecciones() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const crearDireccion = async (datosDir, usuarioId) => {
    setLoading(true);
    setError(null);
    
    try {
      const payload = {
        alias: datosDir.alias,
        callePrincipal: datosDir.callePrincipal,
        calleSecundaria: datosDir.calleSecundaria || "",
        ciudad: datosDir.ciudad,
        telefono: datosDir.telefono,
        referencia: datosDir.referencia || "",
        esPrincipal: datosDir.esPrincipal || false,
        usuarioId: usuarioId  // ⭐ IMPORTANTE: Incluir usuarioId aquí
      };

      const response = await fetch('http://localhost:8080/api/direcciones', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Error al crear dirección');
      }

      const nuevaDireccion = await response.json();
      console.log('✅ Dirección creada:', nuevaDireccion);
      return nuevaDireccion;
    } catch (err) {
      setError(err.message);
      console.error('❌ Error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { crearDireccion, loading, error };
}
```

### Usar en un Componente

```javascript
export function CrearDireccionForm({ usuarioId }) {
  const { crearDireccion, loading, error } = useDirecciones();
  const [formData, setFormData] = useState({
    alias: '',
    callePrincipal: '',
    calleSecundaria: '',
    ciudad: '',
    telefono: '',
    referencia: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const newDireccion = await crearDireccion(formData, usuarioId);
      // Limpiar formulario
      setFormData({
        alias: '',
        callePrincipal: '',
        calleSecundaria: '',
        ciudad: '',
        telefono: '',
        referencia: ''
      });
      // Mostrar mensaje de éxito
      alert('✅ Dirección creada exitosamente');
    } catch (err) {
      // El error ya está en el estado `error`
      alert(`❌ ${error}`);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Alias (Casa, Oficina, etc)</label>
        <input
          type="text"
          value={formData.alias}
          onChange={(e) => setFormData({...formData, alias: e.target.value})}
          required
        />
      </div>
      
      <div>
        <label>Calle Principal</label>
        <input
          type="text"
          value={formData.callePrincipal}
          onChange={(e) => setFormData({...formData, callePrincipal: e.target.value})}
          required
        />
      </div>

      <div>
        <label>Calle Secundaria (opcional)</label>
        <input
          type="text"
          value={formData.calleSecundaria}
          onChange={(e) => setFormData({...formData, calleSecundaria: e.target.value})}
        />
      </div>

      <div>
        <label>Ciudad</label>
        <input
          type="text"
          value={formData.ciudad}
          onChange={(e) => setFormData({...formData, ciudad: e.target.value})}
          required
        />
      </div>

      <div>
        <label>Teléfono</label>
        <input
          type="tel"
          value={formData.telefono}
          onChange={(e) => setFormData({...formData, telefono: e.target.value})}
          required
        />
      </div>

      <div>
        <label>Referencia (opcional)</label>
        <input
          type="text"
          value={formData.referencia}
          onChange={(e) => setFormData({...formData, referencia: e.target.value})}
        />
      </div>

      {error && <div style={{color: 'red'}}>❌ {error}</div>}
      
      <button type="submit" disabled={loading}>
        {loading ? 'Creando...' : 'Crear Dirección'}
      </button>
    </form>
  );
}
```

## 2. Obtener Direcciones del Usuario

```javascript
// Hook para obtener direcciones
export function useObtenerDirecciones(usuarioId) {
  const [direcciones, setDirecciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!usuarioId) return;

    const fetchDirecciones = async () => {
      try {
        setLoading(true);
        const response = await fetch(
          `http://localhost:8080/api/direcciones?usuarioId=${usuarioId}`
        );
        
        if (!response.ok) {
          throw new Error('Error al obtener direcciones');
        }

        const data = await response.json();
        setDirecciones(data);
        setError(null);
      } catch (err) {
        setError(err.message);
        console.error('❌ Error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDirecciones();
  }, [usuarioId]);

  return { direcciones, loading, error };
}
```

### Usar en un Componente

```javascript
export function ListaDirecciones({ usuarioId }) {
  const { direcciones, loading, error } = useObtenerDirecciones(usuarioId);

  if (loading) return <div>⏳ Cargando direcciones...</div>;
  if (error) return <div>❌ Error: {error}</div>;

  return (
    <div>
      <h2>Mis Direcciones ({direcciones.length})</h2>
      
      {direcciones.length === 0 ? (
        <p>No tienes direcciones registradas</p>
      ) : (
        <ul>
          {direcciones.map((dir) => (
            <li key={dir.id}>
              <strong>{dir.alias}</strong>
              {dir.esPrincipal && <span>⭐ Principal</span>}
              <p>{dir.callePrincipal}</p>
              <p>{dir.ciudad}</p>
              <p>Tel: {dir.telefono}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

## 3. Manejo de Errores Comunes

### Error: "usuarioId no puede ser nulo"

**Causa:** No se está enviando `usuarioId` en el payload

**Solución:**
```javascript
// ❌ INCORRECTO
const payload = {
  alias: "Casa",
  callePrincipal: "Calle 123"
  // Falta usuarioId
};

// ✅ CORRECTO
const payload = {
  alias: "Casa",
  callePrincipal: "Calle 123",
  usuarioId: usuarioId  // ← Incluir siempre
};
```

### Error: "El campo 'alias' es requerido"

**Causa:** Alguno de los campos obligatorios está vacío

**Campos obligatorios:**
- ✅ `alias` - Nombre de la dirección (Casa, Oficina, etc)
- ✅ `callePrincipal` - Calle principal
- ✅ `ciudad` - Ciudad
- ✅ `telefono` - Teléfono de contacto
- ✅ `usuarioId` - ID del usuario

**Campos opcionales:**
- `calleSecundaria` - Referencia adicional de la calle
- `referencia` - Detalles adicionales (entre qué calles, edificio, etc)
- `esPrincipal` - Se asigna automáticamente (primera = true)

### Error: "Usuario no encontrado con ID: X"

**Causa:** El usuarioId no existe en la base de datos

**Solución:**
```javascript
// Verificar que el usuarioId sea válido
if (!usuarioId || usuarioId <= 0) {
  alert('❌ Usuario no válido');
  return;
}
```

## 4. Validación en Frontend (Recomendado)

```javascript
export function validarDireccion(datos) {
  const errores = [];

  if (!datos.alias || datos.alias.trim() === '') {
    errores.push('El alias es requerido');
  }

  if (!datos.callePrincipal || datos.callePrincipal.trim() === '') {
    errores.push('La calle principal es requerida');
  }

  if (!datos.ciudad || datos.ciudad.trim() === '') {
    errores.push('La ciudad es requerida');
  }

  if (!datos.telefono || datos.telefono.trim() === '') {
    errores.push('El teléfono es requerido');
  }

  if (datos.telefono && datos.telefono.length < 7) {
    errores.push('El teléfono debe tener al menos 7 dígitos');
  }

  return errores;
}

// Usar en el formulario
const handleSubmit = async (e) => {
  e.preventDefault();
  
  const errores = validarDireccion(formData);
  if (errores.length > 0) {
    alert('❌ ' + errores.join('\n'));
    return;
  }

  try {
    await crearDireccion(formData, usuarioId);
  } catch (err) {
    alert(`❌ ${err.message}`);
  }
};
```

## 5. Integración con Envíos (Siguiente Paso)

Cuando un usuario quiera crear un envío, puede seleccionar una dirección:

```javascript
export function CrearEnvioForm({ usuarioId }) {
  const { direcciones } = useObtenerDirecciones(usuarioId);
  const [formData, setFormData] = useState({
    numeroTracking: '',
    descripcion: '',
    pesoLibras: '',
    valorDeclarado: '',
    direccionId: ''  // Seleccionar de la lista
  });

  return (
    <form>
      {/* ... otros campos ... */}
      
      <div>
        <label>Dirección de envío</label>
        <select value={formData.direccionId} required>
          <option value="">-- Seleccionar dirección --</option>
          {direcciones.map(dir => (
            <option key={dir.id} value={dir.id}>
              {dir.alias} - {dir.callePrincipal}, {dir.ciudad}
            </option>
          ))}
        </select>
      </div>

      {/* ... resto del formulario ... */}
    </form>
  );
}
```

## 6. URLs en Diferentes Ambientes

### Desarrollo
```
http://localhost:8080/api/direcciones
```

### Producción (Railway)
```
https://backend-tesis-spring.onrender.com/api/direcciones
```

**Usar una variable de entorno:**

```javascript
// .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080  # Desarrollo
# NEXT_PUBLIC_API_URL=https://backend-tesis-spring.onrender.com  # Producción

// En el código
const API_URL = process.env.NEXT_PUBLIC_API_URL;

const response = await fetch(`${API_URL}/api/direcciones?usuarioId=${usuarioId}`);
```

---

## Checklist de Implementación

- [ ] Importar hooks en el componente
- [ ] Agregar field `usuarioId` al payload
- [ ] Validar campos en frontend
- [ ] Mostrar errores al usuario
- [ ] Recargar lista de direcciones después de crear
- [ ] Probar en localhost:8080
- [ ] Probar en Railway (producción)

---

**Última actualización:** 2026-02-01
