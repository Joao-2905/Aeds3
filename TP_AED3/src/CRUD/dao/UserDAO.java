package CRUD.dao;

import CRUD.util.*;
import CRUD.model.*;
import CRUD.index.HashExtensivel;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UserDAO {

    private final String FILE = "data/user.bin";
    private HashExtensivel indice;

    public UserDAO() throws IOException {
        initFile();
        indice = new HashExtensivel(2);
        reconstruirIndice();
    }

    private void initFile() throws IOException {

        System.out.println("INIT USER DAO");

        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() == 0) {

            raf.writeInt(0); // último ID
            raf.close();

            // cria admin padrão
            User admin = new User(
                    "admin",
                    "admin@admin.com",
                    "123",
                    true
            );

            create(admin);

            System.out.println("Administrador padrão criado:");
            System.out.println("Email: admin@admin.com");
            System.out.println("Senha: 123");

        } else {
            raf.close();
        }
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

    // CREATE
    public int create(User x) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        byte[] nameBytes = x.getUsername().getBytes(StandardCharsets.UTF_8);
        short nameSize = (short) nameBytes.length;

        byte[] emailBytes = x.getEmail().getBytes(StandardCharsets.UTF_8);
        short emailSize = (short) emailBytes.length;

        // XOR na senha
        String senhaCripto = Crypto.xor(x.getPassword());

        byte[] passwordBytes = senhaCripto.getBytes(StandardCharsets.UTF_8);
        short passwordSize = (short) passwordBytes.length;

        // lápide, ID, tam nome, nome, tam email, email, tam senha, senha, administrador
        int recordSize = 1 + 4 + 2 + nameSize + 2 + emailSize + 2 + passwordSize + 1;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);

        // lápide
        raf.writeBoolean(true);

        raf.writeInt(newID);

        raf.writeShort(nameSize);
        raf.write(nameBytes);

        raf.writeShort(emailSize);
        raf.write(emailBytes);

        raf.writeShort(passwordSize);
        raf.write(passwordBytes);

        // administrador
        raf.writeBoolean(x.isAdministrator());

        raf.close();

        indice.inserir(newID, pos);

        return newID;
    }

    // READ usando hash extensível
    public User read(int idBusca) throws IOException {

        Long pos = indice.buscar(idBusca);
        if (pos == null) {
            return null;
        }

        RandomAccessFile raf = FileManeger.open(FILE);
        User user = readAtPosition(raf, pos, idBusca);
        raf.close();

        return user;
    }

    private User readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt(); // recordSize
        boolean active = raf.readBoolean();
        int id = raf.readInt();

        if (!active || id != expectedId) {
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

        String senhaCripto = new String(passwordBytes, StandardCharsets.UTF_8);
        String password = Crypto.xor(senhaCripto);

        boolean admin = raf.readBoolean();

        return new User(id, username, email, password, admin);
    }

    // UPDATE usando hash extensível
    public boolean update(User x) throws IOException {

        Long pos = indice.buscar(x.getID());
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

        // marca lápide
        raf.seek(pos + 4);
        raf.writeBoolean(false);

        raf.close();

        long novaPos = createWithIdRetornandoPosicao(x);
        indice.inserir(x.getID(), novaPos);

        return true;
    }

    private long createWithIdRetornandoPosicao(User x) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        byte[] nameBytes = x.getUsername().getBytes(StandardCharsets.UTF_8);
        short nameSize = (short) nameBytes.length;

        byte[] emailBytes = x.getEmail().getBytes(StandardCharsets.UTF_8);
        short emailSize = (short) emailBytes.length;

        String senhaCripto = Crypto.xor(x.getPassword());
        byte[] passwordBytes = senhaCripto.getBytes(StandardCharsets.UTF_8);
        short passwordSize = (short) passwordBytes.length;

        int recordSize = 1 + 4 + 2 + nameSize + 2 + emailSize + 2 + passwordSize + 1;

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

        raf.writeBoolean(x.isAdministrator());

        raf.close();
        return pos;
    }

    // DELETE usando hash extensível
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

        System.out.println("\n--- Users ---");

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4 + recordSize);
                continue;
            }

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

            String senhaCripto = new String(passwordBytes, StandardCharsets.UTF_8);
            String password = Crypto.xor(senhaCripto);

            boolean admin = raf.readBoolean();

            System.out.println(
                id + " | " + username + " | " + email + " | " + password + " | admin: " + admin
            );

            raf.seek(pos + 4 + recordSize);
        }

        raf.close();
    }

    // Continua sequencial por email
    public User readByEmail(String emailBusca) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(4); // pula header

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4 + recordSize);
                continue;
            }

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

            String senhaCripto = new String(passwordBytes, StandardCharsets.UTF_8);
            String password = Crypto.xor(senhaCripto);

            boolean admin = raf.readBoolean();

            if (email.equals(emailBusca)) {
                raf.close();
                return new User(id, username, email, password, admin);
            }

            raf.seek(pos + 4 + recordSize);
        }

        raf.close();
        return null;
    }

    public void exibirIndice() {
        indice.exibir("usuario");
    }
}