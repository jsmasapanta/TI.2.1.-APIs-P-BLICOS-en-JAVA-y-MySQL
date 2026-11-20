package ec.edu.monster.dao;

import ec.edu.monster.modelo.Pais;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO: todo el SQL de la tabla pais esta aqui (CRUD, busquedas y reportes).
 */
public class PaisDAO {

    private final Conexion conexion = Conexion.getInstancia();

    // ------------------------------------------------------------ CRUD

    /** Crear. */
    public void insertar(Pais p) throws SQLException {
        String sql = "INSERT INTO pais(nombre, capital, region, poblacion, moneda) VALUES(?,?,?,?,?)";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            llenar(ps, p);
            ps.executeUpdate();
        }
    }

    /**
     * Guarda la lista importada de la API. Si el pais ya existe (mismo nombre)
     * actualiza sus datos, asi reimportar no duplica registros.
     * @return numero de paises procesados
     */
    public int guardarLote(List<Pais> paises) throws SQLException {
        String sql = "INSERT INTO pais(nombre, capital, region, poblacion, moneda) VALUES(?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE capital=VALUES(capital), region=VALUES(region), "
                + "poblacion=VALUES(poblacion), moneda=VALUES(moneda)";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            for (Pais p : paises) {
                llenar(ps, p);
                ps.addBatch();
            }
            ps.executeBatch();
            con.commit();
            return paises.size();
        }
    }

    /** Leer todos. */
    public List<Pais> listar() throws SQLException {
        return consultar("SELECT * FROM pais ORDER BY nombre", null);
    }

    /** Leer uno. */
    public Pais buscarPorId(int id) throws SQLException {
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM pais WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Actualizar. */
    public boolean actualizar(Pais p) throws SQLException {
        String sql = "UPDATE pais SET nombre=?, capital=?, region=?, poblacion=?, moneda=? WHERE id=?";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            llenar(ps, p);
            ps.setInt(6, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Eliminar. */
    public boolean eliminar(int id) throws SQLException {
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement("DELETE FROM pais WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Busquedas

    /** Actividad 1 (sobre la BDD): buscar por nombre. */
    public List<Pais> buscarPorNombre(String texto) throws SQLException {
        return consultar("SELECT * FROM pais WHERE nombre LIKE ? ORDER BY nombre", "%" + texto + "%");
    }

    /** Actividad 4: busqueda por region. */
    public List<Pais> buscarPorRegion(String region) throws SQLException {
        return consultar("SELECT * FROM pais WHERE region=? ORDER BY nombre", region);
    }

    // -------------------------------------------------------- Reportes

    /** Actividad 5a: numero de paises por region. */
    public Map<String, Integer> contarPorRegion() throws SQLException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        String sql = "SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString("region"), rs.getInt("total"));
            }
        }
        return mapa;
    }

    /** Actividad 5b: pais mas poblado. */
    public Pais paisMasPoblado() throws SQLException {
        List<Pais> l = consultar("SELECT * FROM pais ORDER BY poblacion DESC LIMIT 1", null);
        return l.isEmpty() ? null : l.get(0);
    }

    /** Actividad 5c: region con mas paises (las 3 consultas usan SQL, no Java). */
    public String regionConMasPaises() throws SQLException {
        String sql = "SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC LIMIT 1";
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("region") + " (" + rs.getInt("total") + " paises)" : "-";
        }
    }

    // --------------------------------------------------------- Apoyo

    private List<Pais> consultar(String sql, String parametro) throws SQLException {
        List<Pais> lista = new ArrayList<>();
        try (Connection con = conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private void llenar(PreparedStatement ps, Pais p) throws SQLException {
        ps.setString(1, p.getNombre());
        ps.setString(2, p.getCapital());
        ps.setString(3, p.getRegion());
        ps.setLong(4, p.getPoblacion());
        ps.setString(5, p.getMoneda());
    }

    private Pais mapear(ResultSet rs) throws SQLException {
        return new Pais(rs.getInt("id"), rs.getString("nombre"), rs.getString("capital"),
                rs.getString("region"), rs.getLong("poblacion"), rs.getString("moneda"));
    }
}
