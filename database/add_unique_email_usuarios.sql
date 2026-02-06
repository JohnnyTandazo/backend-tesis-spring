-- 1) Detectar duplicados por email
SELECT email, COUNT(*) AS total
FROM usuarios
GROUP BY email
HAVING COUNT(*) > 1;

-- 2) Limpiar duplicados (conserva el usuario con el ID más bajo por email)
--    Ajusta esta lógica si necesitas fusionar datos antes de eliminar.
DELETE u
FROM usuarios u
JOIN (
    SELECT email, MIN(id) AS keep_id
    FROM usuarios
    GROUP BY email
    HAVING COUNT(*) > 1
) k ON u.email = k.email
WHERE u.id <> k.keep_id;

-- 3) Agregar restricción UNIQUE
ALTER TABLE usuarios
ADD CONSTRAINT uq_usuarios_email UNIQUE (email);
