package ec.edu.monster.modelo;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Singleton que lee config.properties (carpeta desde donde se ejecuta la app).
 * Asi se cambia de MySQL local a MySQL en la nube (AWS RDS) sin recompilar.
 */
public final class Configuracion {

    private static Configuracion instancia;
    private final Properties props = new Properties();

    private Configuracion() {
        Path archivo = Path.of("config.properties");
        if (Files.exists(archivo)) {
            try (InputStream in = new FileInputStream(archivo.toFile())) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (Exception e) {
                System.out.println("No se pudo leer config.properties: " + e.getMessage());
            }
        }
    }

    public static synchronized Configuracion getInstancia() {
        if (instancia == null) {
            instancia = new Configuracion();
        }
        return instancia;
    }

    public String get(String clave, String porDefecto) {
        String valor = props.getProperty(clave);
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }

    public String getDbUrl() {
        return get("db.url", "jdbc:mysql://localhost:3306/api_paises"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public String getDbUser() { return get("db.user", "monster"); }

    public String getDbPassword() { return get("db.password", "monster"); }

    public String getApiKey() { return get("api.key", ""); }
}
