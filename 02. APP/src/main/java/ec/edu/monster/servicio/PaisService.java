package ec.edu.monster.servicio;

import ec.edu.monster.modelo.Pais;
import java.util.List;

/**
 * Abstraccion de la fuente de paises (DIP / OCP): el controlador depende de esta
 * interfaz, no de una API concreta. Se puede agregar otra API sin tocar el controlador.
 */
public interface PaisService {

    /** Todos los paises de la fuente. */
    List<Pais> obtenerTodos() throws Exception;

    /** Actividad 1: paises cuyo nombre contiene el texto (ej. "ecuador"). */
    List<Pais> buscarPorNombre(String nombre) throws Exception;

    /** Nombre legible de la fuente, para mostrar en la vista. */
    String nombreFuente();
}
