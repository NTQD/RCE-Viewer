package remotecommandexecution;

import javax.swing.SwingUtilities;
import remotecommandexecution.gui.LoginForm;

public class RemoteCommandExecution {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginForm login = new LoginForm();
            login.setVisible(true);
            login.setLocationRelativeTo(null);
        });
    }
}
