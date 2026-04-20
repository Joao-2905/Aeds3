package CRUD.dao;

import CRUD.model.Review;
import CRUD.index.HashExtensivel;
import CRUD.util.FileManeger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class ReviewDAO {

    private final String FILE = "data/review.bin";
    private HashExtensivel indice;

    public ReviewDAO() throws IOException {
        initFile();
        indice = new HashExtensivel(2);
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

    public int create(Review r) throws Exception {
        UserDAO userDAO = new UserDAO();
        FilmDAO filmDAO = new FilmDAO();

        if (userDAO.read(r.getUserID()) == null) {
            throw new Exception("Usuário não existe");
        }

        if (filmDAO.read(r.getFilmID()) == null) {
            throw new Exception("Filme não existe");
        }

        if (r.getRating() < 0 || r.getRating() > 10) {
            throw new Exception("Nota deve ser entre 0 e 10");
        }

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            raf.seek(0);
            int lastID = raf.readInt();
            int newID = lastID + 1;

            raf.seek(0);
            raf.writeInt(newID);

            byte[] noteBytes = r.getNote().getBytes(StandardCharsets.UTF_8);

            int recordSize =
                    1 + 4 +   // lápide + id
                    4 +       // userID
                    4 +       // filmID
                    1 +       // rating
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

            indice.inserir(newID, pos);

            return newID;
        }

        /*
         * Se o seu Film.java tiver estes métodos:
         * getRating(), getTotalReviews(), setRating(), setTotalReviews()
         * você pode reativar a atualização da média aqui.
         *
         * Film f = filmDAO.read(r.getFilmID());
         * float novaMedia = ((f.getRating() * f.getTotalReviews()) + r.getRating())
         *         / (f.getTotalReviews() + 1);
         * f.setRating(novaMedia);
         * f.setTotalReviews(f.getTotalReviews() + 1);
         * filmDAO.update(f);
         */
    }

    public Review read(int idBusca) throws IOException {
        Long pos = indice.buscar(idBusca);
        if (pos == null) {
            return null;
        }

        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            return readAtPosition(raf, pos, idBusca);
        }
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

    public boolean update(Review r) throws Exception {
        Long pos = indice.buscar(r.getID());
        if (pos == null) {
            return false;
        }

        UserDAO userDAO = new UserDAO();
        FilmDAO filmDAO = new FilmDAO();

        if (userDAO.read(r.getUserID()) == null) {
            throw new Exception("Usuário não existe");
        }

        if (filmDAO.read(r.getFilmID()) == null) {
            throw new Exception("Filme não existe");
        }

        if (r.getRating() < 0 || r.getRating() > 10) {
            throw new Exception("Nota deve ser entre 0 e 10");
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

        long novaPos = createWithIdRetornandoPosicao(r);
        indice.inserir(r.getID(), novaPos);
        return true;
    }

    private long createWithIdRetornandoPosicao(Review r) throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {
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

            return pos;
        }
    }

    public void listByFilm(int filmID) throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            raf.seek(4);

            System.out.println("\n--- Reviews do Filme " + filmID + " ---");

            while (raf.getFilePointer() < raf.length()) {
                long pos = raf.getFilePointer();

                int recordSize = raf.readInt();
                boolean active = raf.readBoolean();

                if (!active) {
                    raf.seek(pos + 4L + recordSize);
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

                raf.seek(pos + 4L + recordSize);
            }
        }
    }

    public void listByUser(int userID) throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {
            raf.seek(4);

            System.out.println("\n--- Reviews do Usuário " + userID + " ---");

            while (raf.getFilePointer() < raf.length()) {
                long pos = raf.getFilePointer();

                int recordSize = raf.readInt();
                boolean active = raf.readBoolean();

                if (!active) {
                    raf.seek(pos + 4L + recordSize);
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

                raf.seek(pos + 4L + recordSize);
            }
        }
    }

    public void listAll() throws IOException {
        try (RandomAccessFile raf = FileManeger.open(FILE)) {
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

                raf.seek(pos + 4L + recordSize);
            }
        }
    }

    public void exibirIndice() {
        indice.exibir();
    }
}