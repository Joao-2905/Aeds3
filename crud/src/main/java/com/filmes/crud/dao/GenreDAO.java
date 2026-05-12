package com.filmes.crud.dao;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.index.HashExtensivel;
import com.filmes.crud.model.Genre;
import com.filmes.crud.util.FileManeger;

public class GenreDAO {

    private final String FILE = "data/genre.bin";
    private final String HASH_NAME = "genre";
    private HashExtensivel indice;

    public GenreDAO() throws IOException {
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

    public int create(Genre genre) throws IOException {
        if (genre == null || genre.getName() == null) {
            throw new IOException("Gênero inválido.");
        }

        prepararGenre(genre);

        if (genre.getName().isEmpty()) {
            throw new IOException("Nome do gênero não pode ser vazio.");
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        byte[] nameBytes = genre.getName().getBytes(StandardCharsets.UTF_8);

        int recordSize =
                1 + 4 +
                2 + nameBytes.length;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(newID);

        raf.writeShort(nameBytes.length);
        raf.write(nameBytes);

        raf.close();

        indice.inserir(newID, pos);

        return newID;
    }

    public Genre read(int id) throws IOException {
        if (id <= 0) return null;

        Long pos = indice.buscar(id);

        if (pos != null) {
            RandomAccessFile raf = FileManeger.open(FILE);
            Genre genre = readAtPosition(raf, pos, id);
            raf.close();

            if (genre != null) return genre;
        }

        return readByScan(id);
    }

    private Genre readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt();
        boolean active = raf.readBoolean();
        int id = raf.readInt();

        if (!active || id != expectedId) return null;

        short size = raf.readShort();
        byte[] nameBytes = new byte[size];
        raf.readFully(nameBytes);
        String name = new String(nameBytes, StandardCharsets.UTF_8);

        return new Genre(id, name);
    }

    private Genre readByScan(int idBusca) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active && id == idBusca) {
                Genre genre = readAtPosition(raf, pos, idBusca);

                if (genre != null) {
                    indice.inserir(idBusca, pos);
                }

                raf.close();
                return genre;
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    public Genre readByName(String nameBusca) throws IOException {
        if (nameBusca == null || nameBusca.trim().isEmpty()) {
            return null;
        }

        String buscaNormalizada = nameBusca.trim();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                short size = raf.readShort();
                byte[] nameBytes = new byte[size];
                raf.readFully(nameBytes);
                String name = new String(nameBytes, StandardCharsets.UTF_8);

                if (name.equalsIgnoreCase(buscaNormalizada)) {
                    indice.inserir(id, pos);
                    raf.close();
                    return new Genre(id, name);
                }
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    public List<Genre> readAllToList() throws IOException {
        List<Genre> lista = new ArrayList<>();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                short size = raf.readShort();
                byte[] nameBytes = new byte[size];
                raf.readFully(nameBytes);
                String name = new String(nameBytes, StandardCharsets.UTF_8);

                lista.add(new Genre(id, name));
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return lista;
    }

    public boolean update(Genre genre) throws IOException {
        if (genre == null || genre.getID() <= 0) return false;

        prepararGenre(genre);

        if (genre.getName() == null || genre.getName().isEmpty()) {
            throw new IOException("Nome do gênero não pode ser vazio.");
        }

        Long pos = indice.buscar(genre.getID());

        if (pos == null) {
            Genre existente = readByScan(genre.getID());
            if (existente == null) {
                return false;
            }

            pos = indice.buscar(genre.getID());
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

        indice.remover(genre.getID());

        long novaPos = createWithIdRetornandoPosicao(genre);
        indice.inserir(genre.getID(), novaPos);

        return true;
    }

    private long createWithIdRetornandoPosicao(Genre genre) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        prepararGenre(genre);

        byte[] nameBytes = genre.getName().getBytes(StandardCharsets.UTF_8);

        int recordSize =
                1 + 4 +
                2 + nameBytes.length;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(genre.getID());

        raf.writeShort(nameBytes.length);
        raf.write(nameBytes);

        raf.close();
        return pos;
    }

    public boolean delete(int id) throws IOException {
        Long pos = indice.buscar(id);

        if (pos == null) {
            Genre existente = readByScan(id);
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

        // Remove também as relações N:N desse gênero na tabela intermediária
        removerRelacoesFilmGenre(id);

        return true;
    }

    private void removerRelacoesFilmGenre(int genreID) throws IOException {
        try {
            FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
            filmGenreDAO.deleteByGenreID(genreID);
        } catch (Exception e) {
            throw new IOException("Erro ao remover relações filme-gênero do gênero " + genreID + ": " + e.getMessage(), e);
        }
    }

    private void prepararGenre(Genre genre) {
        if (genre == null) {
            return;
        }

        if (genre.getName() == null) {
            genre.setName("");
        } else {
            genre.setName(genre.getName().trim());
        }
    }
}