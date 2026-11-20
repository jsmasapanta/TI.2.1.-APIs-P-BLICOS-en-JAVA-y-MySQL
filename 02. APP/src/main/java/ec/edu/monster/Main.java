package ec.edu.monster;

import ec.edu.monster.vista.FrmPais;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // se usa el look and feel por defecto
        }
        SwingUtilities.invokeLater(() -> new FrmPais().setVisible(true));
    }
}
