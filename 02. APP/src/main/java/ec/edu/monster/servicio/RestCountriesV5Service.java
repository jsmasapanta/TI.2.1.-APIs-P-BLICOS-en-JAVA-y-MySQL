package ec.edu.monster.servicio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.modelo.Pais;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * API publica REST Countries v5 (https://api.restcountries.com/countries/v5).
 * Requiere API key gratuita. Respuesta: { "data": { "objects": [...], "meta": {...} } }
 */
public class RestCountriesV5Service implements PaisService {

    private static final String BASE = "https://api.restcountries.com/countries/v5";
    private static final String CAMPOS = "response_fields=names.common,capitals,region,population,currencies";
    private static final int PAGINA = 100; // maximo del plan gratuito

    private final String apiKey;

    public RestCountriesV5Service(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<Pais> obtenerTodos() throws Exception {
        List<Pais> todos = new ArrayList<>();
        int offset = 0;
        boolean hayMas = true;
        while (hayMas) { // la v5 es paginada: ~250 paises = 3 peticiones
            String json = ConsumoAPI.obtenerDatos(
                    BASE + "?" + CAMPOS + "&limit=" + PAGINA + "&offset=" + offset, apiKey);
            JsonObject data = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("data");
            todos.addAll(convertir(data.getAsJsonArray("objects")));
            JsonObject meta = data.getAsJsonObject("meta");
            hayMas = meta != null && meta.has("more") && meta.get("more").getAsBoolean();
            offset += PAGINA;
        }
        return todos;
    }

    @Override
    public List<Pais> buscarPorNombre(String nombre) throws Exception {
        String q = URLEncoder.encode(nombre, StandardCharsets.UTF_8);
        String json = ConsumoAPI.obtenerDatos(BASE + "/name?q=" + q + "&" + CAMPOS, apiKey);
        if (json == null) {
            return new ArrayList<>();
        }
        JsonObject data = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("data");
        return convertir(data.getAsJsonArray("objects"));
    }

    @Override
    public String nombreFuente() {
        return "REST Countries v5";
    }

    /** JSON -> objetos Pais. */
    private List<Pais> convertir(JsonArray objetos) {
        List<Pais> lista = new ArrayList<>();
        if (objetos == null) {
            return lista;
        }
        for (JsonElement e : objetos) {
            JsonObject o = e.getAsJsonObject();
            String nombre = o.getAsJsonObject("names").get("common").getAsString();

            String capital = "";
            JsonArray capitales = o.has("capitals") ? o.getAsJsonArray("capitals") : null;
            if (capitales != null && !capitales.isEmpty()) {
                capital = capitales.get(0).getAsJsonObject().get("name").getAsString();
            }

            String region = Json.texto(o, "region");
            long poblacion = o.has("population") && !o.get("population").isJsonNull()
                    ? o.get("population").getAsLong() : 0;

            String moneda = "";
            JsonArray monedas = o.has("currencies") ? o.getAsJsonArray("currencies") : null;
            if (monedas != null && !monedas.isEmpty()) {
                JsonObject m = monedas.get(0).getAsJsonObject();
                moneda = Json.texto(m, "code") + " - " + Json.texto(m, "name");
            }
            lista.add(new Pais(nombre, capital, region, poblacion, moneda));
        }
        return lista;
    }
}
