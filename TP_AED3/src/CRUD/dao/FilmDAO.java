package CRUD.dao;

import CRUD.util.FileManeger;
import CRUD.model.Film;
import CRUD.index.HashExtensivel;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class FilmDAO {

    private final String FILE = "data/film.bin";
    private HashExtensivel indice;

    public FilmDAO() throws IOException {
        initFile();
        indice = new HashExtensivel(2); // capacidade de 2 por bucket
        reconstruirIndice();
    }

    private void initFile() throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            if (raf.length() == 0) {
                raf.writeInt(0); // último ID
            }
        }
    }

    private void reconstruirIndice() throws IOException {
        indice = new HashExtensivel(2);

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
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
        }
    }

    public int create(Film f) throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {

            raf.seek(0);
            int lastID = raf.readInt();
            int newID = lastID + 1;

            raf.seek(0);
            raf.writeInt(newID);

            byte[] titleBytes = f.getTitle().getBytes(StandardCharsets.UTF_8);
            short titleSize = (short) titleBytes.length;

            byte[] directorBytes = f.getDirector().getBytes(StandardCharsets.UTF_8);
            short directorSize = (short) directorBytes.length;

            byte[] descriptionBytes = f.getDescription().getBytes(StandardCharsets.UTF_8);
            short descriptionSize = (short) descriptionBytes.length;

            int[] genres = f.getGenreID();
            int numGenres = genres.length;

            int recordSize = 1 + 4 + 2 + titleSize + 2 + directorSize + 2 + descriptionSize + 4 + (numGenres * 4);

            long pos = raf.length();
            raf.seek(pos);

            raf.writeInt(recordSize);
            raf.writeBoolean(true);
            raf.writeInt(newID);

            raf.writeShort(titleSize);
            raf.write(titleBytes);

            raf.writeShort(directorSize);
            raf.write(directorBytes);

            raf.writeShort(descriptionSize);
            raf.write(descriptionBytes);

            raf.writeInt(numGenres);
            for (int idGenero : genres) {
                raf.writeInt(idGenero);
            }

            indice.inserir(newID, pos);
            return newID;
        }
    }

    public Film read(int id) throws IOException {
        Long pos = indice.buscar(id);
        if (pos == null) {
            return null;
        }

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            return readAtPosition(raf, pos, id);
        }
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

        short directorSize = raf.readShort();
        byte[] directorBytes = new byte[directorSize];
        raf.readFully(directorBytes);
        String director = new String(directorBytes, StandardCharsets.UTF_8);

        short descriptionSize = raf.readShort();
        byte[] descriptionBytes = new byte[descriptionSize];
        raf.readFully(descriptionBytes);
        String description = new String(descriptionBytes, StandardCharsets.UTF_8);

        int numGenres = raf.readInt();
        int[] genres = new int[numGenres];
        for (int i = 0; i < numGenres; i++) {
            genres[i] = raf.readInt();
        }

        return new Film(tmpID, title, director, description, genres);
    }

    public boolean update(Film f) throws IOException {
        Long pos = indice.buscar(f.getID());
        if (pos == null) {
            return false;
        }

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            raf.seek(pos + 4); // pula o tamanho do registro
            boolean active = raf.readBoolean();

            if (!active) {
                return false;
            }

            raf.seek(pos + 4);
            raf.writeBoolean(false);
        }

        long novaPos = createWithIdRetornandoPosicao(f);
        indice.inserir(f.getID(), novaPos);
        return true;
    }

    private long createWithIdRetornandoPosicao(Film f) throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {

            byte[] titleBytes = f.getTitle().getBytes(StandardCharsets.UTF_8);
            short titleSize = (short) titleBytes.length;

            byte[] directorBytes = f.getDirector().getBytes(StandardCharsets.UTF_8);
            short directorSize = (short) directorBytes.length;

            byte[] descriptionBytes = f.getDescription().getBytes(StandardCharsets.UTF_8);
            short descriptionSize = (short) descriptionBytes.length;

            int[] genres = f.getGenreID();
            int numGenres = genres.length;

            int recordSize = 1 + 4 + 2 + titleSize + 2 + directorSize + 2 + descriptionSize + 4 + (numGenres * 4);

            long pos = raf.length();
            raf.seek(pos);

            raf.writeInt(recordSize);
            raf.writeBoolean(true);
            raf.writeInt(f.getID());

            raf.writeShort(titleSize);
            raf.write(titleBytes);

            raf.writeShort(directorSize);
            raf.write(directorBytes);

            raf.writeShort(descriptionSize);
            raf.write(descriptionBytes);

            raf.writeInt(numGenres);
            for (int idGenero : genres) {
                raf.writeInt(idGenero);
            }

            return pos;
        }
    }

    public boolean delete(int id) throws IOException {
        Long pos = indice.buscar(id);
        if (pos == null) {
            return false;
        }

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            raf.seek(pos + 4); // pula tamanho
            boolean active = raf.readBoolean();

            if (!active) {
                return false;
            }

            raf.seek(pos + 4);
            raf.writeBoolean(false);
        }

        indice.remover(id);
        return true;
    }

    public void listAll() throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            raf.seek(4); // pula header

            System.out.println("\n--- Filmes ---");

            while (raf.getFilePointer() < raf.length()) {
                long pos = raf.getFilePointer();

                int recordSize = raf.readInt();
                boolean active = raf.readBoolean();

                if (active) {
                    int ID = raf.readInt();

                    short titleSize = raf.readShort();
                    byte[] titleBytes = new byte[titleSize];
                    raf.readFully(titleBytes);
                    String title = new String(titleBytes, StandardCharsets.UTF_8);

                    short directorSize = raf.readShort();
                    byte[] directorBytes = new byte[directorSize];
                    raf.readFully(directorBytes);
                    String director = new String(directorBytes, StandardCharsets.UTF_8);

                    short descriptionSize = raf.readShort();
                    byte[] descriptionBytes = new byte[descriptionSize];
                    raf.readFully(descriptionBytes);
                    String description = new String(descriptionBytes, StandardCharsets.UTF_8);

                    int numGenres = raf.readInt();
                    int[] genres = new int[numGenres];
                    for (int i = 0; i < numGenres; i++) {
                        genres[i] = raf.readInt();
                    }

                    System.out.print(
                        "ID: " + ID +
                        " | Título: " + title +
                        " | Diretor: " + director +
                        " | Descrição: " + description +
                        " | Gênero(os) (IDs): "
                    );

                    for (int gID : genres) {
                        System.out.print(gID + " ");
                    }
                    System.out.println();
                }

                raf.seek(pos + 4L + recordSize);
            }
        }
    }

    public void exibirIndice() {
        indice.exibir();
    }
}