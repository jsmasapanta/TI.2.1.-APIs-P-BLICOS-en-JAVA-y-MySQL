package ec.edu.monster.dao;

import ec.edu.monster.modelo.Configuracion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de conexion a MySQL. Entrega una conexion nueva por operacion
 * (se cierra con try-with-resources en el DAO).
 */
public final class Conexion {

    private static Conexion instancia;
    private final String url;
    private final String usuario;
    private final String clave;

    private Conexion() {
        Configuracion cfg = Configuracion.getInstancia();
        this.url = cfg.getDbUrl();
        this.usuario = cfg.getDbUser();
        this.clave = cfg.getDbPassword();
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    public String getUrl() {
        return url;
    }
}
