package com.filmes.crud.model;

public class User {

    private int id;
    private String username;
    private String email;
    private String password;
    private boolean administrator;

    public User() {
        this.username = "";
        this.email = "";
        this.password = "";
        this.administrator = false;
    }

    public User(int id, String username, String email, String password, boolean administrator) {
        this.id = id;
        this.username = normalizar(username);
        this.email = normalizarEmail(email);
        this.password = normalizar(password);
        this.administrator = administrator;
    }

    public User(String username, String email, String password, boolean administrator) {
        this.username = normalizar(username);
        this.email = normalizarEmail(email);
        this.password = normalizar(password);
        this.administrator = administrator;
    }

    public int getID() {
        return id;
    }

    public int getId() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalizar(username);
    }

    // alias opcional
    public String getUsuario() {
        return username;
    }

    public void setUsuario(String usuario) {
        this.username = normalizar(usuario);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizarEmail(email);
    }

    // alias opcional
    public String getSenha() {
        return password;
    }

    public void setSenha(String senha) {
        this.password = normalizar(senha);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = normalizar(password);
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public boolean getAdministrator() {
        return administrator;
    }

    public boolean isAdministrador() {
        return administrator;
    }

    public boolean getAdministrador() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public void setAdministrador(boolean administrador) {
        this.administrator = administrador;
    }


    private String normalizar(String valor) {
        if (valor == null) return "";
        return valor.trim();
    }

    private String normalizarEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }
}