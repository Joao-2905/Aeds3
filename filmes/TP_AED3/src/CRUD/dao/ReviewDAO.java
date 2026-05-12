package CRUD.dao;

import CRUD.model.*;
import CRUD.util.*;
import CRUD.index.HashExtensivel;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ReviewDAO {

    private final String FILE = "data/review.bin";

    private HashExtensivel indice;        // reviewID
    private HashExtensivel indiceFilm;    // filmID
    private HashExtensivel indiceUser;    // userID
    private FilmDAO filmDAO;

    public ReviewDAO() throws IOException {
        initFile();
        reconstruirIndice();
        filmDAO = new FilmDAO(); 
    }

    private void initFile() throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() == 0) {
            raf.writeInt(0);
        }

        raf.close();
    }

    private void reconstruirIndice() throws IOException {

    	indice = new HashExtensivel(2, "review");
    	indiceFilm = new HashExtensivel(2, "reviewFilm");
    	indiceUser = new HashExtensivel(2, "reviewUser");

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            int userID = raf.readInt();
            int filmID = raf.readInt();

            raf.readByte(); // rating

            short size = raf.readShort();
            raf.skipBytes(size);

            long proxFilm = raf.readLong();
            long proxUser = raf.readLong();

            if (active) {
                indice.inserir(id, pos);
                indiceFilm.inserir(filmID, pos);
                indiceUser.inserir(userID, pos);
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
    }

    // ================= CREATE =================

    public int create(Review r) throws Exception {

        UserDAO userDAO = new UserDAO();

        if (userDAO.read(r.getUserID()) == null)
            throw new Exception("Usuário não existe");

        if (filmDAO.read(r.getFilmID()) == null)
            throw new Exception("Filme não existe");

        if (r.getRating() < 0 || r.getRating() > 10)
            throw new Exception("Nota inválida");

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int newID = raf.readInt() + 1;
        raf.seek(0);
        raf.writeInt(newID);

        // 🔥 ENCADEAMENTO
        Long headFilm = indiceFilm.buscar(r.getFilmID());
        Long headUser = indiceUser.buscar(r.getUserID());

        r.setID(newID);
        r.setProxFilm(headFilm == null ? -1 : headFilm);
        r.setProxUser(headUser == null ? -1 : headUser);

        byte[] noteBytes = r.getNote().getBytes(StandardCharsets.UTF_8);

        int recordSize =
                1 + 4 + 4 + 4 + 1 +
                2 + noteBytes.length +
                8 + 8;

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

        raf.writeLong(r.getProxFilm());
        raf.writeLong(r.getProxUser());

        raf.close();

        indice.inserir(newID, pos);
        indiceFilm.inserir(r.getFilmID(), pos);
        indiceUser.inserir(r.getUserID(), pos);
        
        atualizarRatingFilme(r.getFilmID(), r.getRating(), true);

        return newID;
    }

    // ================= READ =================

    public Review read(int id) throws IOException {
        Long pos = indice.buscar(id);
        if (pos == null) return null;

        RandomAccessFile raf = FileManeger.open(FILE);
        Review r = readAtPosition(raf, pos);
        raf.close();
        return r;
    }

    private Review readAtPosition(RandomAccessFile raf, long pos) throws IOException {

        raf.seek(pos);

        raf.readInt();
        boolean active = raf.readBoolean();
        int id = raf.readInt();

        if (!active) return null;

        int userID = raf.readInt();
        int filmID = raf.readInt();
        byte rating = raf.readByte();

        short size = raf.readShort();
        byte[] noteBytes = new byte[size];
        raf.readFully(noteBytes);

        long proxFilm = raf.readLong();
        long proxUser = raf.readLong();

        Review r = new Review(id, userID, filmID, rating,
                new String(noteBytes, StandardCharsets.UTF_8));

        r.setProxFilm(proxFilm);
        r.setProxUser(proxUser);

        return r;
    }

    public boolean delete(int id) throws IOException {

        Long pos = indice.buscar(id);
        if (pos == null) return false;

        Review r = read(id);
        if (r == null) return false;

        // 🔥 1. Atualiza encadeamento
        atualizarEncadeamento(r, pos);

        // 🔥 2. Atualiza HASH SECUNDÁRIO (HEAD)
        atualizarHeadIndices(r, pos);

        // 🔥 3. Marca como removido
        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(pos + 4);
        raf.writeBoolean(false);
        raf.close();

        // 🔥 4. Remove do índice primário
        indice.remover(id);
        
        atualizarRatingFilme(r.getFilmID(), r.getRating(), false);

        return true;
    }
    
    private void atualizarHeadIndices(Review r, long posRemovido) throws IOException {

        // ===== FILME =====
        Long headFilm = indiceFilm.buscar(r.getFilmID());

        if (headFilm != null && headFilm == posRemovido) {

            indiceFilm.remover(r.getFilmID());

            if (r.getProxFilm() != -1) {
                indiceFilm.inserir(r.getFilmID(), r.getProxFilm());
            }
        }

        // ===== USUÁRIO =====
        Long headUser = indiceUser.buscar(r.getUserID());

        if (headUser != null && headUser == posRemovido) {

            indiceUser.remover(r.getUserID());

            if (r.getProxUser() != -1) {
                indiceUser.inserir(r.getUserID(), r.getProxUser());
            }
        }
    }
    
    

    // 🔥 REMOVE DA LISTA ENCADEADA
    private void atualizarEncadeamento(Review r, long posRemovido) throws IOException {

        // FILME
        Long headFilm = indiceFilm.buscar(r.getFilmID());

        if (headFilm != null && headFilm != posRemovido) {
        	atualizarAnterior(headFilm, posRemovido, r.getProxFilm(), true);
        }

        // USUÁRIO
        Long headUser = indiceUser.buscar(r.getUserID());

        if (headUser != null && headUser != posRemovido) {
        	atualizarAnterior(headUser, posRemovido, r.getProxUser(), false);
        }
    }

    private void atualizarAnterior(long atualPos, long removido, long novoProx, boolean filme) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        while (atualPos != -1) {

            raf.seek(atualPos);

            raf.readInt();      // recordSize
            raf.readBoolean();  // active
            raf.readInt();      // id

            raf.readInt();      // userID
            raf.readInt();      // filmID
            raf.readByte();     // rating

            short size = raf.readShort();
            raf.skipBytes(size);

            long proxFilm = raf.readLong();
            long proxUser = raf.readLong();

            long prox = filme ? proxFilm : proxUser;

            // 🔥 achou quem aponta pro removido
            if (prox == removido) {

                if (filme) {
                    raf.seek(atualPos + (4 + 1 + 4 + 4 + 4 + 1 + 2 + size));
                    raf.writeLong(novoProx);
                } else {
                    raf.seek(atualPos + (4 + 1 + 4 + 4 + 4 + 1 + 2 + size + 8));
                    raf.writeLong(novoProx);
                }

                break;
            }

            atualPos = prox;
        }

        raf.close();
    }

    // ================= LISTAS =================

    public void listByFilm(int filmID) throws IOException {

        System.out.println("\n--- Reviews do Filme ---");

        Long pos = indiceFilm.buscar(filmID);
        RandomAccessFile raf = FileManeger.open(FILE);

        while (pos != null && pos != -1) {

            Review r = readAtPosition(raf, pos);

            if (r != null) {
                System.out.println("-----------------------");
                System.out.println("ID: " + r.getID());
                System.out.println("Usuário ID: " + r.getUserID());
                System.out.println("Filme ID: " + r.getFilmID());
                System.out.println("Nota: " + r.getRating());
                System.out.println("Comentário: " + r.getNote());
            }

            pos = r.getProxFilm();
        }

        raf.close();
    }

    public void listByUser(int userID) throws IOException {

        System.out.println("\n--- Reviews do Usuário ---");

        Long pos = indiceUser.buscar(userID);
        RandomAccessFile raf = FileManeger.open(FILE);

        while (pos != null && pos != -1) {

            Review r = readAtPosition(raf, pos);

            if (r != null) {
                System.out.println("-----------------------");
                System.out.println("ID: " + r.getID());
                System.out.println("Usuário ID: " + r.getUserID());
                System.out.println("Filme ID: " + r.getFilmID());
                System.out.println("Nota: " + r.getRating());
                System.out.println("Comentário: " + r.getNote());
            }

            pos = r.getProxUser();
        }

        raf.close();
    }
    
    public void exibirIndiceFilm() throws IOException {
        indiceFilm.exibir("review (film)");
    }

    public void exibirIndiceUser() throws IOException {
        indiceUser.exibir("review (user)");
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

            int userID = raf.readInt();
            int filmID = raf.readInt();
            byte rating = raf.readByte();

            short size = raf.readShort();
            byte[] noteBytes = new byte[size];
            raf.readFully(noteBytes);

            long proxFilm = raf.readLong();
            long proxUser = raf.readLong();

            if (active) {
                System.out.println("-----------------------");
                System.out.println("ID: " + id);
                System.out.println("Usuário: " + userID);
                System.out.println("Filme: " + filmID);
                System.out.println("Nota: " + rating);
                System.out.println("Comentário: " + new String(noteBytes));
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
    }
    
    public boolean update(Review r) throws Exception {

        Long pos = indice.buscar(r.getID());
        if (pos == null) return false;

        Review antiga = read(r.getID());
        if (antiga == null) return false;

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(pos + 4);
        boolean active = raf.readBoolean();

        if (!active) {
            raf.close();
            return false;
        }

        // 🔥 marca como removido
        raf.seek(pos + 4);
        raf.writeBoolean(false);
        raf.close();

        // 🔥 REMOVE DA LISTA ANTIGA
        atualizarEncadeamento(antiga, pos);
        atualizarHeadIndices(antiga, pos);

        // 🔥 ATUALIZA RATING (remove antiga)
        atualizarRatingFilme(antiga.getFilmID(), antiga.getRating(), false);

        // 🔥 INSERE NOVO
        indice.remover(r.getID());
        createComMesmoID(r);

        // 🔥 ATUALIZA RATING (adiciona nova)
        atualizarRatingFilme(r.getFilmID(), r.getRating(), true);

        return true;
    }
    
    private long createComMesmoID(Review r) throws IOException {

        RandomAccessFile raf = FileManeger.open(FILE);

        Long headFilm = indiceFilm.buscar(r.getFilmID());
        Long headUser = indiceUser.buscar(r.getUserID());

        r.setProxFilm(headFilm == null ? -1 : headFilm);
        r.setProxUser(headUser == null ? -1 : headUser);

        byte[] noteBytes = r.getNote().getBytes(StandardCharsets.UTF_8);

        int recordSize =
                1 + 4 + 4 + 4 + 1 +
                2 + noteBytes.length +
                8 + 8;

        long pos = raf.length();
        raf.seek(pos);

        raf.writeInt(recordSize);
        raf.writeBoolean(true);
        raf.writeInt(r.getID()); //  mantém ID

        raf.writeInt(r.getUserID());
        raf.writeInt(r.getFilmID());
        raf.writeByte(r.getRating());

        raf.writeShort(noteBytes.length);
        raf.write(noteBytes);

        raf.writeLong(r.getProxFilm());
        raf.writeLong(r.getProxUser());

        raf.close();

        indice.inserir(r.getID(), pos);
        indiceFilm.inserir(r.getFilmID(), pos);
        indiceUser.inserir(r.getUserID(), pos);

        return pos;
    }
    
    public void exibirIndice() throws IOException {
        indice.exibir("review");
    }
    
    private void atualizarRatingFilme(int filmID, int nota, boolean adicionando) throws IOException {

        Film f = filmDAO.read(filmID);

        if (f == null) return;

        float soma = f.getRating() * f.getTotalReviews();

        if (adicionando) {
            soma += nota;
            f.setTotalReviews(f.getTotalReviews() + 1);
        } else {
            soma -= nota;
            f.setTotalReviews(f.getTotalReviews() - 1);
        }

        if (f.getTotalReviews() > 0)
            f.setRating(soma / f.getTotalReviews());
        else
            f.setRating(0);

        filmDAO.update(f);
    }
    
}