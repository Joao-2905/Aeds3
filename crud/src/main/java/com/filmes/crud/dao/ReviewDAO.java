package com.filmes.crud.dao;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.index.ArvoreBMais;
import com.filmes.crud.index.HashExtensivel;
import com.filmes.crud.index.ListaInvertidaDAO;
import com.filmes.crud.model.Film;
import com.filmes.crud.model.Review;
import com.filmes.crud.model.User;
import com.filmes.crud.sort.ExternalSortReview;
import com.filmes.crud.util.FileManeger;

public class ReviewDAO {

    private final String FILE = "data/review.bin";
    private final String HASH_NAME = "review";

    private HashExtensivel indice;

    private ListaInvertidaDAO indiceReviewPorFilme;
    private ListaInvertidaDAO indiceReviewPorUsuario;

    // =========================================================
    // ÁRVORE B+ - ÍNDICE POR NOTA/RATING
    // =========================================================

    private ArvoreBMais indiceBMaisPorNota;

    public ReviewDAO() throws IOException {
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

    private void reconstruirIndice() throws IOException {
        limparArquivosHash();
        limparArquivosListaInvertida();

        indice = new HashExtensivel(2, HASH_NAME);

        indiceReviewPorFilme = new ListaInvertidaDAO("review_filme");
        indiceReviewPorUsuario = new ListaInvertidaDAO("review_usuario");

        indiceBMaisPorNota = new ArvoreBMais(4, "review_rating");
        indiceBMaisPorNota.limpar();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                int userID = raf.readInt();
                int filmID = raf.readInt();
                byte rating = raf.readByte();

                indice.inserir(id, pos);
                indiceReviewPorFilme.inserir(filmID, id);
                indiceReviewPorUsuario.inserir(userID, id);
                indiceBMaisPorNota.inserir(rating, id);
            }

            raf.seek(pos + 4L + recordSize);
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

    private void limparArquivosListaInvertida() {
        File indiceFilme = new File("data/review_filme_lista_invertida.bin");
        File indiceUsuario = new File("data/review_usuario_lista_invertida.bin");

        if (indiceFilme.exists()) {
            indiceFilme.delete();
        }

        if (indiceUsuario.exists()) {
            indiceUsuario.delete();
        }
    }

    public int create(Review r) throws Exception {
        if (r == null) {
            throw new Exception("Review inválida");
        }

        if (r.getRating() < 0 || r.getRating() > 10) {
            throw new Exception("Nota deve ser entre 0 e 10");
        }

        if (r.getNote() == null) {
            r.setNote("");
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        raf.seek(0);
        int lastID = raf.readInt();
        int newID = lastID + 1;

        raf.seek(0);
        raf.writeInt(newID);

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
        raf.writeInt(newID);
        raf.writeInt(r.getUserID());
        raf.writeInt(r.getFilmID());
        raf.writeByte(r.getRating());
        raf.writeShort(noteBytes.length);
        raf.write(noteBytes);

        raf.close();

        indice.inserir(newID, pos);

        indiceReviewPorFilme.inserir(r.getFilmID(), newID);
        indiceReviewPorUsuario.inserir(r.getUserID(), newID);

        indiceBMaisPorNota.inserir(r.getRating(), newID);

        return newID;
    }

    public Review read(int idBusca) throws IOException {
        Long pos = indice.buscar(idBusca);

        if (pos != null) {
            RandomAccessFile raf = FileManeger.open(FILE);
            Review review = readAtPosition(raf, pos, idBusca);
            raf.close();

            if (review != null) {
                return review;
            }
        }

        return readByScan(idBusca);
    }

    private Review readByScan(int idBusca) throws IOException {
        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active && id == idBusca) {
                Review review = readAtPosition(raf, pos, idBusca);

                if (review != null) {
                    indice.inserir(idBusca, pos);
                }

                raf.close();
                return review;
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return null;
    }

    private Review readAtPosition(RandomAccessFile raf, long pos, int expectedId) throws IOException {
        raf.seek(pos);

        raf.readInt();
        boolean active = raf.readBoolean();
        int id = raf.readInt();

        if (!active || id != expectedId) return null;

        int userID = raf.readInt();
        int filmID = raf.readInt();
        byte rating = raf.readByte();

        short size = raf.readShort();
        byte[] noteBytes = new byte[size];
        raf.readFully(noteBytes);

        String note = new String(noteBytes, StandardCharsets.UTF_8);

        return new Review(id, userID, filmID, rating, note);
    }

    public List<Review> readAllToList() throws IOException {
        List<Review> lista = new ArrayList<>();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

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

                lista.add(new Review(id, userID, filmID, rating, note));
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();
        return lista;
    }

    public List<Review> readByUserID(int userIDBusca) throws IOException {
        List<Review> lista = new ArrayList<>();

        if (userIDBusca <= 0) {
            return lista;
        }

        List<Integer> ids = indiceReviewPorUsuario.buscar(userIDBusca);

        for (Integer id : ids) {
            Review review = read(id);

            if (review != null && review.getUserID() == userIDBusca) {
                lista.add(review);
            }
        }

        return lista;
    }

    public List<Review> readByFilmID(int filmIDBusca) throws IOException {
        List<Review> lista = new ArrayList<>();

        if (filmIDBusca <= 0) {
            return lista;
        }

        List<Integer> ids = indiceReviewPorFilme.buscar(filmIDBusca);

        for (Integer id : ids) {
            Review review = read(id);

            if (review != null && review.getFilmID() == filmIDBusca) {
                lista.add(review);
            }
        }

        return lista;
    }

    public Review readByUserAndFilmID(int userIDBusca, int filmIDBusca) throws IOException {
        if (userIDBusca <= 0 || filmIDBusca <= 0) {
            return null;
        }

        List<Review> reviewsDoUsuario = readByUserID(userIDBusca);

        for (Review review : reviewsDoUsuario) {
            if (review.getFilmID() == filmIDBusca) {
                return review;
            }
        }

        return null;
    }

    public boolean delete(int id) throws IOException {
        Long pos = indice.buscar(id);

        if (pos == null) {
            Review existente = readByScan(id);
            if (existente == null) {
                return false;
            }

            pos = indice.buscar(id);
            if (pos == null) {
                return false;
            }
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        Review review = readAtPosition(raf, pos, id);

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

        if (review != null) {
            indiceReviewPorFilme.remover(review.getFilmID(), id);
            indiceReviewPorUsuario.remover(review.getUserID(), id);
        }

        reconstruirIndice();

        return true;
    }

    public boolean deleteByFilmID(int filmID) throws IOException {
        if (filmID <= 0) {
            return false;
        }

        boolean deletouAlguma = false;

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                int userID = raf.readInt();
                int filmIDAtual = raf.readInt();

                if (filmIDAtual == filmID) {
                    raf.seek(pos + 4);
                    raf.writeBoolean(false);

                    indice.remover(id);
                    indiceReviewPorFilme.remover(filmIDAtual, id);
                    indiceReviewPorUsuario.remover(userID, id);

                    deletouAlguma = true;
                }
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();

        if (deletouAlguma) {
            reconstruirIndice();
        }

        return deletouAlguma;
    }

    public boolean deleteByUserID(int userIDBusca) throws IOException {
        if (userIDBusca <= 0) {
            return false;
        }

        boolean deletouAlguma = false;

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                int userID = raf.readInt();
                int filmID = raf.readInt();

                if (userID == userIDBusca) {
                    raf.seek(pos + 4);
                    raf.writeBoolean(false);

                    indice.remover(id);
                    indiceReviewPorUsuario.remover(userID, id);
                    indiceReviewPorFilme.remover(filmID, id);

                    deletouAlguma = true;
                }
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();

        if (deletouAlguma) {
            reconstruirIndice();
        }

        return deletouAlguma;
    }

    public boolean update(Review r) throws Exception {
        if (r == null) {
            throw new Exception("Review inválida");
        }

        Long pos = indice.buscar(r.getID());

        if (pos == null) {
            Review existente = readByScan(r.getID());
            if (existente == null) {
                return false;
            }

            pos = indice.buscar(r.getID());
            if (pos == null) {
                return false;
            }
        }

        if (r.getRating() < 0 || r.getRating() > 10) {
            throw new Exception("Nota deve ser entre 0 e 10");
        }

        if (r.getNote() == null) {
            r.setNote("");
        }

        RandomAccessFile raf = FileManeger.open(FILE);

        Review antiga = readAtPosition(raf, pos, r.getID());

        raf.seek(pos + 4);
        boolean active = raf.readBoolean();

        if (!active) {
            raf.close();
            return false;
        }

        raf.seek(pos + 4);
        raf.writeBoolean(false);

        raf.close();

        indice.remover(r.getID());

        long novaPos = createWithIdRetornandoPosicao(r);
        indice.inserir(r.getID(), novaPos);

        if (antiga != null) {
            if (antiga.getFilmID() != r.getFilmID()) {
                indiceReviewPorFilme.atualizar(
                        antiga.getFilmID(),
                        r.getFilmID(),
                        r.getID()
                );
            } else {
                indiceReviewPorFilme.inserir(r.getFilmID(), r.getID());
            }

            if (antiga.getUserID() != r.getUserID()) {
                indiceReviewPorUsuario.atualizar(
                        antiga.getUserID(),
                        r.getUserID(),
                        r.getID()
                );
            } else {
                indiceReviewPorUsuario.inserir(r.getUserID(), r.getID());
            }
        } else {
            indiceReviewPorFilme.inserir(r.getFilmID(), r.getID());
            indiceReviewPorUsuario.inserir(r.getUserID(), r.getID());
        }

        reconstruirIndice();

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

    public void recalcularMediaDoFilme(int filmID) throws Exception {
        FilmDAO filmDao = new FilmDAO();
        Film film = filmDao.read(filmID);

        if (film == null) {
            return;
        }

        List<Review> reviews = readByFilmID(filmID);

        int soma = 0;
        int total = 0;

        for (Review review : reviews) {
            if (review != null && review.getFilmID() == filmID) {
                soma += review.getRating();
                total++;
            }
        }

        if (total == 0) {
            film.setRating(0);
            film.setTotalReviews(0);
        } else {
            film.setRating((float) soma / total);
            film.setTotalReviews(total);
        }

        filmDao.update(film);
    }

    public int deleteReviewsDeFilmesInexistentes() throws Exception {
        int removidas = 0;
        FilmDAO filmDao = new FilmDAO();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                int userID = raf.readInt();
                int filmID = raf.readInt();

                Film film = filmDao.read(filmID);

                if (film == null) {
                    raf.seek(pos + 4);
                    raf.writeBoolean(false);

                    indice.remover(id);
                    indiceReviewPorFilme.remover(filmID, id);
                    indiceReviewPorUsuario.remover(userID, id);

                    removidas++;
                }
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();

        if (removidas > 0) {
            reconstruirIndice();
        }

        return removidas;
    }

    public int deleteReviewsDeUsuariosInexistentes() throws Exception {
        int removidas = 0;
        UserDAO userDao = new UserDAO();

        RandomAccessFile raf = FileManeger.open(FILE);
        raf.seek(4);

        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();
            int id = raf.readInt();

            if (active) {
                int userID = raf.readInt();
                int filmID = raf.readInt();

                User user = userDao.read(userID);

                if (user == null) {
                    raf.seek(pos + 4);
                    raf.writeBoolean(false);

                    indice.remover(id);
                    indiceReviewPorUsuario.remover(userID, id);
                    indiceReviewPorFilme.remover(filmID, id);

                    removidas++;
                }
            }

            raf.seek(pos + 4L + recordSize);
        }

        raf.close();

        if (removidas > 0) {
            reconstruirIndice();
        }

        return removidas;
    }

    // =========================================================
    // ORDENAÇÃO EXTERNA POR INTERCALAÇÃO
    // =========================================================

    public List<Review> listarOrdenadoPorNotaCrescente() throws Exception {
        ExternalSortReview sorter = new ExternalSortReview();
        return sorter.listarOrdenadoCrescente();
    }

    public List<Review> listarOrdenadoPorNotaDecrescente() throws Exception {
        ExternalSortReview sorter = new ExternalSortReview();
        return sorter.listarOrdenadoDecrescente();
    }

    public void gerarArquivoOrdenadoPorNota() throws Exception {
        ExternalSortReview sorter = new ExternalSortReview();
        sorter.ordenar();
    }

    // =========================================================
    // LISTA INVERTIDA
    // =========================================================

    public List<Integer> buscarIdsReviewsPorFilme(int filmID) throws IOException {
        return indiceReviewPorFilme.buscar(filmID);
    }

    public List<Integer> buscarIdsReviewsPorUsuario(int userID) throws IOException {
        return indiceReviewPorUsuario.buscar(userID);
    }

    public void reconstruirListaInvertida() throws IOException {
        reconstruirIndice();
    }

    // =========================================================
    // ÁRVORE B+ POR NOTA/RATING
    // =========================================================

    public List<Review> buscarPorNotaBMais(int rating) throws IOException {
        List<Review> lista = new ArrayList<>();

        if (rating < 0 || rating > 10) {
            return lista;
        }

        List<Integer> ids = indiceBMaisPorNota.buscar(rating);

        for (Integer id : ids) {
            Review review = read(id);

            if (review != null && review.getRating() == rating) {
                lista.add(review);
            }
        }

        return lista;
    }

    public List<Review> buscarPorIntervaloNotaBMais(int inicio, int fim) throws IOException {
        List<Review> lista = new ArrayList<>();

        if (inicio > fim) {
            int temp = inicio;
            inicio = fim;
            fim = temp;
        }

        if (inicio < 0) {
            inicio = 0;
        }

        if (fim > 10) {
            fim = 10;
        }

        List<Integer> ids = indiceBMaisPorNota.buscarIntervalo(inicio, fim);

        for (Integer id : ids) {
            Review review = read(id);

            if (review != null && review.getRating() >= inicio && review.getRating() <= fim) {
                lista.add(review);
            }
        }

        return lista;
    }

    public List<Review> listarOrdenadoPorNotaCrescenteBMais() throws IOException {
        List<Review> lista = new ArrayList<>();

        List<Integer> ids = indiceBMaisPorNota.listarCrescente();

        for (Integer id : ids) {
            Review review = read(id);

            if (review != null) {
                lista.add(review);
            }
        }

        return lista;
    }

    public List<Review> listarOrdenadoPorNotaDecrescenteBMais() throws IOException {
        List<Review> lista = new ArrayList<>();

        List<Integer> ids = indiceBMaisPorNota.listarDecrescente();

        for (Integer id : ids) {
            Review review = read(id);

            if (review != null) {
                lista.add(review);
            }
        }

        return lista;
    }

    public void reconstruirArvoreBMaisPorNota() throws IOException {
        reconstruirIndice();
    }

    public void exibirArvoreBMaisPorNota() {
        indiceBMaisPorNota.exibir();
    }
}