package ec.edu.monster.vista;

import ec.edu.monster.controlador.PaisController;
import ec.edu.monster.modelo.Pais;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Vista: solo muestra datos y delega cada accion al controlador.
 * Las llamadas lentas (API / MySQL) corren en SwingWorker para no congelar la ventana.
 */
public class FrmPais extends JFrame {

    /** Etiqueta en espanol -> valor de region que devuelve la API (Actividad 4). */
    private static final String[][] REGIONES = {
        {"Todas", ""}, {"America", "Americas"}, {"Europa", "Europe"},
        {"Asia", "Asia"}, {"Africa", "Africa"}, {"Oceania", "Oceania"}
    };
    private static final String[] COLUMNAS = {"ID", "Nombre", "Capital", "Region", "Poblacion", "Moneda"};

    private final PaisController controller = PaisController.getInstancia();
    private final NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("es", "EC"));

    // Barra superior
    private final JButton btnImportar = new JButton("IMPORTAR PAISES");
    private final JLabel lblEstado = new JLabel(" ");

    // Pestana CRUD
    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtCapital = new JTextField();
    private final JComboBox<String> cboRegionForm = new JComboBox<>(
            new String[]{"Americas", "Europe", "Asia", "Africa", "Oceania", "Antarctic"});
    private final JTextField txtPoblacion = new JTextField();
    private final JTextField txtMoneda = new JTextField();
    private final JComboBox<String> cboFiltroRegion = new JComboBox<>();
    private final JTextField txtFiltroNombre = new JTextField(12);
    private final JLabel lblTotal = new JLabel("0 paises");
    private final DefaultTableModel modeloBdd = modeloTabla();
    private final JTable tblBdd = new JTable(modeloBdd);

    // Pestana API
    private final JTextField txtBuscarApi = new JTextField("ecuador", 20);
    private final DefaultTableModel modeloApi = modeloTabla();
    private final JTable tblApi = new JTable(modeloApi);
    private List<Pais> resultadosApi = List.of();

    // Pestana Reportes
    private final JTextArea txtReporte = new JTextArea();

    public FrmPais() {
        super("Consumo de API Publica REST Countries - Java + MySQL + MVC");
        initComponents();
        cargarTabla(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        // ---------- Barra superior
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        btnImportar.setFont(btnImportar.getFont().deriveFont(Font.BOLD, 14f));
        btnImportar.setBackground(new Color(0x2E7D32));
        btnImportar.setForeground(Color.WHITE);
        btnImportar.addActionListener(e -> btnImportarActionPerformed());
        JLabel titulo = new JLabel("Paises del mundo");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        barra.add(titulo, BorderLayout.WEST);
        barra.add(lblEstado, BorderLayout.CENTER);
        barra.add(btnImportar, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Paises en MySQL (CRUD)", panelCrud());
        tabs.addTab("Consultar API por nombre", panelApi());
        tabs.addTab("Reportes", panelReportes());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barra, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
    }

    // ================================================================ Paneles

    private JPanel panelCrud() {
        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos del pais"));
        txtId.setEditable(false);
        cboRegionForm.setEditable(true);
        Object[][] campos = {
            {"ID", txtId}, {"Nombre *", txtNombre}, {"Capital", txtCapital},
            {"Region", cboRegionForm}, {"Poblacion", txtPoblacion}, {"Moneda", txtMoneda}
        };
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < campos.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            form.add(new JLabel((String) campos[i][0]), c);
            c.gridx = 1; c.weightx = 1;
            form.add((java.awt.Component) campos[i][1], c);
        }
        JPanel botones = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        botones.add(btnNuevo); botones.add(btnGuardar);
        botones.add(btnActualizar); botones.add(btnEliminar);
        c.gridx = 0; c.gridy = campos.length; c.gridwidth = 2; c.insets = new Insets(14, 6, 4, 6);
        form.add(botones, c);
        c.gridy++; c.weighty = 1;
        form.add(new JLabel(" "), c);
        form.setPreferredSize(new Dimension(320, 0));

        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());

        // Filtros + tabla (Actividades 3 y 4)
        for (String[] r : REGIONES) {
            cboFiltroRegion.addItem(r[0]);
        }
        cboFiltroRegion.addActionListener(e -> {
            txtFiltroNombre.setText("");
            cargarTabla(null);
        });
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> cargarTabla(txtFiltroNombre.getText()));
        txtFiltroNombre.addActionListener(e -> cargarTabla(txtFiltroNombre.getText()));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Region:"));
        filtros.add(cboFiltroRegion);
        filtros.add(new JLabel("   Nombre:"));
        filtros.add(txtFiltroNombre);
        filtros.add(btnBuscar);
        filtros.add(new JLabel("   "));
        filtros.add(lblTotal);

        configurarTabla(tblBdd);
        tblBdd.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(filtros, BorderLayout.NORTH);
        derecha.add(new JScrollPane(tblBdd), BorderLayout.CENTER);

        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        p.add(form, BorderLayout.WEST);
        p.add(derecha, BorderLayout.CENTER);
        return p;
    }

    private JPanel panelApi() {
        JButton btnConsultar = new JButton("Consultar API");
        JButton btnGuardarSel = new JButton("Guardar seleccionado en MySQL");
        btnConsultar.addActionListener(e -> consultarApi());
        txtBuscarApi.addActionListener(e -> consultarApi());
        btnGuardarSel.addActionListener(e -> guardarDesdeApi());

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(new JLabel("Nombre del pais:"));
        arriba.add(txtBuscarApi);
        arriba.add(btnConsultar);
        arriba.add(btnGuardarSel);
        configurarTabla(tblApi);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        p.add(arriba, BorderLayout.NORTH);
        p.add(new JScrollPane(tblApi), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelReportes() {
        JButton btnGenerar = new JButton("Generar reporte");
        btnGenerar.addActionListener(e -> ejecutar("Generando reporte...",
                controller::generarReporte, txtReporte::setText));
        txtReporte.setEditable(false);
        txtReporte.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(btnGenerar);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        p.add(arriba, BorderLayout.NORTH);
        p.add(new JScrollPane(txtReporte), BorderLayout.CENTER);
        return p;
    }

    // ================================================================ Acciones

    private void btnImportarActionPerformed() {
        btnImportar.setEnabled(false);
        ejecutar("Consumiendo la API e importando paises...", controller::importarPaises, n -> {
            btnImportar.setEnabled(true);
            cargarTabla(null);
            JOptionPane.showMessageDialog(this,
                    "Importacion Finalizada\n" + n + " paises guardados en MySQL\nFuente: " + controller.getFuente(),
                    "Importar paises", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void cargarTabla(String nombre) {
        String region = REGIONES[Math.max(0, cboFiltroRegion.getSelectedIndex())][1];
        Callable<List<Pais>> consulta = (nombre != null && !nombre.isBlank())
                ? () -> controller.buscarEnBdd(nombre)
                : () -> controller.buscarPorRegion(region);
        ejecutar("Consultando MySQL...", consulta, lista -> {
            llenarTabla(modeloBdd, lista);
            lblTotal.setText(lista.size() + " paises");
        });
    }

    private void consultarApi() {
        String nombre = txtBuscarApi.getText();
        ejecutar("Consultando la API...", () -> controller.buscarEnApi(nombre), lista -> {
            resultadosApi = lista;
            llenarTabla(modeloApi, lista);
            lblEstado.setText(lista.size() + " resultado(s) desde " + controller.getFuente());
            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron paises con \"" + nombre + "\".");
            }
        });
    }

    private void guardarDesdeApi() {
        int fila = tblApi.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un pais de los resultados.");
            return;
        }
        Pais p = resultadosApi.get(tblApi.convertRowIndexToModel(fila));
        ejecutar("Guardando...", () -> { controller.crear(p); return p; }, x -> {
            cargarTabla(null);
            JOptionPane.showMessageDialog(this, p.getNombre() + " guardado en MySQL.");
        });
    }

    private void guardar() {
        Pais p = leerFormulario();
        if (p == null) {
            return;
        }
        ejecutar("Guardando...", () -> { controller.crear(p); return p; }, x -> {
            limpiar();
            cargarTabla(null);
            JOptionPane.showMessageDialog(this, "Pais creado.");
        });
    }

    private void actualizar() {
        Pais p = leerFormulario();
        if (p == null) {
            return;
        }
        ejecutar("Actualizando...", () -> { controller.actualizar(p); return p; }, x -> {
            cargarTabla(null);
            JOptionPane.showMessageDialog(this, "Pais actualizado.");
        });
    }

    private void eliminar() {
        int id = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
        if (id > 0 && JOptionPane.showConfirmDialog(this, "Eliminar " + txtNombre.getText() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutar("Eliminando...", () -> { controller.eliminar(id); return id; }, x -> {
            limpiar();
            cargarTabla(null);
        });
    }

    // ================================================================ Apoyo

    private Pais leerFormulario() {
        long poblacion;
        try {
            String t = txtPoblacion.getText().replace(".", "").replace(",", "").trim();
            poblacion = t.isEmpty() ? 0 : Long.parseLong(t);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La poblacion debe ser un numero entero.");
            return null;
        }
        int id = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
        Object region = cboRegionForm.getEditor().getItem();
        return new Pais(id, txtNombre.getText().trim(), txtCapital.getText().trim(),
                region == null ? "" : region.toString().trim(), poblacion, txtMoneda.getText().trim());
    }

    private void seleccionarFila() {
        int fila = tblBdd.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int m = tblBdd.convertRowIndexToModel(fila);
        txtId.setText(String.valueOf(modeloBdd.getValueAt(m, 0)));
        txtNombre.setText(String.valueOf(modeloBdd.getValueAt(m, 1)));
        txtCapital.setText(String.valueOf(modeloBdd.getValueAt(m, 2)));
        cboRegionForm.setSelectedItem(String.valueOf(modeloBdd.getValueAt(m, 3)));
        txtPoblacion.setText(String.valueOf(modeloBdd.getValueAt(m, 4)));
        txtMoneda.setText(String.valueOf(modeloBdd.getValueAt(m, 5)));
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtCapital.setText("");
        cboRegionForm.setSelectedIndex(0);
        txtPoblacion.setText("");
        txtMoneda.setText("");
        tblBdd.clearSelection();
        txtNombre.requestFocus();
    }

    private void llenarTabla(DefaultTableModel modelo, List<Pais> lista) {
        modelo.setRowCount(0);
        for (Pais p : lista) {
            modelo.addRow(new Object[]{p.getId() == 0 ? "" : p.getId(), p.getNombre(), p.getCapital(),
                p.getRegion(), nf.format(p.getPoblacion()), p.getMoneda()});
        }
    }

    private static DefaultTableModel modeloTabla() {
        return new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static void configurarTabla(JTable t) {
        t.setAutoCreateRowSorter(true);
        t.setRowHeight(24);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] anchos = {50, 200, 140, 100, 120, 220};
        for (int i = 0; i < anchos.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    /** Ejecuta una tarea en segundo plano y muestra el resultado (o el error) en la vista. */
    private <T> void ejecutar(String mensaje, Callable<T> tarea, Consumer<T> alTerminar) {
        lblEstado.setText(mensaje);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                btnImportar.setEnabled(true);
                try {
                    T resultado = get();
                    lblEstado.setText("Listo. Fuente API: " + controller.getFuente());
                    alTerminar.accept(resultado);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    lblEstado.setText("Error: " + causa.getMessage());
                    JOptionPane.showMessageDialog(FrmPais.this, mensajeAmigable(causa),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String mensajeAmigable(Throwable e) {
        String m = String.valueOf(e.getMessage());
        if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
            return "Ya existe un pais con ese nombre.";
        }
        if (m.contains("Communications link failure") || m.contains("Access denied")
                || m.contains("Unknown database")) {
            return "No se pudo conectar a MySQL.\nRevise que el servicio este iniciado, que exista la base "
                    + "api_paises y el usuario de config.properties.\n\nDetalle: " + m;
        }
        return m;
    }
}
