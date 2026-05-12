package main.java.CRUD.dao;

import java.io.*;
import java.nio.charset.StandardCharsets;

import main.java.CRUD.index.HashExtensivel;
import main.java.CRUD.model.*;
import main.java.CRUD.util.*;

public class ReviewDAO {

    private final String FILE = "data/review.bin";
    private HashExtensivel indice;

    public ReviewDAO() throws IOException {
        initFile();
        indice = new HashExtensivel(2);
        reconstruirIndice();
    }

    private void initFile() throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() == 0) {
            raf.writeInt(0); // último ID
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

    // CREATE
    public int create(Review r) throws Exception {

        UserDAO userDAO = new UserDAO();
        FilmDAO filmDAO = new FilmDAO();

        if (userDAO.read(r.getUserID()) == null)
            throw new Exception("Usuário não existe");

        if (filmDAO.read(r.getFilmID()) == null)
            throw new Exception("Filme não existe");

        if (r.getRating() < 0 || r.getRating() > 10)
            throw new Exception("Nota deve ser entre 0 e 10");

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

        byte[] noteBytes = r.getNote().getBytes(StandardCharsets.UTF_8);

        int recordSize =
                1 + 4 + // lápide + id
                4 +     // userID
                4 +     // filmID
                1 +     // rating (byte)
                2 + noteBytes.length;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);

        raf.writeBoolean(true);
        raf.writeInt(newID);

        raf.writeInt(r.getUserID());
        raf.writeInt(r.getFilmID());
        raf.writeByte(r.getRating());

        raf.writeShort(noteBytes.length);
        raf.write(noteBytes);

        raf.close();

        indice.inserir(newID, pos);

        // Atualiza média do filme
        Film f = filmDAO.read(r.getFilmID());

        if (f != null) {
            float novaMedia = ((f.getRating() * f.getTotalReviews()) + r.getRating())
                    / (f.getTotalReviews() + 1);

            f.setRating(novaMedia);
            f.setTotalReviews(f.getTotalReviews() + 1);

            filmDAO.update(f);
        }

        return newID;
    }

    // READ usando hash extensível
    public Review read(int idBusca) throws IOException {

        Long pos = indice.buscar(idBusca);
        if (pos == null) {
            return null;
        }

        RandomAccessFile raf = FileManeger.open(FILE);
        Review review = readAtPosition(raf, pos, idBusca);
        raf.close();

        return review;
    }

    private Review readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt(); // recordSize
        boolean active = raf.readBoolean();
        int id = raf.readInt();

        if (!active || id != expectedId) {
            return null;
        }

        int userID = raf.readInt();
        int filmID = raf.readInt();
        byte rating = raf.readByte();

        short size = raf.readShort();
        byte[] noteBytes = new byte[size];
        raf.readFully(noteBytes);
        String note = new String(noteBytes, StandardCharsets.UTF_8);

        return new Review(id, userID, filmID, rating, note);
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

        // lê a review antes de apagar para atualizar média do filme
        Review review = read(id);

        raf.seek(pos + 4);
        raf.writeBoolean(false);

        raf.close();

        indice.remover(id);

        // Atualiza média do filme
        if (review != null) {
            FilmDAO filmDAO = new FilmDAO();
            Film f = filmDAO.read(review.getFilmID());

            if (f != null && f.getTotalReviews() > 0) {
                int totalAtual = f.getTotalReviews();
                float somaAtual = f.getRating() * totalAtual;
                float novaSoma = somaAtual - review.getRating();
                int novoTotal = totalAtual - 1;

                float novaMedia = (novoTotal > 0) ? (novaSoma / novoTotal) : 0;

                f.setRating(novaMedia);
                f.setTotalReviews(novoTotal);

                filmDAO.update(f);
            }
        }

        return true;
    }

    // UPDATE usando hash extensível
    public boolean update(Review r) throws Exception {

        Long pos = indice.buscar(r.getID());
        if (pos == null) {
            return false;
        }

        UserDAO userDAO = new UserDAO();
        FilmDAO filmDAO = new FilmDAO();

        if (userDAO.read(r.getUserID()) == null)
            throw new Exception("Usuário não existe");

        if (filmDAO.read(r.getFilmID()) == null)
            throw new Exception("Filme não existe");

        if (r.getRating() < 0 || r.getRating() > 10)
            throw new Exception("Nota deve ser entre 0 e 10");

        // lê review antiga para ajustar média se necessário
        Review antiga = read(r.getID());

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

        long novaPos = createWithIdRetornandoPosicao(r);
        indice.inserir(r.getID(), novaPos);

        // Atualiza médias dos filmes
        if (antiga != null) {
            // se mudou de filme, remove do antigo e adiciona no novo
            if (antiga.getFilmID() != r.getFilmID()) {
                Film filmeAntigo = filmDAO.read(antiga.getFilmID());
                if (filmeAntigo != null && filmeAntigo.getTotalReviews() > 0) {
                    int totalAtual = filmeAntigo.getTotalReviews();
                    float somaAtual = filmeAntigo.getRating() * totalAtual;
                    float novaSoma = somaAtual - antiga.getRating();
                    int novoTotal = totalAtual - 1;

                    float novaMedia = (novoTotal > 0) ? (novaSoma / novoTotal) : 0;

                    filmeAntigo.setRating(novaMedia);
                    filmeAntigo.setTotalReviews(novoTotal);
                    filmDAO.update(filmeAntigo);
                }

                Film filmeNovo = filmDAO.read(r.getFilmID());
                if (filmeNovo != null) {
                    float novaMedia = ((filmeNovo.getRating() * filmeNovo.getTotalReviews()) + r.getRating())
                            / (filmeNovo.getTotalReviews() + 1);

                    filmeNovo.setRating(novaMedia);
                    filmeNovo.setTotalReviews(filmeNovo.getTotalReviews() + 1);
                    filmDAO.update(filmeNovo);
                }
            } else {
                // mesmo filme, só recalcula nota
                Film filme = filmDAO.read(r.getFilmID());
                if (filme != null && filme.getTotalReviews() > 0) {
                    float soma = (filme.getRating() * filme.getTotalReviews()) - antiga.getRating() + r.getRating();
                    float novaMedia = soma / filme.getTotalReviews();

                    filme.setRating(novaMedia);
                    filmDAO.update(filme);
                }
            }
        }

        return true;
    }

    private long createWithIdRetornandoPosicao(Review r) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        byte[] noteBytes = r.getNote().getBytes(StandardCharsets.UTF_8);

        int recordSize =
                1 + 4 +
                4 +
                4 +
                1 +
                2 + noteBytes.length;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);

        raf.writeBoolean(true);
        raf.writeInt(r.getID());

        raf.writeInt(r.getUserID());
        raf.writeInt(r.getFilmID());
        raf.writeByte(r.getRating());

        raf.writeShort(noteBytes.length);
        raf.write(noteBytes);

        raf.close();
        return pos;
    }

    // LISTAR POR FILME (continua sequencial)
    public void listByFilm(int filmID) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(4);

        System.out.println("\n--- Reviews do Filme " + filmID + " ---");

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4 + recordSize);
                continue;
            }

            int id = raf.readInt();

            int uID = raf.readInt();
            int fID = raf.readInt();
            byte rating = raf.readByte();

            short size = raf.readShort();
            byte[] noteBytes = new byte[size];
            raf.readFully(noteBytes);
            String note = new String(noteBytes, StandardCharsets.UTF_8);

            if (fID == filmID) {
                System.out.println(id + " | user: " + uID + " | nota: " + rating + " | " + note);
            }

            raf.seek(pos + 4 + recordSize);
        }

        raf.close();
    }

    // LISTAR POR USUÁRIO (continua sequencial)
    public void listByUser(int userID) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(4);

        System.out.println("\n--- Reviews do Usuário " + userID + " ---");

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4 + recordSize);
                continue;
            }

            int id = raf.readInt();

            int uID = raf.readInt();
            int fID = raf.readInt();
            byte rating = raf.readByte();

            short size = raf.readShort();
            byte[] noteBytes = new byte[size];
            raf.readFully(noteBytes);
            String note = new String(noteBytes, StandardCharsets.UTF_8);

            if (uID == userID) {
                System.out.println(id + " | filme: " + fID + " | nota: " + rating + " | " + note);
            }

            raf.seek(pos + 4 + recordSize);
        }

        raf.close();
    }

    public void listAll() throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(4);

        System.out.println("\n--- TODAS AS REVIEWS ---");

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {

                int userID = raf.readInt();
                int filmID = raf.readInt();
                byte rating = raf.readByte();

                short size = raf.readShort();
                byte[] noteBytes = new byte[size];
                raf.readFully(noteBytes);
                String note = new String(noteBytes, StandardCharsets.UTF_8);

                System.out.println("-----------------------");
                System.out.println("ID: " + id);
                System.out.println("Usuário ID: " + userID);
                System.out.println("Filme ID: " + filmID);
                System.out.println("Nota: " + rating);
                System.out.println("Comentário: " + note);
            }

            raf.seek(pos + recordSize + 4);
        }

        raf.close();
    }

    public void exibirIndice() {
        indice.exibir("review");
    }
}