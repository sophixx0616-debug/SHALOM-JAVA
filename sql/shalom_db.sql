-- =========================================================
--  SHALOM BEAUTY SALON - Base de datos MySQL (XAMPP)
--  Este script SOLO crea la base de datos. Las tablas y los
--  datos semilla los genera la aplicacion automaticamente:
--    - ddl-auto=update  (Hibernate crea/actualiza las tablas)
--    - DataInitializer  (siembra roles, admin, servicios,
--                         especialistas, inventario, etc.)
-- =========================================================
--  Requiere un usuario con permisos: ej. root (XAMPP)
-- -----------------------------------------------

CREATE DATABASE IF NOT EXISTS shalom_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE shalom_db;