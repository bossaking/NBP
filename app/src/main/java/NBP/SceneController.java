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

//Klasa odpowiadaj�ca do zarz�dzania okienkiem aplikacji oraz ca�� logik�
public class SceneController implements Initializable {

    //Lista walut
    private List<Rate> rates;

    @FXML
    private DatePicker datePicker; //Kontrolka do wyboru daty
    @FXML
    private Label notFoundDateLabel; //Kontrolka do wypisywania komunikat�w
    @FXML
    private ComboBox<Rate> currenciesComboBox; //Kontrolka do wyboru waluty

    private Rate curRate; //Aktualnie wybrana waluta

    //Fabryka umozliwiaj�ca wy�wietlanie nazw walut w odpowiedniej kontrolce
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
    private TextField plnInput; //Kontrolka do wpisywania ilo�ci w z� kt�r� trzeba przeliczy�
    @FXML
    private Label plnLabel; //Kontrolka do wypisywania przeliczonej warto�ci ze z� na wybran� walut�
    @FXML
    private TextField otherInput; //Kontrolka do wpisywania ilo�ci w wybranej walucie kt�r� trzeba przeliczy�
    @FXML
    private Label otherLabel;//Kontrolka do wypisywania przeliczonej warto�ci z wybranej waluty na z�

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     * 
     * metoda zostaje wywo�ana w momencie odtworzenia okna aplikacji
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Ustawiamy zakres dat. Minimalna data to 10 marca 2012 roku. Maksymalna data to dzisiejsza
        LocalDate min = LocalDate.of(2012, Month.MARCH, 10);
        LocalDate max = LocalDate.now();

        //Musimy wy��czy� mo�liwo�� wyboru daty spoza naszego zakresu
        datePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(max) || item.isBefore(min));
            }
        });
        
        //Ustawiamy nads�uchiwanie zmian w kontrolce wyboru daty
        datePicker.valueProperty().addListener((ov, oldValue, newValue) -> {
            //W momencie wyboru jakiejkolwiek dost�pnej dany zostanie wywo�ana metoda pobierania kurs�w walu dla danej daty
            getRates(newValue, false);
        });

        //Ustawiamy nasz� fabryk� dla kontrolki
        currenciesComboBox.setButtonCell(cellFactory.call(null));
        currenciesComboBox.setCellFactory(cellFactory);

        //Ustawiamy nads�uchiwanie zmian w kontrolce wyboru waluty
        currenciesComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            //Je�li nowa warto�� nie zosta�a wybrana to korzystamy ze starej
            if(newValue == null) newValue = oldValue;
            //Ustawiamy aktualnie wybran� walut�
            this.curRate = newValue;
            plnInput.setText("1"); //Ustawiamy warto�� do przeliczenia dla z� na 1
            double plnValue = 1 / newValue.getMid(); //Obliczamy warto�� wybranej waluty na podstawie kursu za 1 z�
            plnLabel.setText("PLN = " + String.format("%.4f", plnValue) + " " + newValue.getCode()); //Wy�wietlamy obliczon� warto�� dla u�ytkownika
            otherInput.setText("1");//Ustawiamy warto�� do przeliczenia dla wybranej waluty na 1
            otherLabel.setText(newValue.getCode() + " = " + newValue.getMid() + " PLN"); //Wy�wietlamy obliczon� warto�� dla u�ytkownika
        });

        //Musimy uniemo�liwi� wprowadzanie danych do kontrolek gdy� waluta mo�e nie zosta� jeszcze pobrana
        plnInput.setDisable(true);
        otherInput.setDisable(true);

        //Ustawiamy nads�uchiwanie zmian w kontrolce do podawania warto�ci do przeliczenia w z�
        plnInput.textProperty().addListener((obs, oldValue, newValue) -> {
            if(curRate == null) return; //Je�li waluta nie zosta�a wybrana - wychodzimy
            try {// Probujemy przekonwertowa� podan� warto�� do typu zmiennoprzecinkowego
                double val = Double.parseDouble(newValue);
                if (val != 0) { //Je�li podana warto�� jest zerem - wychodzimy
                    //Inaczej obliczamy warto�� na podstawie wybranego kursu oraz podanej warto�ci dla �� w wybranej walucie
                    plnLabel.setText("PLN = " + String.format("%.4f", val / curRate.getMid()) + " " + curRate.getCode());
                }
            } catch (NumberFormatException ex) {
            }
        });

        //Ustawiamy nads�uchiwanie zmian w kontrolce do podawania warto�ci do przeliczenia w wybranej walucie
        otherInput.textProperty().addListener((obs, oldValue, newValue) -> {
            if(curRate == null) return;//Je�li waluta nie zosta�a wybrana - wychodzimy
            try {// Probujemy przekonwertowa� podan� warto�� do typu zmiennoprzecinkowego
                double val = Double.parseDouble(newValue);
                if (val != 0) {//Je�li podana warto�� jest zerem - wychodzimy
                    //Inaczej obliczamy warto�� na podstawie wybranego kursu oraz podanej warto�ci dla wybranej waluty w z�
                    otherLabel.setText(curRate.getCode() + " = " + String.format("%.4f", curRate.getMid() * val) + " PLN");
                }
            } catch (NumberFormatException ex) {
            }
        });
    }

    //Metoda, pozwalaj�ca na pobiranie kurs�w walut z podanej daty oraz ustawienie odpowiednich zmniennych
    private void getRates(LocalDate date, boolean recursive) {
        //Wy��czamy mo�liwo�� wyboru waluty
        currenciesComboBox.setDisable(true);
        //Tworzymy nowy obiekt klasy do wysy�ania ��da� do serwera
        HttpClient client = HttpClient.newHttpClient();
        //Tworzymy nowe ��danie http na adres api udost�pniony poprzez serwis NBP oraz doklejamy do linku podan� dat�
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://api.nbp.pl/api/exchangerates/tables/A/" + date)).build();
        try { //Pr�bujemy wys�a� ��danie
            //Wysy�amy ��danie oraz dostajemy odpowied�
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            //Sprawdzamy kod odpowiedzi od serwera
            switch (response.statusCode()) {
                case 200: //Ten kod oznacza �e wszystko jest ok i dostali�my jakie� dane
                    if (recursive) { //Je�li flaga by�a ustawiona na true - wypisujemy odpowiedni komunikat
                        notFoundDateLabel.setText("Brak danych dla wybranej daty. Pokazano kursy z dnia " + date);
                    } else { //Inaczej czy�cimy warto�� tej kontrolki
                        notFoundDateLabel.setText("");
                    }
                    //Odpoweid� w naszym przypadku dostajemy jako tablic� json wi�c pobieramy t� tablic�
                    JSONArray array = new JSONArray(response.body()); 
                    //W tej tablicy jest tylko jeden nasz g�owny element kt�ry przedstawia sob� obiekt json wi�c pobieramy ten pierwszy obiekt
                    JSONObject obj = array.getJSONObject(0);
                    //Tworzymy now� list� kurs�w walut usuwaj�c tym samym poprzednie
                    this.rates = new ArrayList<>();
                    //Kursy walut w odpoweidzi znajduj� si� w tablicy o nazwie rates wi�c wyci�gamy t� tablic�
                    JSONArray jsonRates = obj.getJSONArray("rates");
                    //Teraz musimy przej�� po wszystkich elementach oraz uzupe�ni� nasz� list� kurs�w walut
                    for (int i = 0; i < jsonRates.length(); i++) {
                        //Dodajemy nowy obiekt typu Rate do listy, tworz�c go na podstawie danych z obiekty typu json
                        this.rates.add(new Rate(jsonRates.getJSONObject(i).getString("currency"),
                                jsonRates.getJSONObject(i).getString("code"), jsonRates.getJSONObject(i).getDouble("mid")));
                    }
                    //Usuwamy wszystkie warto�ci z kontrolki wybotu walut
                    currenciesComboBox.getItems().clear();
                    //Dodajemy nasze waluty do kontrolki
                    currenciesComboBox.getItems().addAll(rates);
                    //Wybieramy pierwsz� opcj� z listy
                    currenciesComboBox.getSelectionModel().selectFirst();
                    //Odblokowujemy kontrolk�
                    currenciesComboBox.setDisable(false);
                    //Odblokowujemy kontrolki do wprowadzania warto�ci do przeliczenia
                    plnInput.setDisable(false);
                    otherInput.setDisable(false);
                    //Wypisujemy komunikat do konsoli
                    System.out.println("Za�adowano kursy walut z dnia " + date);
                    break;
                case 404: //Ten kod oznacza �e nie udost�pniono kurs�w walut dla podanej dany
                    getRates(date.plusDays(1), true); //W takim przypadku dodajemy dzie� do podanej daty oraz wywo�ujemy t� sam� metod� ustawiaj�c flage.
                    //Dzi�ki temu b�dziemy szli dzie� do przodu do momentu a� znajdziemy dzie� w kt�rym kursy walut zosta�y udost�pnione albo a� wyjdziemy poza zakres naszych dat
                    break;
                default: //W przypadku innych kod�w
                    //Wy��czamy mo�liwo�� podawania warto�ci do przeliczenia
                    plnInput.setDisable(true);
                    otherInput.setDisable(true);
                    //Wypisujemy komunikat dla u�ytkownika w okienku aplikacji
                    notFoundDateLabel.setText("Co� posz�o nie tak. B��d zosta� wypisany do konsoli");
                    //Wypisujemy zawarto�� b��du do konsoli
                    System.out.println(response.body());
                    break;
            }

        } catch (IOException | InterruptedException | JSONException e) { //Obs�ugiwanie wyj�tk�w wysy�ania �adania oraz odczytywania danych w postaci json
            System.out.println(e.getMessage());//Wypisywanie zawarto�ci b��du do konsoli
        }
    }

}
