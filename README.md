# TI.2.1 — Consumo de APIs Públicas en Java con MySQL (MVC)

Aplicación de escritorio en **Java Swing** que consume la API pública **REST Countries**, convierte el JSON a objetos Java con **Gson** y guarda los países en **MySQL** mediante un **DAO**. Está organizada con el patrón **MVC** y aplica **Singleton**.

**Universidad de las Fuerzas Armadas ESPE** · Arquitectura de Software · NRC 36884 · GR01
**Autor:** Masapanta Guilcatoma Jefferson Santiago
**Tutor:** Ing. Eduardo Mauricio Campaña Ortega

---

## Arquitectura

```
+-----------------+      +--------------------+      +-------------------------------+
|      VISTA      | ---> |    CONTROLADOR     | ---> |  SERVICIO (API)  /  DAO (SQL) |
|     FrmPais     | <--- |   PaisController   | <--- |  PaisService       PaisDAO    |
|     (Swing)     |      | (Singleton, valida)|      |  ConsumoAPI        Conexion   |
+-----------------+      +--------------------+      +-------------------------------+
                                                         |                    |
                                              REST Countries (JSON)       MySQL api_paises
```

| Patrón | Dónde | Para qué |
|---|---|---|
| MVC | `vista`, `controlador`, `modelo` | Separar presentación, coordinación y datos |
| DAO | `PaisDAO` | Todo el SQL en un solo lugar, siempre con `PreparedStatement` |
| Singleton | `Configuracion`, `Conexion`, `PaisController` | Una sola instancia, con constructor privado y `getInstancia()` |
| Fallback | `PaisServiceResiliente` | Si la API principal falla, usa la fuente de respaldo |
| Inyección de dependencias | Constructor de `PaisController` | Recibe `PaisService` y `PaisDAO`, así se puede probar con mocks |

## Estructura

```
01. BDD/api_paises.sql              Script: base api_paises, tabla pais y usuario monster
02. APP/                            Proyecto Maven (se abre en NetBeans)
    pom.xml
    config.properties               Conexión MySQL y API key (se cambian sin recompilar)
    src/main/java/ec/edu/monster/
        Main.java
        modelo/        Pais (entidad), Configuracion (Singleton)
        dao/           Conexion (Singleton), PaisDAO
        servicio/      PaisService (interfaz), RestCountriesV5Service,
                       RestCountriesDatasetService, PaisServiceResiliente,
                       ConsumoAPI (HTTP GET), Json
        controlador/   PaisController (Singleton, validaciones)
        vista/         FrmPais (JFrame)
03. INFORME/                        Informe del taller (Word) y capturas
```

## Requisitos

- JDK 17 o superior
- Apache NetBeans 21+ y Maven
- MySQL Server 8+ (probado con 9.5) y MySQL Workbench
- Dependencias, que Maven descarga solo: Gson 2.10.1 y mysql-connector-j 8.3.0

## Cómo ejecutar

1. **Iniciar MySQL.** Como administrador, ejecutar `net start MySQL95`, o abrir *Servicios* de Windows → MySQL95 → Iniciar.
2. **Crear la base.** En MySQL Workbench, conectado como `root`, ejecutar `01. BDD/api_paises.sql`. Crea la base `api_paises`, la tabla `pais` y el usuario `monster` con clave `monster`.
3. **Abrir el proyecto.** En NetBeans: *File → Open Project →* carpeta `02. APP` → *Run* (F6).
   También se puede por consola, desde `02. APP`:
   ```bash
   mvn package
   java -jar target/ApiPaises-1.0-jar-with-dependencies.jar
   ```

### Configuración (`02. APP/config.properties`)

```properties
db.url=jdbc:mysql://localhost:3306/api_paises?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=monster
db.password=monster
api.key=
```

Para usar **MySQL en la nube (AWS RDS)** basta con cambiar `db.url` por el endpoint de RDS. No hay que tocar el código.

> Si pones tu API key en `api.key`, no subas ese cambio al repositorio.

## Nota importante sobre la API

El endpoint de la guía (`https://restcountries.com/v3.1/all`) **fue retirado**. Ahora responde que la versión está obsoleta y pide migrar a la **v5**.

| Aspecto | Guía (v3.1) | Esta aplicación |
|---|---|---|
| Endpoint | `restcountries.com/v3.1/all` | `api.restcountries.com/countries/v5` (páginas de 100) |
| Autenticación | Ninguna | API key gratuita (`Authorization: Bearer`), 1000 solicitudes al mes |
| Buscar por nombre | `/v3.1/name/ecuador` | `/countries/v5/name?q=ecuador` |
| Campos | `name.common`, `capital[0]` | `names.common`, `capitals[0].name` |

**La aplicación funciona aunque no tengas API key.** En ese caso usa como respaldo el dataset público del proyecto REST Countries en GitHub, con los mismos 250 países (patrón Fallback). La key gratuita se crea en https://restcountries.com/sign-up.

## Actividades del taller

| Actividad | Dónde |
|---|---|
| Botón **IMPORTAR PAÍSES** (API → JSON → objetos → MySQL → mensaje) | Barra superior. Reimportar no duplica gracias a `UNIQUE` y `ON DUPLICATE KEY UPDATE` |
| 1. Consulta de un país por nombre | Pestaña *Consultar API por nombre* (incluye botón para guardarlo en MySQL) |
| 2. CRUD completo | Pestaña *Países en MySQL*: Nuevo / Guardar / Actualizar / Eliminar (clic en una fila para editar) |
| 3. Países en un JTable | Misma pestaña; la tabla se ordena por columna |
| 4. Búsqueda por región | Combo *Región*: América, Europa, Asia, África, Oceanía. También busca por nombre |
| 5. Reportes estadísticos | Pestaña *Reportes*: países por región, país más poblado y región con más países, calculados con SQL `GROUP BY` |

## Capturas

**Importación finalizada (250 países)**
![Importación](03.%20INFORME/capturas/f03_importacion_finalizada.png)

**Actividad 1: consulta por nombre en la API**
![Consulta API](03.%20INFORME/capturas/f04_consulta_api.png)

**Actividades 2 y 3: CRUD y JTable**
![CRUD](03.%20INFORME/capturas/f06_crud_editar.png)

**Actividad 4: filtro por región (Europa)**
![Región](03.%20INFORME/capturas/f07_region_europa.png)

**Actividad 5: reportes**
![Reportes](03.%20INFORME/capturas/f08_reportes.png)

**Verificación en MySQL**
![MySQL](03.%20INFORME/capturas/f09_mysql_consola.png)

### Resultados de los reportes

| Reporte | Resultado |
|---|---|
| Países por región | África 60, América 57, Europa 53, Asia 50, Oceanía 27, Polar 3 (total 250) |
| País más poblado | China, 1.402.112.000 habitantes |
| Región con más países | África (60) |

## Informe

El informe completo, con desarrollo, capturas y las preguntas de análisis (MVC, DAO, JSON vs XML, disponibilidad de la API, caché, Docker/Kubernetes, SOLID y microservicios), está en [`03. INFORME`](03.%20INFORME/).
