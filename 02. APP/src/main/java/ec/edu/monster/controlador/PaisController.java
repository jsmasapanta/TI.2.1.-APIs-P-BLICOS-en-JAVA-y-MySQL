package ec.edu.monster.controlador;

import ec.edu.monster.dao.PaisDAO;
import ec.edu.monster.modelo.Configuracion;
import ec.edu.monster.modelo.Pais;
import ec.edu.monster.servicio.PaisService;
import ec.edu.monster.servicio.PaisServiceResiliente;
import ec.edu.monster.servicio.RestCountriesDatasetService;
import ec.edu.monster.servicio.RestCountriesV5Service;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador (Singleton): coordina Vista, Servicio (API) y DAO (MySQL).
 * La vista nunca ve SQL ni JSON.
 */
public final class PaisController {

    private static PaisController instancia;

    private final PaisService servicio; // abstraccion (DIP)
    private final PaisDAO dao;

    /** Constructor con inyeccion de dependencias (util para pruebas con Mocks). */
    private PaisController(PaisService servicio, PaisDAO dao) {
        this.servicio = servicio;
        this.dao = dao;
    }

    public static synchronized PaisController getInstancia() {
        if (instancia == null) {
            String key = Configuracion.getInstancia().getApiKey();
            PaisService principal = key.isEmpty() ? null : new RestCountriesV5Service(key);
            PaisService servicio = new PaisServiceResiliente(principal, new RestCountriesDatasetService());
            instancia = new PaisController(servicio, new PaisDAO());
        }
        return instancia;
    }

    // ------------------------------------------------- API -> MySQL

    /** Boton IMPORTAR PAISES: consume la API, convierte JSON y guarda en MySQL. */
    public int importarPaises() throws Exception {
        List<Pais> paises = servicio.obtenerTodos();
        return dao.guardarLote(paises);
    }

    /** Actividad 1: consultar un pais por nombre en la API. */
    public List<Pais> buscarEnApi(String nombre) throws Exception {
        validarTexto(nombre, "nombre a buscar");
        return servicio.buscarPorNombre(nombre.trim());
    }

    public String getFuente() {
        return servicio.nombreFuente();
    }

    // ------------------------------------------------------- CRUD

    public void crear(Pais p) throws Exception {
        validar(p);
        dao.insertar(p);
    }

    public List<Pais> listar() throws Exception {
        return dao.listar();
    }

    public void actualizar(Pais p) throws Exception {
        if (p.getId() <= 0) {
            throw new IllegalArgumentException("Seleccione un pais de la tabla para actualizar.");
        }
        validar(p);
        if (!dao.actualizar(p)) {
            throw new IllegalArgumentException("El pais ya no existe en la base de datos.");
        }
    }

    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("Seleccione un pais de la tabla para eliminar.");
        }
        dao.eliminar(id);
    }

    /** Busqueda local por nombre (en MySQL). */
    public List<Pais> buscarEnBdd(String nombre) throws Exception {
        return dao.buscarPorNombre(nombre == null ? "" : nombre.trim());
    }

    /** Actividad 4: filtrar por region (null o vacio = todas). */
    public List<Pais> buscarPorRegion(String region) throws Exception {
        return (region == null || region.isEmpty()) ? dao.listar() : dao.buscarPorRegion(region);
    }

    // --------------------------------------------------- Reportes

    /** Actividad 5: reporte estadistico en texto. */
    public String generarReporte() throws Exception {
        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("es", "EC"));
        StringBuilder sb = new StringBuilder();
        sb.append("REPORTE ESTADISTICO DE PAISES\n");
        sb.append("==============================\n\n");

        sb.append("1) Numero de paises por region\n");
        int total = 0;
        for (Map.Entry<String, Integer> e : dao.contarPorRegion().entrySet()) {
            String region = e.getKey() == null || e.getKey().isEmpty() ? "(sin region)" : e.getKey();
            sb.append(String.format("   %-12s %4d%n", region, e.getValue()));
            total += e.getValue();
        }
        sb.append(String.format("   %-12s %4d%n%n", "TOTAL", total));

        Pais mayor = dao.paisMasPoblado();
        sb.append("2) Pais mas poblado\n   ");
        sb.append(mayor == null ? "-" : mayor.getNombre() + " con " + nf.format(mayor.getPoblacion()) + " habitantes");
        sb.append("\n\n3) Region con mas paises\n   ").append(dao.regionConMasPaises()).append('\n');
        return sb.toString();
    }

    // --------------------------------------------------- Validaciones

    private void validar(Pais p) {
        validarTexto(p.getNombre(), "nombre");
        if (p.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres.");
        }
        if (p.getPoblacion() < 0) {
            throw new IllegalArgumentException("La poblacion no puede ser negativa.");
        }
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el " + campo + ".");
        }
    }
}
