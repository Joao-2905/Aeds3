package main.java.CRUD.controller;

import main.java.CRUD.dao.FilmDAO;
import main.java.CRUD.model.Film;

public class FilmController {

    private FilmDAO filmDao;

    public FilmController() throws Exception {
        filmDao = new FilmDAO();
    }

    public int createFilm(String title, String description, int releaseDate, String[] directors) throws Exception {

        if (title == null || title.trim().isEmpty())
            throw new Exception("Título não pode ser vazio.");

        if (description == null || description.trim().isEmpty())
            throw new Exception("Descrição não pode ser vazia.");

        if (String.valueOf(releaseDate).length() != 8)
            throw new Exception("A data deve estar no formato AAAAMMDD.");

        if (!dataValida(releaseDate))
            throw new Exception("Data inválida.");

        if (directors == null || directors.length == 0)
            throw new Exception("O filme precisa ter pelo menos um diretor.");

        Film f = new Film(title, description, releaseDate, directors);

        return filmDao.create(f);
    }

    public Film readFilm(int id) throws Exception {

        if (id <= 0)
            throw new Exception("ID inválido.");

        return filmDao.read(id);
    }

    public boolean deleteFilm(int id) throws Exception {

        if (id <= 0)
            throw new Exception("ID inválido.");

        return filmDao.delete(id);
    }

    public boolean updateFilm(Film f) throws Exception {

        if (f == null)
            throw new Exception("Filme inválido.");

        if (f.getID() <= 0)
            throw new Exception("ID inválido.");

        if (f.getTitle() == null || f.getTitle().trim().isEmpty())
            throw new Exception("Título inválido.");

        if (f.getDescription() == null || f.getDescription().trim().isEmpty())
            throw new Exception("Descrição inválida.");

        if (String.valueOf(f.getReleaseDate()).length() != 8)
            throw new Exception("A data deve estar no formato AAAAMMDD.");

        if (!dataValida(f.getReleaseDate()))
            throw new Exception("Data inválida.");

        if (f.getDirectors() == null || f.getDirectors().length == 0)
            throw new Exception("O filme precisa ter pelo menos um diretor.");

        return filmDao.update(f);
    }

    public void listFilms() throws Exception {
        filmDao.listAll();
    }

    public void exibirIndice() throws Exception {
        filmDao.exibirIndice();
    }

    private boolean dataValida(int data) {

        int ano = data / 10000;
        int mes = (data / 100) % 100;
        int dia = data % 100;

        if (ano < 1800 || ano > 3000)
            return false;

        if (mes < 1 || mes > 12)
            return false;

        int[] diasMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        // ano bissexto
        if ((ano % 4 == 0 && ano % 100 != 0) || (ano % 400 == 0)) {
            diasMes[1] = 29;
        }

        return dia >= 1 && dia <= diasMes[mes - 1];
    }
}