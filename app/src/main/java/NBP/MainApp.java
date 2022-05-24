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
 
    //Metoda zostaje wywo³ana w momencie startu aplikacji
    @Override
    public void start(Stage stage) throws Exception {
        //£adujemy graficzne przedstawienie aplikacji
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("scene.fxml"));
        
        //Tworzymy now¹ scene na podstawie za³adowanego pliku
        Scene scene = new Scene(root);
        
        //Metoda, odpawiadaj¹ca za ca³kowite zamkniêcie aplikacji w momencie gdy u¿ytkownik zamknie oknienko
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        //Ustawiamy scenê
        stage.setScene(scene);
        //Tytu³ aplikacji
        stage.setTitle("Przelicznik walut");
        //Metoda która wywo³uje pokazywanie okna
        stage.show();
    }
    
    //Metoda, pozwalaj¹ca na uruchomienie okna aplikacji
    public static void main(String[] args) {
        launch(args);
    }
    
}
