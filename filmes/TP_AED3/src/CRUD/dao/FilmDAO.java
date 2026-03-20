package CRUD.dao;

import CRUD.model.*;
import CRUD.util.*;

import java.io.*;

public class FilmDAO {

    private final String FILE = "data/film.bin";

    public FilmDAO() throws IOException {
        initFile();
    }

    private void initFile() throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() == 0) {
            raf.writeInt(0);
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

        byte[] titleBytes = f.getTitle().getBytes("UTF-8");
        byte[] descBytes = f.getDescription().getBytes("UTF-8");

        String[] directors = f.getDirectors();

        int recordSize =
                1 + 4 +
                2 + titleBytes.length +
                2 + descBytes.length +
                4 +
                4;

        for (String d : directors) {
            byte[] db = d.getBytes("UTF-8");
            recordSize += 2 + db.length;
        }

        recordSize += 4 + 4;

        raf.seek(raf.length());

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

            byte[] db = d.getBytes("UTF-8");

            raf.writeShort(db.length);
            raf.write(db);
        }

        raf.writeFloat(f.getRating());
        raf.writeInt(f.getTotalReviews());

        raf.close();

        return newID;
    }

    public Film read(int id) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();

            boolean active = raf.readBoolean();
            int tmpID = raf.readInt();

            if (active && tmpID == id) {

                short titleSize = raf.readShort();
                byte[] titleBytes = new byte[titleSize];
                raf.readFully(titleBytes);
                String title = new String(titleBytes, "UTF-8");

                short descSize = raf.readShort();
                byte[] descBytes = new byte[descSize];
                raf.readFully(descBytes);
                String desc = new String(descBytes, "UTF-8");

                int date = raf.readInt();

                int qtdDirectors = raf.readInt();

                String[] directors = new String[qtdDirectors];

                for (int i = 0; i < qtdDirectors; i++) {

                    short size = raf.readShort();

                    byte[] db = new byte[size];
                    raf.readFully(db);

                    directors[i] = new String(db, "UTF-8");
                }

                float rating = raf.readFloat();
                int totalReviews = raf.readInt();

                raf.close();

                return new Film(tmpID, title, desc, date, directors, rating, totalReviews);
            }

            raf.seek(pos + recordSize + 4);
        }

        raf.close();
        return null;
    }

    public boolean delete(int id) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();

            boolean active = raf.readBoolean();
            int tmpID = raf.readInt();

            if (active && tmpID == id) {

                raf.seek(pos + 4);
                raf.writeBoolean(false);

                raf.close();
                return true;
            }

            raf.seek(pos + recordSize + 4);
        }

        raf.close();
        return false;
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

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(4); // pula header

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();

            boolean active = raf.readBoolean();
            int tmpID = raf.readInt();

            if (active && tmpID == f.getID()) {

                raf.seek(pos + 4); // posição da lápide
                raf.writeBoolean(false);

                raf.close();

                createWithId(f);

                return true;
            }

            raf.seek(pos + recordSize + 4);
        }

        raf.close();
        return false;
    }
    
    private void createWithId(Film f) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(raf.length());

        byte[] titleBytes = f.getTitle().getBytes("UTF-8");
        byte[] descBytes = f.getDescription().getBytes("UTF-8");

        String[] directors = f.getDirectors();

        int recordSize =
                1 + 4 +
                2 + titleBytes.length +
                2 + descBytes.length +
                4 +
                4;

        for (String d : directors) {
            byte[] db = d.getBytes("UTF-8");
            recordSize += 2 + db.length;
        }

        recordSize += 4 + 4;

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

            byte[] db = d.getBytes("UTF-8");

            raf.writeShort(db.length);
            raf.write(db);
        }

        raf.writeFloat(f.getRating());
        raf.writeInt(f.getTotalReviews());

        raf.close();
    }
}