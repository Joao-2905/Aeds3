package CRUD.dao;

import CRUD.model.*;
import CRUD.util.*;
import CRUD.index.HashExtensivel;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FilmDAO {

    private final String FILE = "data/film.bin";
    private HashExtensivel indice;

    public FilmDAO() throws IOException {
        initFile();
        indice = new HashExtensivel(2);
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
        indice = new HashExtensivel(2);

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4); // pula header

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

    public int create(Film f) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        byte[] titleBytes = f.getTitle().getBytes(StandardCharsets.UTF_8);
        byte[] descBytes = f.getDescription().getBytes(StandardCharsets.UTF_8);

        String[] directors = f.getDirectors();

        int recordSize =
                1 + 4 +
                2 + titleBytes.length +
                2 + descBytes.length +
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

        Long pos = indice.buscar(id);
        if (pos == null) {
            return null;
        }

        RandomAccessFile raf = FileManeger.open(FILE);
        Film film = readAtPosition(raf, pos, id);
        raf.close();

        return film;
    }

    private Film readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt(); // recordSize
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

        return new Film(tmpID, title, desc, date, directors, rating, totalReviews);
    }

    public boolean delete(int id) throws IOException {

        Long pos = indice.buscar(id);
        if (pos == null) {
            return false;
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(pos + 4); // pula tamanho do registro
        boolean active = raf.readBoolean();

        if (!active) {
            raf.close();
            return false;
        }

        raf.seek(pos + 4);
        raf.writeBoolean(false);

        raf.close();

        indice.remover(id);
        return true;
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
                Film f = read(id);

                if (f != null) {

                    int d = f.getReleaseDate();
                    int ano = d / 10000;
                    int mes = (d / 100) % 100;
                    int dia = d % 100;

                    String dataFormatada = dia + "/" + mes + "/" + ano;

                    System.out.println("-----------------------");
                    System.out.println("ID: " + f.getID());
                    System.out.println("Título: " + f.getTitle());
                    System.out.println("Descrição: " + f.getDescription());
                    System.out.println("Data: " + dataFormatada);
                    System.out.println("Rating: " + f.getRating());
                    System.out.println("Total reviews: " + f.getTotalReviews());
                }
            }

            raf.seek(pos + size + 4);
        }

        raf.close();
    }

    public boolean update(Film f) throws IOException {

        Long pos = indice.buscar(f.getID());
        if (pos == null) {
            return false;
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(pos + 4); // pula tamanho do registro
        boolean active = raf.readBoolean();

        if (!active) {
            raf.close();
            return false;
        }

        raf.seek(pos + 4); // posição da lápide
        raf.writeBoolean(false);

        raf.close();

        long novaPos = createWithIdRetornandoPosicao(f);
        indice.inserir(f.getID(), novaPos);

        return true;
    }

    private long createWithIdRetornandoPosicao(Film f) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        byte[] titleBytes = f.getTitle().getBytes(StandardCharsets.UTF_8);
        byte[] descBytes = f.getDescription().getBytes(StandardCharsets.UTF_8);

        String[] directors = f.getDirectors();

        int recordSize =
                1 + 4 +
                2 + titleBytes.length +
                2 + descBytes.length +
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
}