package ec.edu.monster.servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP GET reutilizable (HttpURLConnection, como en el enunciado):
 * devuelve el JSON como texto. Incluye timeouts y un reintento ante fallos transitorios.
 */
public final class ConsumoAPI {

    private static final int REINTENTOS = 2;
    private static final int TIMEOUT_CONEXION_MS = 10_000;
    private static final int TIMEOUT_LECTURA_MS = 30_000;

    private ConsumoAPI() {
    }

    /**
     * @param url   endpoint
     * @param token API key (Bearer) o null si la fuente no la necesita
     * @return cuerpo JSON, o null si la API responde 404 (sin resultados)
     */
    public static String obtenerDatos(String url, String token) throws IOException, InterruptedException {
        IOException ultimo = null;
        for (int intento = 1; intento <= REINTENTOS; intento++) {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) URI.create(url).toURL().openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(TIMEOUT_CONEXION_MS);
                con.setReadTimeout(TIMEOUT_LECTURA_MS);
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-Agent", "ApiPaises-Java/1.0");
                if (token != null && !token.isBlank()) {
                    con.setRequestProperty("Authorization", "Bearer " + token);
                }

                int estado = con.getResponseCode();
                if (estado == 200) {
                    return leer(con);
                }
                if (estado == 404) {
                    return null;
                }
                ultimo = new IOException("HTTP " + estado + " en " + url);
                if (estado < 500) {
                    break; // 400, 401, 429... reintentar no sirve
                }
            } catch (IOException e) {
                ultimo = e; // sin red, timeout...
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            Thread.sleep(1000L * intento);
        }
        throw ultimo;
    }

    private static String leer(HttpURLConnection con) throws IOException {
        StringBuilder resultado = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                resultado.append(linea);
            }
        }
        return resultado.toString();
    }
}
