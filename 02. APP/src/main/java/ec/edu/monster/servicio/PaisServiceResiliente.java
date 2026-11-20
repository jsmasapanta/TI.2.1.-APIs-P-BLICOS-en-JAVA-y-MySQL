package ec.edu.monster.servicio;

import ec.edu.monster.modelo.Pais;
import java.util.List;

/**
 * Patron Fallback: intenta la fuente principal (REST Countries v5) y, si no hay
 * API key o falla, usa la fuente de respaldo. Implementa la misma interfaz (LSP),
 * por eso el controlador no se entera del cambio.
 */
public class PaisServiceResiliente implements PaisService {

    private final PaisService principal;   // puede ser null si no hay API key
    private final PaisService respaldo;
    private String ultimaFuente;

    public PaisServiceResiliente(PaisService principal, PaisService respaldo) {
        this.principal = principal;
        this.respaldo = respaldo;
        this.ultimaFuente = principal != null ? principal.nombreFuente() : respaldo.nombreFuente();
    }

    @Override
    public List<Pais> obtenerTodos() throws Exception {
        if (principal != null) {
            try {
                List<Pais> l = principal.obtenerTodos();
                ultimaFuente = principal.nombreFuente();
                return l;
            } catch (Exception e) {
                System.out.println("Fuente principal no disponible: " + e.getMessage());
            }
        }
        ultimaFuente = respaldo.nombreFuente();
        return respaldo.obtenerTodos();
    }

    @Override
    public List<Pais> buscarPorNombre(String nombre) throws Exception {
        if (principal != null) {
            try {
                List<Pais> l = principal.buscarPorNombre(nombre);
                ultimaFuente = principal.nombreFuente();
                return l;
            } catch (Exception e) {
                System.out.println("Fuente principal no disponible: " + e.getMessage());
            }
        }
        ultimaFuente = respaldo.nombreFuente();
        return respaldo.buscarPorNombre(nombre);
    }

    @Override
    public String nombreFuente() {
        return ultimaFuente;
    }
}
