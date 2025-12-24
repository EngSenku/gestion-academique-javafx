package ma.examen.model;

public class Eleve {
    private int id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private Filiere filiere;

    public Eleve() {}

    public Eleve(int id, String matricule, String nom, String prenom, String email, Filiere filiere) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.filiere = filiere;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Filiere getFiliere() { return filiere; }
    public void setFiliere(Filiere filiere) { this.filiere = filiere; }
}