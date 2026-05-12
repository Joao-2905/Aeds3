package com.filmes.crud.model;

import java.util.ArrayList;
import java.util.List;

public class Film {

    private int ID;
    private String title;
    private String description;
    private int releaseDate;
    private String[] directors;

    // Mantido para compatibilidade com o projeto antigo
    private int genreID;

    // Novo campo para múltiplos gêneros
    private int[] genreIDs;

    private float rating;
    private int totalReviews;

    public Film() {
        this.title = "";
        this.description = "";
        this.directors = new String[0];
        this.genreID = 0;
        this.genreIDs = new int[0];
        this.rating = 0;
        this.totalReviews = 0;
    }

    public Film(String title, String description, int releaseDate, String[] directors, int genreID) {
        this.title = (title == null) ? "" : title;
        this.description = (description == null) ? "" : description;
        this.releaseDate = releaseDate;
        this.directors = normalizarDiretores(directors);
        this.genreID = genreID;
        this.genreIDs = (genreID > 0) ? new int[] { genreID } : new int[0];
        this.rating = 0;
        this.totalReviews = 0;
    }

    public Film(int ID, String title, String description, int releaseDate, String[] directors, int genreID, float rating, int totalReviews) {
        this.ID = ID;
        this.title = (title == null) ? "" : title;
        this.description = (description == null) ? "" : description;
        this.releaseDate = releaseDate;
        this.directors = normalizarDiretores(directors);
        this.genreID = genreID;
        this.genreIDs = (genreID > 0) ? new int[] { genreID } : new int[0];
        this.rating = rating;
        this.totalReviews = totalReviews;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getId() {
        return ID;
    }

    public void setId(int id) {
        this.ID = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = (title == null) ? "" : title;
    }

    public String getTitulo() {
        return title;
    }

    public void setTitulo(String titulo) {
        this.title = (titulo == null) ? "" : titulo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description;
    }

    public String getDescricao() {
        return description;
    }

    public void setDescricao(String descricao) {
        this.description = (descricao == null) ? "" : descricao;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(int releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDataLancamento() {
        if (releaseDate <= 0) return "";

        String data = String.valueOf(releaseDate);
        if (data.length() != 8) return data;

        return data.substring(0, 4) + "-"
             + data.substring(4, 6) + "-"
             + data.substring(6, 8);
    }

    public void setDataLancamento(String dataLancamento) {
        if (dataLancamento == null || dataLancamento.trim().isEmpty()) {
            this.releaseDate = 0;
            return;
        }

        try {
            String limpa = dataLancamento.replace("-", "").trim();
            this.releaseDate = Integer.parseInt(limpa);
        } catch (Exception e) {
            this.releaseDate = 0;
        }
    }

    public String[] getDirectors() {
        return directors;
    }

    public void setDirectors(String[] directors) {
        this.directors = normalizarDiretores(directors);
    }

    public String getDiretores() {
        if (directors == null || directors.length == 0) {
            return "";
        }

        return String.join(", ", directors);
    }

    public void setDiretores(String diretoresTexto) {
        if (diretoresTexto == null || diretoresTexto.trim().isEmpty()) {
            this.directors = new String[0];
            return;
        }

        this.directors = normalizarDiretores(diretoresTexto.split(","));
    }

    public int getGenreID() {
        return genreID;
    }

    public void setGenreID(int genreID) {
        this.genreID = genreID;

        if (genreID > 0 && (this.genreIDs == null || this.genreIDs.length == 0)) {
            this.genreIDs = new int[] { genreID };
        }
    }

    public int getGeneroID() {
        return genreID;
    }

    public void setGeneroID(int generoID) {
        setGenreID(generoID);
    }

    public int[] getGenreIDs() {
        return genreIDs;
    }

    public void setGenreIDs(int[] genreIDs) {
        this.genreIDs = normalizarGenreIDs(genreIDs);

        if (this.genreIDs.length > 0) {
            this.genreID = this.genreIDs[0];
        }
    }

    public int[] getGenerosIDs() {
        return genreIDs;
    }

    public void setGenerosIDs(int[] generosIDs) {
        setGenreIDs(generosIDs);
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getNotaMedia() {
        return rating;
    }

    public void setNotaMedia(float notaMedia) {
        this.rating = notaMedia;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    private String[] normalizarDiretores(String[] diretoresOriginais) {
        if (diretoresOriginais == null || diretoresOriginais.length == 0) {
            return new String[0];
        }

        List<String> lista = new ArrayList<>();

        for (String diretor : diretoresOriginais) {
            if (diretor == null) {
                continue;
            }

            String[] partes = diretor.split(",");

            for (String parte : partes) {
                String nome = (parte == null) ? "" : parte.trim();

                if (!nome.isEmpty()) {
                    lista.add(nome);
                }
            }
        }

        return lista.toArray(new String[0]);
    }

    private int[] normalizarGenreIDs(int[] idsOriginais) {
        if (idsOriginais == null || idsOriginais.length == 0) {
            return new int[0];
        }

        List<Integer> lista = new ArrayList<>();

        for (int id : idsOriginais) {
            if (id > 0 && !lista.contains(id)) {
                lista.add(id);
            }
        }

        int[] resultado = new int[lista.size()];

        for (int i = 0; i < lista.size(); i++) {
            resultado[i] = lista.get(i);
        }

        return resultado;
    }
}