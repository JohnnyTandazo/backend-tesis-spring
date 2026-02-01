# Script para probar el endpoint POST /api/direcciones

# Variables
$baseUrl = "http://localhost:8080"
$usuarioId = 1

# 1. Crear una dirección con usuarioId en el body
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRUEBA 1: Crear dirección con usuarioId en BODY" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$payload1 = @{
    alias = "Casa Principal"
    callePrincipal = "Calle 10 # 25-50"
    calleSecundaria = "Entre carreras 5 y 6"
    ciudad = "Bogotá"
    telefono = "601-1234567"
    referencia = "Cerca al parque"
    esPrincipal = $true
    usuarioId = $usuarioId
} | ConvertTo-Json

Write-Host "Enviando: $payload1" -ForegroundColor Green
try {
    $response1 = Invoke-WebRequest -Uri "$baseUrl/api/direcciones" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $payload1
    Write-Host "✅ Éxito (HTTP $($response1.StatusCode))" -ForegroundColor Green
    Write-Host "Respuesta: $($response1.Content)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host ""

# 2. Crear dirección con usuarioId en URL params
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRUEBA 2: Crear dirección con usuarioId en QUERY PARAM" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

$payload2 = @{
    alias = "Oficina"
    callePrincipal = "Carrera 7 # 32-10"
    calleSecundaria = ""
    ciudad = "Medellín"
    telefono = "604-5678901"
    referencia = "Edificio administrativo"
} | ConvertTo-Json

Write-Host "Enviando a: $baseUrl/api/direcciones?usuarioId=$usuarioId" -ForegroundColor Green
Write-Host "Payload: $payload2" -ForegroundColor Green
try {
    $response2 = Invoke-WebRequest -Uri "$baseUrl/api/direcciones?usuarioId=$usuarioId" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $payload2
    Write-Host "✅ Éxito (HTTP $($response2.StatusCode))" -ForegroundColor Green
    Write-Host "Respuesta: $($response2.Content)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 3. Obtener direcciones del usuario
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRUEBA 3: Obtener direcciones del usuario" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "GET $baseUrl/api/direcciones?usuarioId=$usuarioId" -ForegroundColor Green
try {
    $response3 = Invoke-WebRequest -Uri "$baseUrl/api/direcciones?usuarioId=$usuarioId" `
        -Method GET `
        -Headers @{"Content-Type"="application/json"}
    Write-Host "✅ Éxito (HTTP $($response3.StatusCode))" -ForegroundColor Green
    Write-Host "Respuesta:" -ForegroundColor Green
    $response3.Content | ConvertFrom-Json | Format-Table -AutoSize
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 4. Obtener direcciones del usuario por endpoint alternativo
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRUEBA 4: Obtener direcciones del usuario (endpoint usuarios)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "GET $baseUrl/api/usuarios/$usuarioId/direcciones" -ForegroundColor Green
try {
    $response4 = Invoke-WebRequest -Uri "$baseUrl/api/usuarios/$usuarioId/direcciones" `
        -Method GET `
        -Headers @{"Content-Type"="application/json"}
    Write-Host "✅ Éxito (HTTP $($response4.StatusCode))" -ForegroundColor Green
    Write-Host "Respuesta:" -ForegroundColor Green
    $response4.Content | ConvertFrom-Json | Format-Table -AutoSize
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRUEBAS COMPLETADAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
