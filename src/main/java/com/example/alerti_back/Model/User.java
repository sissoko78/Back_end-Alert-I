package com.example.alerti_back.Model;

public class User {
    private int id;
    private String prenom;
    private String nom;
    private String email;
    private String password;
    private String role;
    private String localite;
    private int num_tel;

    public User() {
    }

    public User(int id, String prenom, String nom, String email, String password, String role, String localite, int num_tel) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.role = role;
        this.localite = localite;
        this.num_tel = num_tel;
    }
    public int getId() {
        return id;
    }
    public String getPrenom() {
        return prenom;
    }
    public String getNom() {
        return nom;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getRole() {
        return role;
    }
    public String getLocalite() {
        return localite;
    }
    public int getNum_tel() {
        return num_tel;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setLocalite(String localite) {
        this.localite = localite;
    }
    public void setNum_tel(int num_tel) {
        this.num_tel = num_tel;
    }
    
}
