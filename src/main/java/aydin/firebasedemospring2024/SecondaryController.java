package aydin.firebasedemospring2024;

import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        DemoApp.setRoot("primary");
    }

    public void switchToSignIn() throws IOException{
        DemoApp.setRoot("login");
    }


    public void switchToRegister() throws IOException{
        DemoApp.setRoot("register");
    }

}
