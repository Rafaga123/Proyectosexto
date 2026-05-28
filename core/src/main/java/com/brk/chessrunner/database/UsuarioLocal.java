package com.brk.chessrunner.database;

public class UsuarioLocal {
    private String id;
    private String alias;
    private String correo;
    private boolean sesionActiva;

    public UsuarioLocal(String id, String alias, String correo, boolean sesionActiva) {
        this.id = id;
        this.alias = alias;
        this.correo = correo;
        this.sesionActiva = sesionActiva;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isSesionActiva() {
        return sesionActiva;
    }

    public void setSesionActiva(boolean sesionActiva) {
        this.sesionActiva = sesionActiva;
    }
}
