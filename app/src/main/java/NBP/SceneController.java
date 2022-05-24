package NBP;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Klasa odpowiadaj¹ca do zarz¹dzania okienkiem aplikacji oraz ca³¹ logik¹
public class SceneController implements Initializable {

    //Lista walut
    private List<Rate> rates;

    @FXML
    private DatePicker datePicker; //Kontrolka do wyboru daty
    @FXML
    private Label notFoundDateLabel; //Kontrolka do wypisywania komunikatów
    @FXML
    private ComboBox<Rate> currenciesComboBox; //Kontrolka do wyboru waluty

    private Rate curRate; //Aktualnie wybrana waluta

    //Fabryka umozliwiaj¹ca wyœwietlanie nazw walut w odpowiedniej kontrolce
    Callback<ListView<Rate>, ListCell<Rate>> cellFactory = (ListView<Rate> l) -> new ListCell<Rate>() {

        @Override
        protected void updateItem(Rate item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setGraphic(null);
            } else {
                setText(item.getCurrency());
            }
        }
    };
    @FXML
    private TextField plnInput; //Kontrolka do wpisywania iloœci w z³ któr¹ trzeba przeliczyæ
    @FXML
    private Label plnLabel; //Kontrolka do wypisywania przeliczonej wartoœci ze z³ na wybran¹ walutê
    @FXML
    private TextField otherInput; //Kontrolka do wpisywania iloœci w wybranej walucie któr¹ trzeba przeliczyæ
    @FXML
    private Label otherLabel;//Kontrolka do wypisywania przeliczonej wartoœci z wybranej waluty na z³

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     * 
     * metoda zostaje wywo³ana w momencie odtworzenia okna aplikacji
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Ustawiamy zakres dat. Minimalna data to 10 marca 2012 roku. Maksymalna data to dzisiejsza
        LocalDate min = LocalDate.of(2012, Month.MARCH, 10);
        LocalDate max = LocalDate.now();

        //Musimy wy³¹czyæ mo¿liwoœæ wyboru daty spoza naszego zakresu
        datePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(max) || item.isBefore(min));
            }
        });
        
        //Ustawiamy nads³uchiwanie zmian w kontrolce wyboru daty
        datePicker.valueProperty().addListener((ov, oldValue, newValue) -> {
            //W momencie wyboru jakiejkolwiek dostêpnej dany zostanie wywo³ana metoda pobierania kursów walu dla danej daty
            getRates(newValue, false);
        });

        //Ustawiamy nasz¹ fabrykê dla kontrolki
        currenciesComboBox.setButtonCell(cellFactory.call(null));
        currenciesComboBox.setCellFactory(cellFactory);

        //Ustawiamy nads³uchiwanie zmian w kontrolce wyboru waluty
        currenciesComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            //Jeœli nowa wartoœæ nie zosta³a wybrana to korzystamy ze starej
            if(newValue == null) newValue = oldValue;
            //Ustawiamy aktualnie wybran¹ walutê
            this.curRate = newValue;
            plnInput.setText("1"); //Ustawiamy wartoœæ do przeliczenia dla z³ na 1
            double plnValue = 1 / newValue.getMid(); //Obliczamy wartoœæ wybranej waluty na podstawie kursu za 1 z³
            plnLabel.setText("PLN = " + String.format("%.4f", plnValue) + " " + newValue.getCode()); //Wyœwietlamy obliczon¹ wartoœæ dla u¿ytkownika
            otherInput.setText("1");//Ustawiamy wartoœæ do przeliczenia dla wybranej waluty na 1
            otherLabel.setText(newValue.getCode() + " = " + newValue.getMid() + " PLN"); //Wyœwietlamy obliczon¹ wartoœæ dla u¿ytkownika
        });

        //Musimy uniemo¿liwiæ wprowadzanie danych do kontrolek gdy¿ waluta mo¿e nie zostaæ jeszcze pobrana
        plnInput.setDisable(true);
        otherInput.setDisable(true);

        //Ustawiamy nads³uchiwanie zmian w kontrolce do podawania wartoœci do przeliczenia w z³
        plnInput.textProperty().addListener((obs, oldValue, newValue) -> {
            if(curRate == null) return; //Jeœli waluta nie zosta³a wybrana - wychodzimy
            try {// Probujemy przekonwertowaæ podan¹ wartoœæ do typu zmiennoprzecinkowego
                double val = Double.parseDouble(newValue);
                if (val != 0) { //Jeœli podana wartoœæ jest zerem - wychodzimy
                    //Inaczej obliczamy wartoœæ na podstawie wybranego kursu oraz podanej wartoœci dla ¿³ w wybranej walucie
                    plnLabel.setText("PLN = " + String.format("%.4f", val / curRate.getMid()) + " " + curRate.getCode());
                }
            } catch (NumberFormatException ex) {
            }
        });

        //Ustawiamy nads³uchiwanie zmian w kontrolce do podawania wartoœci do przeliczenia w wybranej walucie
        otherInput.textProperty().addListener((obs, oldValue, newValue) -> {
            if(curRate == null) return;//Jeœli waluta nie zosta³a wybrana - wychodzimy
            try {// Probujemy przekonwertowaæ podan¹ wartoœæ do typu zmiennoprzecinkowego
                double val = Double.parseDouble(newValue);
                if (val != 0) {//Jeœli podana wartoœæ jest zerem - wychodzimy
                    //Inaczej obliczamy wartoœæ na podstawie wybranego kursu oraz podanej wartoœci dla wybranej waluty w z³
                    otherLabel.setText(curRate.getCode() + " = " + String.format("%.4f", curRate.getMid() * val) + " PLN");
                }
            } catch (NumberFormatException ex) {
            }
        });
    }

    //Metoda, pozwalaj¹ca na pobiranie kursów walut z podanej daty oraz ustawienie odpowiednich zmniennych
    private void getRates(LocalDate date, boolean recursive) {
        //Wy³¹czamy mo¿liwoœæ wyboru waluty
        currenciesComboBox.setDisable(true);
        //Tworzymy nowy obiekt klasy do wysy³ania ¿¹dañ do serwera
        HttpClient client = HttpClient.newHttpClient();
        //Tworzymy nowe ¿¹danie http na adres api udostêpniony poprzez serwis NBP oraz doklejamy do linku podan¹ datê
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://api.nbp.pl/api/exchangerates/tables/A/" + date)).build();
        try { //Próbujemy wys³aæ ¿¹danie
            //Wysy³amy ¿¹danie oraz dostajemy odpowiedŸ
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //Sprawdzamy kod odpowiedzi od serwera
            switch (response.statusCode()) {
                case 200: //Ten kod oznacza ¿e wszystko jest ok i dostaliœmy jakieœ dane
                    if (recursive) { //Jeœli flaga by³a ustawiona na true - wypisujemy odpowiedni komunikat
                        notFoundDateLabel.setText("Brak danych dla wybranej daty. Pokazano kursy z dnia " + date);
                    } else { //Inaczej czyœcimy wartoœæ tej kontrolki
                        notFoundDateLabel.setText("");
                    }
                    //OdpoweidŸ w naszym przypadku dostajemy jako tablicê json wiêc pobieramy t¹ tablicê
                    JSONArray array = new JSONArray(response.body()); 
                    //W tej tablicy jest tylko jeden nasz g³owny element który przedstawia sob¹ obiekt json wiêc pobieramy ten pierwszy obiekt
                    JSONObject obj = array.getJSONObject(0);
                    //Tworzymy now¹ listê kursów walut usuwaj¹c tym samym poprzednie
                    this.rates = new ArrayList<>();
                    //Kursy walut w odpoweidzi znajduj¹ siê w tablicy o nazwie rates wiêc wyci¹gamy t¹ tablicê
                    JSONArray jsonRates = obj.getJSONArray("rates");
                    //Teraz musimy przejœæ po wszystkich elementach oraz uzupe³niæ nasz¹ listê kursów walut
                    for (int i = 0; i < jsonRates.length(); i++) {
                        //Dodajemy nowy obiekt typu Rate do listy, tworz¹c go na podstawie danych z obiekty typu json
                        this.rates.add(new Rate(jsonRates.getJSONObject(i).getString("currency"),
                                jsonRates.getJSONObject(i).getString("code"), jsonRates.getJSONObject(i).getDouble("mid")));
                    }
                    //Usuwamy wszystkie wartoœci z kontrolki wybotu walut
                    currenciesComboBox.getItems().clear();
                    //Dodajemy nasze waluty do kontrolki
                    currenciesComboBox.getItems().addAll(rates);
                    //Wybieramy pierwsz¹ opcjê z listy
                    currenciesComboBox.getSelectionModel().selectFirst();
                    //Odblokowujemy kontrolkê
                    currenciesComboBox.setDisable(false);
                    //Odblokowujemy kontrolki do wprowadzania wartoœci do przeliczenia
                    plnInput.setDisable(false);
                    otherInput.setDisable(false);
                    //Wypisujemy komunikat do konsoli
                    System.out.println("Za³adowano kursy walut z dnia " + date);
                    break;
                case 404: //Ten kod oznacza ¿e nie udostêpniono kursów walut dla podanej dany
                    getRates(date.plusDays(1), true); //W takim przypadku dodajemy dzieñ do podanej daty oraz wywo³ujemy t¹ sam¹ metodê ustawiaj¹c flage.
                    //Dziêki temu bêdziemy szli dzieñ do przodu do momentu a¿ znajdziemy dzieñ w którym kursy walut zosta³y udostêpnione albo a¿ wyjdziemy poza zakres naszych dat
                    break;
                default: //W przypadku innych kodów
                    //Wy³¹czamy mo¿liwoœæ podawania wartoœci do przeliczenia
                    plnInput.setDisable(true);
                    otherInput.setDisable(true);
                    //Wypisujemy komunikat dla u¿ytkownika w okienku aplikacji
                    notFoundDateLabel.setText("Coœ posz³o nie tak. B³¹d zosta³ wypisany do konsoli");
                    //Wypisujemy zawartoœæ b³êdu do konsoli
                    System.out.println(response.body());
                    break;
            }

        } catch (IOException | InterruptedException | JSONException e) { //Obs³ugiwanie wyj¹tków wysy³ania ¿adania oraz odczytywania danych w postaci json
            System.out.println(e.getMessage());//Wypisywanie zawartoœci b³êdu do konsoli
        }
    }

}
