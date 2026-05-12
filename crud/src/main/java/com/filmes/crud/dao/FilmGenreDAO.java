package com.filmes.crud.dao;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.index.HashExtensivel;
import com.filmes.crud.index.ListaInvertidaDAO;
import com.filmes.crud.model.FilmGenre;
import com.filmes.crud.util.FileManeger;

public class FilmGenreDAO {

    private final String FILE = "data/film_genre.bin";
    private final String HASH_NAME = "film_genre";

    private HashExtensivel indice;
    private ListaInvertidaDAO indicePorFilme;
    private ListaInvertidaDAO indicePorGenero;

    public FilmGenreDAO() throws IOException {
        initFile();
        reconstruirIndices();
    }

    private void initFile() throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() == 0) {
            raf.writeInt(0);
        }

        raf.close();
    }

    private void reconstruirIndices() throws IOException {
        limparArquivosHash();
        limparArquivosListaInvertida();

        indice = new HashExtensivel(2, HASH_NAME);

        indicePorFilme = new ListaInvertidaDAO("film_genre_filme");
        indicePorGenero = new ListaInvertidaDAO("film_genre_genero");

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();
            int filmID = raf.readInt();
            int genreID = raf.readInt();

            if (active) {
                indice.inserir(id, pos);
                indicePorFilme.inserir(filmID, id);
                indicePorGenero.inserir(genreID, id);
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
    }

    private void limparArquivosHash() {
        File diretorio = new File("data/" + HASH_NAME + "_diretorio.bin");
        File buckets = new File("data/" + HASH_NAME + "_buckets.bin");

        if (diretorio.exists()) {
            diretorio.delete();
        }

        if (buckets.exists()) {
            buckets.delete();
        }
    }

    private void limparArquivosListaInvertida() {
        File porFilme = new File("data/film_genre_filme_lista_invertida.bin");
        File porGenero = new File("data/film_genre_genero_lista_invertida.bin");

        if (porFilme.exists()) {
            porFilme.delete();
        }

        if (porGenero.exists()) {
            porGenero.delete();
        }
    }

    public int create(FilmGenre fg) throws Exception {
        validarRelacaoBasica(fg);

        FilmGenre existente = readByFilmAndGenre(fg.getFilmID(), fg.getGenreID());

        if (existente != null) {
            return existente.getID();
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        int recordSize =
                1 +
                4 +
                4 +
                4;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(newID);
        raf.writeInt(fg.getFilmID());
        raf.writeInt(fg.getGenreID());

        raf.close();

        indice.inserir(newID, pos);
        indicePorFilme.inserir(fg.getFilmID(), newID);
        indicePorGenero.inserir(fg.getGenreID(), newID);

        return newID;
    }

    public FilmGenre read(int idBusca) throws IOException {
        if (idBusca <= 0) {
            return null;
        }

        Long pos = indice.buscar(idBusca);

        if (pos != null) {
            RandomAccessFile raf = FileManeger.open(FILE);
            FilmGenre fg = readAtPosition(raf, pos, idBusca);
            raf.close();

            if (fg != null) {
                return fg;
            }
        }

        return readByScan(idBusca);
    }

    private FilmGenre readByScan(int idBusca) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();
            int filmID = raf.readInt();
            int genreID = raf.readInt();

            if (active && id == idBusca) {
                indice.inserir(idBusca, pos);
                raf.close();
                return new FilmGenre(id, filmID, genreID);
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    private FilmGenre readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt();
        boolean active = raf.readBoolean();
        int id = raf.readInt();

        if (!active || id != expectedId) {
            return null;
        }

        int filmID = raf.readInt();
        int genreID = raf.readInt();

        return new FilmGenre(id, filmID, genreID);
    }

    public List<FilmGenre> readAllToList() throws IOException {
        List<FilmGenre> lista = new ArrayList<>();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();
            int filmID = raf.readInt();
            int genreID = raf.readInt();

            if (active) {
                lista.add(new FilmGenre(id, filmID, genreID));
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return lista;
    }

    public List<FilmGenre> readByFilmID(int filmID) throws IOException {
        List<FilmGenre> lista = new ArrayList<>();

        if (filmID <= 0) {
            return lista;
        }

        List<Integer> ids = indicePorFilme.buscar(filmID);

        for (Integer id : ids) {
            FilmGenre fg = read(id);

            if (fg != null && fg.getFilmID() == filmID) {
                lista.add(fg);
            }
        }

        return lista;
    }

    public List<FilmGenre> readByGenreID(int genreID) throws IOException {
        List<FilmGenre> lista = new ArrayList<>();

        if (genreID <= 0) {
            return lista;
        }

        List<Integer> ids = indicePorGenero.buscar(genreID);

        for (Integer id : ids) {
            FilmGenre fg = read(id);

            if (fg != null && fg.getGenreID() == genreID) {
                lista.add(fg);
            }
        }

        return lista;
    }

    public FilmGenre readByFilmAndGenre(int filmID, int genreID) throws IOException {
        if (filmID <= 0 || genreID <= 0) {
            return null;
        }

        List<FilmGenre> relacoes = readByFilmID(filmID);

        for (FilmGenre fg : relacoes) {
            if (fg != null && fg.getGenreID() == genreID) {
                return fg;
            }
        }

        return null;
    }

    public boolean delete(int id) throws IOException {
        if (id <= 0) {
            return false;
        }

        Long pos = indice.buscar(id);

        if (pos == null) {
            FilmGenre existente = readByScan(id);

            if (existente == null) {
                return false;
            }

            pos = indice.buscar(id);

            if (pos == null) {
                return false;
            }
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        FilmGenre fg = readAtPosition(raf, pos, id);

        if (fg == null) {
            raf.close();
            return false;
        }

        raf.seek(pos + 4);
        boolean active = raf.readBoolean();

        if (!active) {
            raf.close();
            return false;
        }

        raf.seek(pos + 4);
        raf.writeBoolean(false);

        raf.close();

        indice.remover(id);
        indicePorFilme.remover(fg.getFilmID(), id);
        indicePorGenero.remover(fg.getGenreID(), id);

        return true;
    }

    public boolean deleteByFilmID(int filmID) throws IOException {
        if (filmID <= 0) {
            return false;
        }

        boolean removeu = false;

        List<FilmGenre> relacoes = new ArrayList<>(readByFilmID(filmID));

        for (FilmGenre fg : relacoes) {
            if (fg != null) {
                boolean ok = delete(fg.getID());

                if (ok) {
                    removeu = true;
                }
            }
        }

        return removeu;
    }

    public boolean deleteByGenreID(int genreID) throws IOException {
        if (genreID <= 0) {
            return false;
        }

        boolean removeu = false;

        List<FilmGenre> relacoes = new ArrayList<>(readByGenreID(genreID));

        for (FilmGenre fg : relacoes) {
            if (fg != null) {
                boolean ok = delete(fg.getID());

                if (ok) {
                    removeu = true;
                }
            }
        }

        return removeu;
    }

    public boolean deleteByFilmAndGenre(int filmID, int genreID) throws IOException {
        FilmGenre fg = readByFilmAndGenre(filmID, genreID);

        if (fg == null) {
            return false;
        }

        return delete(fg.getID());
    }

    public boolean update(FilmGenre fg) throws Exception {
        validarRelacaoBasica(fg);

        if (fg.getID() <= 0) {
            throw new Exception("ID da relação inválido.");
        }

        FilmGenre antiga = read(fg.getID());

        if (antiga == null) {
            return false;
        }

        FilmGenre duplicada = readByFilmAndGenre(fg.getFilmID(), fg.getGenreID());

        if (duplicada != null && duplicada.getID() != fg.getID()) {
            return true;
        }

        boolean removeu = delete(fg.getID());

        if (!removeu) {
            return false;
        }

        long novaPos = createWithIdRetornandoPosicao(fg);

        indice.inserir(fg.getID(), novaPos);
        indicePorFilme.inserir(fg.getFilmID(), fg.getID());
        indicePorGenero.inserir(fg.getGenreID(), fg.getID());

        return true;
    }

    private long createWithIdRetornandoPosicao(FilmGenre fg) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        int recordSize =
                1 +
                4 +
                4 +
                4;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(fg.getID());
        raf.writeInt(fg.getFilmID());
        raf.writeInt(fg.getGenreID());

        raf.close();

        return pos;
    }

    public List<Integer> buscarIdsRelacoesPorFilme(int filmID) throws IOException {
        if (filmID <= 0) {
            return new ArrayList<>();
        }

        return indicePorFilme.buscar(filmID);
    }

    public List<Integer> buscarIdsRelacoesPorGenero(int genreID) throws IOException {
        if (genreID <= 0) {
            return new ArrayList<>();
        }

        return indicePorGenero.buscar(genreID);
    }

    public void reconstruirListaInvertida() throws IOException {
        reconstruirIndices();
    }

    private void validarRelacaoBasica(FilmGenre fg) throws Exception {
        if (fg == null) {
            throw new Exception("Relação filme-gênero inválida.");
        }

        if (fg.getFilmID() <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        if (fg.getGenreID() <= 0) {
            throw new Exception("ID do gênero inválido.");
        }
    }
}