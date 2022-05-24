package NBP;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//Klasa aplikacji
public class MainApp extends Application {
 
    //Metoda zostaje wywo�ana w momencie startu aplikacji
    @Override
    public void start(Stage stage) throws Exception {
        //�adujemy graficzne przedstawienie aplikacji
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("scene.fxml"));
        
        //Tworzymy now� scene na podstawie za�adowanego pliku
        Scene scene = new Scene(root);
        
        //Metoda, odpawiadaj�ca za ca�kowite zamkni�cie aplikacji w momencie gdy u�ytkownik zamknie oknienko
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        //Ustawiamy scen�
        stage.setScene(scene);
        //Tytu� aplikacji
        stage.setTitle("Przelicznik walut");
        //Metoda kt�ra wywo�uje pokazywanie okna
        stage.show();
    }
    
    //Metoda, pozwalaj�ca na uruchomienie okna aplikacji
    public static void main(String[] args) {
        launch(args);
    }
    
}
