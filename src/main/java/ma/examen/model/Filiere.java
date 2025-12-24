package ma.examen.model;

public class Filiere {
    private int id;
    private String code;
    private String nom;
    private String description;
    private int nombreEleves;

    public Filiere() {}

    public Filiere(int id, String code, String nom, String description) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getNombreEleves() { return nombreEleves; }
    public void setNombreEleves(int nombreEleves) { this.nombreEleves = nombreEleves; }

    @Override
    public String toString() { return code + " - " + nom; }
}