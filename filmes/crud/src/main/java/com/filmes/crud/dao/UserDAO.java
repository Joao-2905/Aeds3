package com.filmes.crud.dao;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.index.Bucket;
import com.filmes.crud.index.HashExtensivel;
import com.filmes.crud.index.IndexEntry;
import com.filmes.crud.model.User;
import com.filmes.crud.util.Crypto;
import com.filmes.crud.util.FileManeger;

public class UserDAO {

    private final String FILE = "./data/user.bin";
    private final String HASH_NAME = "user";
    private HashExtensivel indice;

    public UserDAO() throws IOException {

        File pasta = new File(System.getProperty("user.dir") + "/data");

        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        initFile();
        reconstruirIndice();
        garantirAdminPadrao();
        reconstruirIndice();
}

    private void initFile() throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() == 0) {
            raf.writeInt(0);
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

    private void garantirAdminPadrao() throws IOException {
        int totalAdmins = countAdmins();

        if (totalAdmins <= 0) {
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
        }
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

    public int create(User user) throws IOException {
        if (user == null) {
            throw new IOException("Usuário inválido.");
        }

        prepararUsuario(user);
        validarUsuarioParaPersistencia(user);

        User existente = readByEmail(user.getEmail());
        if (existente != null) {
            throw new IOException("Já existe um usuário com esse e-mail.");
        }

        User existenteUsername = readByUsername(user.getUsername());
        if (existenteUsername != null) {
            throw new IOException("Já existe um usuário com esse username.");
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        byte[] usernameBytes = user.getUsername().getBytes(StandardCharsets.UTF_8);
        short usernameSize = (short) usernameBytes.length;

        byte[] emailBytes = user.getEmail().getBytes(StandardCharsets.UTF_8);
        short emailSize = (short) emailBytes.length;

        String encryptedPassword = Crypto.xor(user.getPassword());
        byte[] passwordBytes = encryptedPassword.getBytes(StandardCharsets.UTF_8);
        short passwordSize = (short) passwordBytes.length;

        int recordSize =
                1 +
                4 +
                2 + usernameSize +
                2 + emailSize +
                2 + passwordSize +
                1;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(newID);

        raf.writeShort(usernameSize);
        raf.write(usernameBytes);

        raf.writeShort(emailSize);
        raf.write(emailBytes);

        raf.writeShort(passwordSize);
        raf.write(passwordBytes);

        raf.writeBoolean(user.isAdministrator());

        raf.close();

        indice.inserir(newID, pos);

        return newID;
    }

    public User read(int idBusca) throws IOException {
        if (idBusca <= 0) {
            return null;
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        Long pos = indice.buscar(idBusca);
        if (pos != null) {
            User user = readAtPosition(raf, pos, idBusca);
            if (user != null) {
                raf.close();
                return user;
            }
        }

        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long posAtual = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active && id == idBusca) {
                User user = readAtPosition(raf, posAtual, idBusca);

                if (user != null) {
                    indice.inserir(idBusca, posAtual);
                }

                raf.close();
                return user;
            }

            raf.seek(posAtual + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    private User readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt();
        boolean active = raf.readBoolean();
        int id = raf.readInt();

        if (!active || id != expectedId) {
            return null;
        }

        short usernameSize = raf.readShort();
        byte[] usernameBytes = new byte[usernameSize];
        raf.readFully(usernameBytes);
        String username = new String(usernameBytes, StandardCharsets.UTF_8);

        short emailSize = raf.readShort();
        byte[] emailBytes = new byte[emailSize];
        raf.readFully(emailBytes);
        String email = new String(emailBytes, StandardCharsets.UTF_8);

        short passwordSize = raf.readShort();
        byte[] passwordBytes = new byte[passwordSize];
        raf.readFully(passwordBytes);
        String encryptedPassword = new String(passwordBytes, StandardCharsets.UTF_8);
        String password = Crypto.xor(encryptedPassword);

        boolean administrador = raf.readBoolean();

        return new User(id, username, email, password, administrador);
    }

    public User readByEmail(String emailBusca) throws IOException {
        if (emailBusca == null || emailBusca.trim().isEmpty()) {
            return null;
        }

        emailBusca = emailBusca.trim().toLowerCase();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4L + recordSize);
                continue;
            }

            int id = raf.readInt();

            short usernameSize = raf.readShort();
            byte[] usernameBytes = new byte[usernameSize];
            raf.readFully(usernameBytes);
            String username = new String(usernameBytes, StandardCharsets.UTF_8);

            short emailSize = raf.readShort();
            byte[] emailBytes = new byte[emailSize];
            raf.readFully(emailBytes);
            String email = new String(emailBytes, StandardCharsets.UTF_8);

            short passwordSize = raf.readShort();
            byte[] passwordBytes = new byte[passwordSize];
            raf.readFully(passwordBytes);
            String encryptedPassword = new String(passwordBytes, StandardCharsets.UTF_8);
            String password = Crypto.xor(encryptedPassword);

            boolean administrador = raf.readBoolean();

            if (email != null && email.trim().toLowerCase().equals(emailBusca)) {
                indice.inserir(id, pos);
                raf.close();
                return new User(id, username, email, password, administrador);
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    public User readByUsername(String usernameBusca) throws IOException {
        if (usernameBusca == null || usernameBusca.trim().isEmpty()) {
            return null;
        }

        usernameBusca = usernameBusca.trim();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4L + recordSize);
                continue;
            }

            int id = raf.readInt();

            short usernameSize = raf.readShort();
            byte[] usernameBytes = new byte[usernameSize];
            raf.readFully(usernameBytes);
            String username = new String(usernameBytes, StandardCharsets.UTF_8);

            short emailSize = raf.readShort();
            byte[] emailBytes = new byte[emailSize];
            raf.readFully(emailBytes);
            String email = new String(emailBytes, StandardCharsets.UTF_8);

            short passwordSize = raf.readShort();
            byte[] passwordBytes = new byte[passwordSize];
            raf.readFully(passwordBytes);
            String encryptedPassword = new String(passwordBytes, StandardCharsets.UTF_8);
            String password = Crypto.xor(encryptedPassword);

            boolean administrador = raf.readBoolean();

            if (username != null && username.trim().equals(usernameBusca)) {
                indice.inserir(id, pos);
                raf.close();
                return new User(id, username, email, password, administrador);
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    public List<User> readAllToList() throws IOException {
        List<User> lista = new ArrayList<>();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4L + recordSize);
                continue;
            }

            int id = raf.readInt();

            short usernameSize = raf.readShort();
            byte[] usernameBytes = new byte[usernameSize];
            raf.readFully(usernameBytes);
            String username = new String(usernameBytes, StandardCharsets.UTF_8);

            short emailSize = raf.readShort();
            byte[] emailBytes = new byte[emailSize];
            raf.readFully(emailBytes);
            String email = new String(emailBytes, StandardCharsets.UTF_8);

            short passwordSize = raf.readShort();
            byte[] passwordBytes = new byte[passwordSize];
            raf.readFully(passwordBytes);
            String encryptedPassword = new String(passwordBytes, StandardCharsets.UTF_8);
            String password = Crypto.xor(encryptedPassword);

            boolean administrador = raf.readBoolean();

            lista.add(new User(id, username, email, password, administrador));

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return lista;
    }

    public int countAdmins() throws IOException {
        int total = 0;

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4L + recordSize);
                continue;
            }

            raf.readInt();

            short usernameSize = raf.readShort();
            raf.skipBytes(usernameSize);

            short emailSize = raf.readShort();
            raf.skipBytes(emailSize);

            short passwordSize = raf.readShort();
            raf.skipBytes(passwordSize);

            boolean administrador = raf.readBoolean();

            if (administrador) {
                total++;
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return total;
    }

    public boolean update(User user) throws IOException {
        if (user == null || user.getID() <= 0) {
            return false;
        }

        prepararUsuario(user);
        validarUsuarioParaPersistencia(user);

        User existenteEmail = readByEmail(user.getEmail());
        if (existenteEmail != null && existenteEmail.getID() != user.getID()) {
            throw new IOException("Já existe um usuário com esse e-mail.");
        }

        User existenteUsername = readByUsername(user.getUsername());
        if (existenteUsername != null && existenteUsername.getID() != user.getID()) {
            throw new IOException("Já existe um usuário com esse username.");
        }

        Long pos = indice.buscar(user.getID());

        if (pos == null) {
            User existente = read(user.getID());
            if (existente == null) {
                return false;
            }
            pos = indice.buscar(user.getID());
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

        indice.remover(user.getID());

        long novaPos = createWithIdRetornandoPosicao(user);
        indice.inserir(user.getID(), novaPos);

        return true;
    }

    private long createWithIdRetornandoPosicao(User user) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        prepararUsuario(user);
        validarUsuarioParaPersistencia(user);

        byte[] usernameBytes = user.getUsername().getBytes(StandardCharsets.UTF_8);
        short usernameSize = (short) usernameBytes.length;

        byte[] emailBytes = user.getEmail().getBytes(StandardCharsets.UTF_8);
        short emailSize = (short) emailBytes.length;

        String encryptedPassword = Crypto.xor(user.getPassword());
        byte[] passwordBytes = encryptedPassword.getBytes(StandardCharsets.UTF_8);
        short passwordSize = (short) passwordBytes.length;

        int recordSize =
                1 +
                4 +
                2 + usernameSize +
                2 + emailSize +
                2 + passwordSize +
                1;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(user.getID());

        raf.writeShort(usernameSize);
        raf.write(usernameBytes);

        raf.writeShort(emailSize);
        raf.write(emailBytes);

        raf.writeShort(passwordSize);
        raf.write(passwordBytes);

        raf.writeBoolean(user.isAdministrator());

        raf.close();
        return pos;
    }

    public boolean delete(int id) throws IOException {
        Long pos = indice.buscar(id);

        if (pos == null) {
            User existente = read(id);
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
                raf.seek(pos + 4L + recordSize);
                continue;
            }

            int id = raf.readInt();

            short usernameSize = raf.readShort();
            byte[] usernameBytes = new byte[usernameSize];
            raf.readFully(usernameBytes);
            String username = new String(usernameBytes, StandardCharsets.UTF_8);

            short emailSize = raf.readShort();
            byte[] emailBytes = new byte[emailSize];
            raf.readFully(emailBytes);
            String email = new String(emailBytes, StandardCharsets.UTF_8);

            short passwordSize = raf.readShort();
            byte[] passwordBytes = new byte[passwordSize];
            raf.readFully(passwordBytes);
            String encryptedPassword = new String(passwordBytes, StandardCharsets.UTF_8);
            String password = Crypto.xor(encryptedPassword);

            boolean administrador = raf.readBoolean();

            System.out.println(
                id + " | " + username + " | " + email + " | " + password + " | admin: " + administrador
            );

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
    }

    public void exibirIndice() {
        indice.exibir("usuario");
    }

    public void validarIndice() throws IOException {
        System.out.println("\n=== VALIDANDO HASH EXTENSÍVEL (USER) ===");

        RandomAccessFile raf = FileManeger.open(FILE);

        for (int i = 0; i < indice.getDiretorio().size(); i++) {
            Bucket bucket = indice.getDiretorio().get(i);

            for (IndexEntry entry : bucket.getEntradas()) {
                int id = entry.getId();
                long pos = entry.getEndereco();

                if (pos < 4 || pos >= raf.length()) {
                    System.out.println("❌ ERRO!");
                    System.out.println("ID no índice: " + id);
                    System.out.println("Offset inválido: " + pos);
                    continue;
                }

                raf.seek(pos);

                int size = raf.readInt();
                boolean active = raf.readBoolean();
                int idLido = raf.readInt();

                if (!active) {
                    System.out.println("⚠️ Registro deletado ainda presente no índice! ID: " + id);
                    continue;
                }

                if (id != idLido) {
                    System.out.println("❌ ERRO!");
                    System.out.println("ID no índice: " + id);
                    System.out.println("ID no arquivo: " + idLido);
                    System.out.println("Posição: " + pos);
                    System.out.println("Tamanho do registro: " + size);
                } else {
                    System.out.println("✔ OK -> ID: " + id + " | Offset: " + pos);
                }
            }
        }

        raf.close();
    }

    private void prepararUsuario(User user) {
        if (user.getUsername() == null) {
            user.setUsername("");
        } else {
            user.setUsername(user.getUsername().trim());
        }

        if (user.getEmail() == null) {
            user.setEmail("");
        } else {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }

        if (user.getPassword() == null) {
            user.setPassword("");
        } else {
            user.setPassword(user.getPassword().trim());
        }
    }

    private void validarUsuarioParaPersistencia(User user) throws IOException {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IOException("Username é obrigatório.");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IOException("E-mail é obrigatório.");
        }

        if (!user.getEmail().contains("@")) {
            throw new IOException("E-mail inválido.");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IOException("Senha é obrigatória.");
        }
    }
}