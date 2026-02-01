import requests
import json
import time
import sys

BASE_URL = "http://localhost:8080"
USUARIO_ID = 1

def print_header(title):
    print("\n" + "="*60)
    print(title.center(60))
    print("="*60)

def test_create_direccion_with_body():
    """Test creating direccion with usuarioId in request body"""
    print_header("PRUEBA 1: Crear Dirección con usuarioId en BODY")
    
    payload = {
        "alias": "Casa Principal",
        "callePrincipal": "Calle 10 # 25-50",
        "calleSecundaria": "Entre carreras 5 y 6",
        "ciudad": "Bogotá",
        "telefono": "601-1234567",
        "referencia": "Cerca al parque",
        "esPrincipal": True,
        "usuarioId": USUARIO_ID
    }
    
    print(f"Endpoint: POST {BASE_URL}/api/direcciones")
    print(f"Payload:\n{json.dumps(payload, indent=2, ensure_ascii=False)}")
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/direcciones",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=5
        )
        print(f"\n✅ HTTP {response.status_code}")
        print(f"Respuesta:\n{json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        return response.status_code == 201
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def test_create_direccion_with_params():
    """Test creating direccion with usuarioId in URL params"""
    print_header("PRUEBA 2: Crear Dirección con usuarioId en QUERY PARAM")
    
    payload = {
        "alias": "Oficina",
        "callePrincipal": "Carrera 7 # 32-10",
        "calleSecundaria": "",
        "ciudad": "Medellín",
        "telefono": "604-5678901",
        "referencia": "Edificio administrativo"
    }
    
    url = f"{BASE_URL}/api/direcciones?usuarioId={USUARIO_ID}"
    print(f"Endpoint: POST {url}")
    print(f"Payload:\n{json.dumps(payload, indent=2, ensure_ascii=False)}")
    
    try:
        response = requests.post(
            url,
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=5
        )
        print(f"\n✅ HTTP {response.status_code}")
        print(f"Respuesta:\n{json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        return response.status_code == 201
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def test_get_direcciones():
    """Test getting user's addresses"""
    print_header("PRUEBA 3: Obtener Direcciones del Usuario")
    
    url = f"{BASE_URL}/api/direcciones?usuarioId={USUARIO_ID}"
    print(f"Endpoint: GET {url}")
    
    try:
        response = requests.get(
            url,
            headers={"Content-Type": "application/json"},
            timeout=5
        )
        print(f"\n✅ HTTP {response.status_code}")
        data = response.json()
        print(f"Total de direcciones: {len(data)}")
        for i, d in enumerate(data, 1):
            print(f"\n  {i}. {d.get('alias', 'N/A')}")
            print(f"     Dirección: {d.get('callePrincipal', 'N/A')}")
            print(f"     Ciudad: {d.get('ciudad', 'N/A')}")
            print(f"     Principal: {'Sí' if d.get('esPrincipal') else 'No'}")
        return response.status_code == 200 and len(data) > 0
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def test_get_direcciones_from_usuarios():
    """Test getting user's addresses from usuarios endpoint"""
    print_header("PRUEBA 4: Obtener Direcciones del Usuario (endpoint /usuarios)")
    
    url = f"{BASE_URL}/api/usuarios/{USUARIO_ID}/direcciones"
    print(f"Endpoint: GET {url}")
    
    try:
        response = requests.get(
            url,
            headers={"Content-Type": "application/json"},
            timeout=5
        )
        print(f"\n✅ HTTP {response.status_code}")
        data = response.json()
        print(f"Total de direcciones: {len(data)}")
        for i, d in enumerate(data, 1):
            print(f"\n  {i}. {d.get('alias', 'N/A')}")
            print(f"     Dirección: {d.get('callePrincipal', 'N/A')}")
            print(f"     Ciudad: {d.get('ciudad', 'N/A')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return False

def main():
    print("\n" + "🚀 INICIANDO PRUEBAS DE DIRECCIONES".center(60))
    print("Esperando a que el servidor esté listo...")
    
    # Try to connect
    for i in range(10):
        try:
            requests.get(f"{BASE_URL}/api/usuarios/1", timeout=1)
            print("✅ Servidor conectado!")
            break
        except:
            if i < 9:
                print(f"   Intento {i+1}/10... esperando...")
                time.sleep(1)
            else:
                print("❌ No se pudo conectar al servidor")
                sys.exit(1)
    
    results = []
    results.append(("Crear con body", test_create_direccion_with_body()))
    results.append(("Crear con params", test_create_direccion_with_params()))
    results.append(("Obtener direcciones", test_get_direcciones()))
    results.append(("Obtener del endpoint usuarios", test_get_direcciones_from_usuarios()))
    
    # Summary
    print_header("RESUMEN DE PRUEBAS")
    for nombre, resultado in results:
        estado = "✅ PASÓ" if resultado else "❌ FALLÓ"
        print(f"{nombre:.<40} {estado}")
    
    total = len(results)
    pasaron = sum(1 for _, r in results if r)
    print(f"\nResultado final: {pasaron}/{total} pruebas pasaron")

if __name__ == "__main__":
    main()
