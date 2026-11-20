package ec.edu.monster.servicio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.modelo.Pais;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fuente de respaldo SIN API key: el dataset publico del proyecto original
 * REST Countries en GitHub (formato v2). Se usa cuando no hay API key o la v5 falla.
 */
public class RestCountriesDatasetService implements PaisService {

    private static final String URL =
            "https://raw.githubusercontent.com/apilayer/restcountries/master/src/main/resources/countriesV2.json";

    /** Cache en memoria: el archivo se descarga una sola vez por ejecucion. */
    private List<Pais> cache;

    @Override
    public synchronized List<Pais> obtenerTodos() throws Exception {
        if (cache == null) {
            String json = ConsumoAPI.obtenerDatos(URL, null);
            cache = convertir(JsonParser.parseString(json).getAsJsonArray());
        }
        return cache;
    }

    @Override
    public List<Pais> buscarPorNombre(String nombre) throws Exception {
        String buscado = normalizar(nombre);
        List<Pais> resultado = new ArrayList<>();
        for (Pais p : obtenerTodos()) {
            if (normalizar(p.getNombre()).contains(buscado)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public String nombreFuente() {
        return "REST Countries (dataset GitHub, respaldo)";
    }

    private List<Pais> convertir(JsonArray array) {
        List<Pais> lista = new ArrayList<>();
        for (JsonElement e : array) {
            JsonObject o = e.getAsJsonObject();
            String moneda = "";
            JsonArray monedas = o.has("currencies") ? o.getAsJsonArray("currencies") : null;
            if (monedas != null && !monedas.isEmpty()) {
                JsonObject m = monedas.get(0).getAsJsonObject();
                moneda = Json.texto(m, "code") + " - " + Json.texto(m, "name");
            }
            long poblacion = o.has("population") ? o.get("population").getAsLong() : 0;
            lista.add(new Pais(Json.texto(o, "name"), Json.texto(o, "capital"),
                    Json.texto(o, "region"), poblacion, moneda));
        }
        return lista;
    }

    /** Minusculas y sin tildes, para que "japon" encuentre "Japón". */
    private static String normalizar(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
