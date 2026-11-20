-- =====================================================================
-- Taller: Consumo de APIs Publicas en Java con MySQL (MVC)
-- Base de datos: api_paises
-- Ejecutar en MySQL Workbench conectado como root.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS api_paises
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE api_paises;

CREATE TABLE IF NOT EXISTS pais (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    capital    VARCHAR(100),
    region     VARCHAR(100),
    poblacion  BIGINT,
    moneda     VARCHAR(100),
    -- Evita duplicados si se importa varias veces
    CONSTRAINT uq_pais_nombre UNIQUE (nombre)
);

-- Usuario de la aplicacion (convencion del curso: monster / monster)
CREATE USER IF NOT EXISTS 'monster'@'localhost' IDENTIFIED BY 'monster';
GRANT SELECT, INSERT, UPDATE, DELETE ON api_paises.* TO 'monster'@'localhost';
FLUSH PRIVILEGES;

-- ---------------------------------------------------------------------
-- Consultas de la Actividad 5 (las mismas que usa PaisDAO)
-- ---------------------------------------------------------------------
-- Numero de paises por region
-- SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC;
-- Pais mas poblado
-- SELECT nombre, poblacion FROM pais ORDER BY poblacion DESC LIMIT 1;
-- Region con mas paises
-- SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC LIMIT 1;
