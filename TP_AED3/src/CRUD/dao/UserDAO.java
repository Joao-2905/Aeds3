package CRUD.dao;

import CRUD.util.FileManeger;
import CRUD.model.User;
import CRUD.index.HashExtensivel;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class UserDAO {

    private final String FILE = "data/user.bin";
    private HashExtensivel indice;

    public UserDAO() throws IOException {
        initFile();
        indice = new HashExtensivel(2); // capacidade do bucket
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

    public int create(User x) throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {

            raf.seek(0);
            int lastID = raf.readInt();
            int newID = lastID + 1;

            raf.seek(0);
            raf.writeInt(newID);

            byte[] nameBytes = x.getUsername().getBytes(StandardCharsets.UTF_8);
            short nameSize = (short) nameBytes.length;

            byte[] emailBytes = x.getEmail().getBytes(StandardCharsets.UTF_8);
            short emailSize = (short) emailBytes.length;

            byte[] passwordBytes = x.getPassword().getBytes(StandardCharsets.UTF_8);
            short passwordSize = (short) passwordBytes.length;

            // lápide + ID + tam nome + nome + tam email + email + tam senha + senha
            int recordSize = 1 + 4 + 2 + nameSize + 2 + emailSize + 2 + passwordSize;

            long pos = raf.length();
            raf.seek(pos);

            raf.writeInt(recordSize);
            raf.writeBoolean(true);
            raf.writeInt(newID);

            raf.writeShort(nameSize);
            raf.write(nameBytes);

            raf.writeShort(emailSize);
            raf.write(emailBytes);

            raf.writeShort(passwordSize);
            raf.write(passwordBytes);

            indice.inserir(newID, pos);
            return newID;
        }
    }

    public User read(int id) throws IOException {
        Long pos = indice.buscar(id);
        if (pos == null) {
            return null;
        }

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            return readAtPosition(raf, pos, id);
        }
    }

    private User readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt(); // recordSize
        boolean active = raf.readBoolean();
        int tmpID = raf.readInt();

        if (!active || tmpID != expectedId) {
            return null;
        }

        short nameSize = raf.readShort();
        byte[] nameBytes = new byte[nameSize];
        raf.readFully(nameBytes);
        String username = new String(nameBytes, StandardCharsets.UTF_8);

        short emailSize = raf.readShort();
        byte[] emailBytes = new byte[emailSize];
        raf.readFully(emailBytes);
        String email = new String(emailBytes, StandardCharsets.UTF_8);

        short passwordSize = raf.readShort();
        byte[] passwordBytes = new byte[passwordSize];
        raf.readFully(passwordBytes);
        String password = new String(passwordBytes, StandardCharsets.UTF_8);

        return new User(tmpID, username, email, password);
    }

    public boolean update(User x) throws IOException {
        Long pos = indice.buscar(x.getID());
        if (pos == null) {
            return false;
        }

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            raf.seek(pos + 4); // pula tamanho do registro
            boolean active = raf.readBoolean();

            if (!active) {
                return false;
            }

            raf.seek(pos + 4);
            raf.writeBoolean(false);
        }

        long novaPos = createWithIdRetornandoPosicao(x);
        indice.inserir(x.getID(), novaPos);
        return true;
    }

    private long createWithIdRetornandoPosicao(User x) throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {

            byte[] nameBytes = x.getUsername().getBytes(StandardCharsets.UTF_8);
            short nameSize = (short) nameBytes.length;

            byte[] emailBytes = x.getEmail().getBytes(StandardCharsets.UTF_8);
            short emailSize = (short) emailBytes.length;

            byte[] passwordBytes = x.getPassword().getBytes(StandardCharsets.UTF_8);
            short passwordSize = (short) passwordBytes.length;

            int recordSize = 1 + 4 + 2 + nameSize + 2 + emailSize + 2 + passwordSize;

            long pos = raf.length();
            raf.seek(pos);

            raf.writeInt(recordSize);
            raf.writeBoolean(true);
            raf.writeInt(x.getID());

            raf.writeShort(nameSize);
            raf.write(nameBytes);

            raf.writeShort(emailSize);
            raf.write(emailBytes);

            raf.writeShort(passwordSize);
            raf.write(passwordBytes);

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

            System.out.println("\n--- Users ---");

            while (raf.getFilePointer() < raf.length()) {
                long pos = raf.getFilePointer();

                int recordSize = raf.readInt();
                boolean active = raf.readBoolean();

                if (active) {
                    int id = raf.readInt();

                    short nameSize = raf.readShort();
                    byte[] nameBytes = new byte[nameSize];
                    raf.readFully(nameBytes);
                    String username = new String(nameBytes, StandardCharsets.UTF_8);

                    short emailSize = raf.readShort();
                    byte[] emailBytes = new byte[emailSize];
                    raf.readFully(emailBytes);
                    String email = new String(emailBytes, StandardCharsets.UTF_8);

                    short passwordSize = raf.readShort();
                    byte[] passwordBytes = new byte[passwordSize];
                    raf.readFully(passwordBytes);
                    String password = new String(passwordBytes, StandardCharsets.UTF_8);

                    System.out.println(id + " | " + username + " | " + email + " | " + password);
                }

                raf.seek(pos + 4L + recordSize);
            }
        }
    }

    public void exibirIndice() {
        indice.exibir();
    }
}