package com.filmes.crud.dao;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.index.HashExtensivel;
import com.filmes.crud.model.Film;
import com.filmes.crud.util.FileManeger;

public class FilmDAO {

    private final String FILE = "data/film.bin";
    private final String HASH_NAME = "film";
    private HashExtensivel indice;

    public FilmDAO() throws IOException {
        initFile();
        reconstruirIndice();
    }

    private void initFile() throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() == 0) {
            raf.writeInt(0);
        }

        raf.close();
    }

    private void reconstruirIndice() throws IOException {
        limparArquivosHash();

        indice = new HashExtensivel(2, HASH_NAME);

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                indice.inserir(id, pos);
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

    public int create(Film f) throws IOException {
        if (f == null) {
            throw new IOException("Filme inválido.");
        }

        prepararFilme(f);

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        byte[] titleBytes = f.getTitle().getBytes(StandardCharsets.UTF_8);
        byte[] descBytes = f.getDescription().getBytes(StandardCharsets.UTF_8);

        String[] directors = f.getDirectors();
        if (directors == null) {
            directors = new String[0];
        }

        int recordSize =
                1 + 4 +
                2 + titleBytes.length +
                2 + descBytes.length +
                4 +
                4 +
                4;

        for (String d : directors) {
            byte[] db = d.getBytes(StandardCharsets.UTF_8);
            recordSize += 2 + db.length;
        }

        recordSize += 4 + 4;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(newID);

        raf.writeShort(titleBytes.length);
        raf.write(titleBytes);

        raf.writeShort(descBytes.length);
        raf.write(descBytes);

        raf.writeInt(f.getReleaseDate());

        // Mantido como gênero principal para compatibilidade com o arquivo antigo
        raf.writeInt(f.getGenreID());

        raf.writeInt(directors.length);
        for (String d : directors) {
            byte[] db = d.getBytes(StandardCharsets.UTF_8);
            raf.writeShort(db.length);
            raf.write(db);
        }

        raf.writeFloat(f.getRating());
        raf.writeInt(f.getTotalReviews());

        raf.close();

        indice.inserir(newID, pos);

        return newID;
    }

    public Film read(int id) throws IOException {
        if (id <= 0) {
            return null;
        }

        Long pos = indice.buscar(id);

        if (pos != null) {
            RandomAccessFile raf = FileManeger.open(FILE);
            Film film = readAtPosition(raf, pos, id);
            raf.close();

            if (film != null) {
                return film;
            }
        }

        return readByScan(id);
    }

    private Film readByScan(int idBusca) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active && id == idBusca) {
                Film film = readAtPosition(raf, pos, idBusca);

                if (film != null) {
                    indice.inserir(idBusca, pos);
                }

                raf.close();
                return film;
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    private Film readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt();
        boolean active = raf.readBoolean();
        int tmpID = raf.readInt();

        if (!active || tmpID != expectedId) {
            return null;
        }

        short titleSize = raf.readShort();
        byte[] titleBytes = new byte[titleSize];
        raf.readFully(titleBytes);
        String title = new String(titleBytes, StandardCharsets.UTF_8);

        short descSize = raf.readShort();
        byte[] descBytes = new byte[descSize];
        raf.readFully(descBytes);
        String desc = new String(descBytes, StandardCharsets.UTF_8);

        int date = raf.readInt();
        int genreID = raf.readInt();

        int qtdDirectors = raf.readInt();
        String[] directors = new String[qtdDirectors];

        for (int i = 0; i < qtdDirectors; i++) {
            short size = raf.readShort();
            byte[] db = new byte[size];
            raf.readFully(db);
            directors[i] = new String(db, StandardCharsets.UTF_8);
        }

        float rating = raf.readFloat();
        int totalReviews = raf.readInt();

        Film film = new Film(tmpID, title, desc, date, directors, genreID, rating, totalReviews);
        film.setGenreID(genreID);

        return film;
    }

    public List<Film> readAllToList() throws IOException {
        List<Film> lista = new ArrayList<>();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                short titleSize = raf.readShort();
                byte[] titleBytes = new byte[titleSize];
                raf.readFully(titleBytes);
                String title = new String(titleBytes, StandardCharsets.UTF_8);

                short descSize = raf.readShort();
                byte[] descBytes = new byte[descSize];
                raf.readFully(descBytes);
                String desc = new String(descBytes, StandardCharsets.UTF_8);

                int date = raf.readInt();
                int genreID = raf.readInt();

                int qtdDirectors = raf.readInt();
                String[] directors = new String[qtdDirectors];

                for (int i = 0; i < qtdDirectors; i++) {
                    short size = raf.readShort();
                    byte[] db = new byte[size];
                    raf.readFully(db);
                    directors[i] = new String(db, StandardCharsets.UTF_8);
                }

                float rating = raf.readFloat();
                int totalReviews = raf.readInt();

                Film film = new Film(id, title, desc, date, directors, genreID, rating, totalReviews);
                film.setGenreID(genreID);

                lista.add(film);
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return lista;
    }

    public boolean delete(int id) throws IOException {
        Long pos = indice.buscar(id);

        if (pos == null) {
            Film existente = readByScan(id);
            if (existente == null) {
                return false;
            }

            pos = indice.buscar(id);
            if (pos == null) {
                return false;
            }
        }

        RandomAccessFile raf = FileManeger.open(FILE);

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

        removerRelacoesFilmGenre(id);

        return true;
    }

    private void removerRelacoesFilmGenre(int filmID) throws IOException {
        try {
            FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
            filmGenreDAO.deleteByFilmID(filmID);
        } catch (Exception e) {
            throw new IOException("Erro ao remover relações filme-gênero do filme " + filmID + ": " + e.getMessage(), e);
        }
    }

    public void listAll() throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        System.out.println("\n--- FILMES ---");

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int size = raf.readInt();
            boolean lapide = raf.readBoolean();
            int id = raf.readInt();

            if (lapide) {
                short titleSize = raf.readShort();
                byte[] titleBytes = new byte[titleSize];
                raf.readFully(titleBytes);
                String title = new String(titleBytes, StandardCharsets.UTF_8);

                short descSize = raf.readShort();
                byte[] descBytes = new byte[descSize];
                raf.readFully(descBytes);
                String desc = new String(descBytes, StandardCharsets.UTF_8);

                int d = raf.readInt();
                int genreID = raf.readInt();

                int qtdDirectors = raf.readInt();
                String[] directors = new String[qtdDirectors];

                for (int i = 0; i < qtdDirectors; i++) {
                    short dirSize = raf.readShort();
                    byte[] db = new byte[dirSize];
                    raf.readFully(db);
                    directors[i] = new String(db, StandardCharsets.UTF_8);
                }

                float rating = raf.readFloat();
                int totalReviews = raf.readInt();

                int ano = d / 10000;
                int mes = (d / 100) % 100;
                int dia = d % 100;

                String dataFormatada = String.format("%02d/%02d/%04d", dia, mes, ano);

                System.out.println("-----------------------");
                System.out.println("ID: " + id);
                System.out.println("Título: " + title);
                System.out.println("Descrição: " + desc);
                System.out.println("Data: " + dataFormatada);
                System.out.println("GenreID principal: " + genreID);

                if (directors != null && directors.length > 0) {
                    if (directors.length == 1) {
                        System.out.println("Diretor: " + directors[0]);
                    } else {
                        System.out.println("Diretores:");
                        for (int i = 0; i < directors.length; i++) {
                            System.out.println("  Diretor " + (i + 1) + ": " + directors[i]);
                        }
                    }
                } else {
                    System.out.println("Diretor: não informado");
                }

                System.out.println("Rating: " + rating);
                System.out.println("Total reviews: " + totalReviews);
            }

            raf.seek(pos + size + 4);
        }

        raf.close();
    }

    public boolean update(Film f) throws IOException {
        if (f == null || f.getID() <= 0) {
            return false;
        }

        prepararFilme(f);

        Long pos = indice.buscar(f.getID());

        if (pos == null) {
            Film existente = readByScan(f.getID());
            if (existente == null) {
                return false;
            }

            pos = indice.buscar(f.getID());
            if (pos == null) {
                return false;
            }
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(pos + 4);
        boolean active = raf.readBoolean();

        if (!active) {
            raf.close();
            return false;
        }

        raf.seek(pos + 4);
        raf.writeBoolean(false);

        raf.close();

        indice.remover(f.getID());

        long novaPos = createWithIdRetornandoPosicao(f);
        indice.inserir(f.getID(), novaPos);

        return true;
    }

    private long createWithIdRetornandoPosicao(Film f) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        prepararFilme(f);

        byte[] titleBytes = f.getTitle().getBytes(StandardCharsets.UTF_8);
        byte[] descBytes = f.getDescription().getBytes(StandardCharsets.UTF_8);

        String[] directors = f.getDirectors();
        if (directors == null) {
            directors = new String[0];
        }

        int recordSize =
                1 + 4 +
                2 + titleBytes.length +
                2 + descBytes.length +
                4 +
                4 +
                4;

        for (String d : directors) {
            byte[] db = d.getBytes(StandardCharsets.UTF_8);
            recordSize += 2 + db.length;
        }

        recordSize += 4 + 4;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(f.getID());

        raf.writeShort(titleBytes.length);
        raf.write(titleBytes);

        raf.writeShort(descBytes.length);
        raf.write(descBytes);

        raf.writeInt(f.getReleaseDate());

        // Mantido para compatibilidade: primeiro gênero selecionado
        raf.writeInt(f.getGenreID());

        raf.writeInt(directors.length);

        for (String d : directors) {
            byte[] db = d.getBytes(StandardCharsets.UTF_8);
            raf.writeShort(db.length);
            raf.write(db);
        }

        raf.writeFloat(f.getRating());
        raf.writeInt(f.getTotalReviews());

        raf.close();

        return pos;
    }

    public void exibirIndice() {
        indice.exibir("filme");
    }

    private void prepararFilme(Film f) {
        if (f == null) {
            return;
        }

        if (f.getTitle() == null) {
            f.setTitle("");
        } else {
            f.setTitle(f.getTitle().trim());
        }

        if (f.getDescription() == null) {
            f.setDescription("");
        } else {
            f.setDescription(f.getDescription().trim());
        }

        String[] directors = f.getDirectors();
        if (directors == null) {
            directors = new String[0];
        }

        List<String> lista = new ArrayList<>();

        for (String d : directors) {
            if (d == null) {
                continue;
            }

            String[] partes = d.split(",");

            for (String p : partes) {
                String nome = p == null ? "" : p.trim();

                if (!nome.isEmpty()) {
                    lista.add(nome);
                }
            }
        }

        f.setDirectors(lista.toArray(new String[0]));

        int[] genreIDs = f.getGenreIDs();

        if ((genreIDs == null || genreIDs.length == 0) && f.getGenreID() > 0) {
            f.setGenreIDs(new int[] { f.getGenreID() });
        }

        if (genreIDs != null && genreIDs.length > 0) {
            f.setGenreID(genreIDs[0]);
        }
    }
}