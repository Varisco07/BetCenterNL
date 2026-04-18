package core;

public class User {
    private String nome;
    private String cognome;
    private String username;
    private String email;
    private String password;
    private String dob;
    private double saldo;
    private int giociGiocati;
    private int giociVinti;
    private int giociPersi;
    private double guadagnoTotale;

    public User(String nome, String cognome, String username, String email, String password, String dob) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.dob = dob;
        this.saldo = 1000.0;
        this.giociGiocati = 0;
        this.giociVinti = 0;
        this.giociPersi = 0;
        this.guadagnoTotale = 0.0;
    }

    // Getters
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getDob() { return dob; }
    public double getSaldo() { return saldo; }
    public int getGiociGiocati() { return giociGiocati; }
    public int getGiociVinti() { return giociVinti; }
    public int getGiociPersi() { return giociPersi; }
    public double getGuadagnoTotale() { return guadagnoTotale; }

    // Setters
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public void setGiociGiocati(int giociGiocati) { this.giociGiocati = giociGiocati; }
    public void setGiociVinti(int giociVinti) { this.giociVinti = giociVinti; }
    public void setGiociPersi(int giociPersi) { this.giociPersi = giociPersi; }
    public void setGuadagnoTotale(double guadagnoTotale) { this.guadagnoTotale = guadagnoTotale; }

    // Metodi per saldo
    public boolean deductBalance(double amount) {
        if (saldo >= amount) {
            saldo -= amount;
            return true;
        }
        return false;
    }

    public void addBalance(double amount) {
        saldo += amount;
    }

    // Metodi statistiche
    public void recordWin(double gain) {
        giociVinti++;
        giociGiocati++;
        guadagnoTotale += gain;
    }

    public void recordLoss(double loss) {
        giociPersi++;
        giociGiocati++;
        guadagnoTotale -= loss;
    }

    public double getWinRate() {
        if (giociGiocati == 0) return 0;
        return (giociVinti * 100.0) / giociGiocati;
    }
}
