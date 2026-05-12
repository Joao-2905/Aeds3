package CRUD.dao;

import CRUD.model.*;
import CRUD.util.*;
import CRUD.index.HashExtensivel;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class GenreDAO {

    private final String FILE = "data/genre.bin";
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

    // =============================
    // RECONSTRUIR ÍNDICE
    // =============================
    private void reconstruirIndice() throws IOException {

        indice = new HashExtensivel(2, "genero");

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

    // =============================
    // CREATE
    // =============================
    public int create(Genre g) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        byte[] nameBytes = g.getName().getBytes(StandardCharsets.UTF_8);

        int recordSize = 1 + 4 + 2 + nameBytes.length;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);

        raf.writeBoolean(true);
        raf.writeInt(newID);

        raf.writeShort(nameBytes.length);
        raf.write(nameBytes);

        raf.close();

        indice.inserir(newID, pos); // 🔥 INDEX

        return newID;
    }

    // =============================
    // READ (USANDO HASH)
    // =============================
    public Genre read(int id) throws IOException {

        Long pos = indice.buscar(id);

        if (pos == null) {
            return null;
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(pos);

        raf.readInt(); // recordSize
        boolean active = raf.readBoolean();
        int tmpID = raf.readInt();

        if (!active || tmpID != id) {
            raf.close();
            return null;
        }

        short nameSize = raf.readShort();
        byte[] nameBytes = new byte[nameSize];
        raf.readFully(nameBytes);

        raf.close();

        return new Genre(tmpID, new String(nameBytes, StandardCharsets.UTF_8));
    }

    // =============================
    // DELETE
    // =============================
    public boolean delete(int id) throws IOException {

        Long pos = indice.buscar(id);

        if (pos == null) return false;

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

        indice.remover(id); // 🔥 INDEX

        return true;
    }

    // =============================
    // UPDATE
    // =============================
    public boolean update(Genre g) throws IOException {

        Long pos = indice.buscar(g.getID());

        if (pos == null) return false;

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(pos + 4);
        boolean active = raf.readBoolean();

        if (!active) {
            raf.close();
            return false;
        }

        // marca como removido
        raf.seek(pos + 4);
        raf.writeBoolean(false);

        raf.close();

        long novaPos = createWithIdRetornandoPosicao(g);

        indice.inserir(g.getID(), novaPos); // 🔥 INDEX

        return true;
    }

    private long createWithIdRetornandoPosicao(Genre g) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        byte[] nameBytes = g.getName().getBytes(StandardCharsets.UTF_8);

        int recordSize = 1 + 4 + 2 + nameBytes.length;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);

        raf.writeBoolean(true);
        raf.writeInt(g.getID());

        raf.writeShort(nameBytes.length);
        raf.write(nameBytes);

        raf.close();

        return pos;
    }

    // =============================
    // LIST
    // =============================
    public void listAll() throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        System.out.println("\n--- GÊNEROS ---");

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int size = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {

                short nameSize = raf.readShort();
                byte[] nameBytes = new byte[nameSize];
                raf.readFully(nameBytes);

                String name = new String(nameBytes, StandardCharsets.UTF_8);

                System.out.println("ID: " + id + " | Gênero: " + name);
            }

            raf.seek(pos + size + 4);
        }

        raf.close();
    }

    // =============================
    // DEBUG
    // =============================
    public void exibirIndice() throws IOException {
        indice.exibir("genero");
    }
}