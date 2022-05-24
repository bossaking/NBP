package NBP;

//Klasa przedstawiaj¹ca opis pojedyñczego obiektu kursu walut
public class Rate {
    
    //Zmienne do przechowywania pe³nej oraz skróconej nazwy waluty
    private String currency, code;
    //Zmienna do przechowywania œredniej wartoœci kuna oraz sprzeda¿y danej waluty odnoœnie z³otego
    private double mid;
    
    //publiczny konstruktor
    public Rate(String currency, String code, double mid){
        this.currency = currency;
        this.code = code;
        this.mid = mid;
    }

    //Gettery oraz settery
    
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getMid() {
        return mid;
    }

    public void setMid(double mid) {
        this.mid = mid;
    }
    
    
    
}
