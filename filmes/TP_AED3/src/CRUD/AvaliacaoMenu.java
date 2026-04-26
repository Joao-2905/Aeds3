package CRUD;

import java.io.IOException;
import java.util.Scanner;

import CRUD.dao.ReviewDAO;
import CRUD.model.Review;
import CRUD.sort.ExternalSortReview;

public class AvaliacaoMenu {

    public static void menuAvaliacao(Scanner sc) {

        ReviewDAO reviewDao;

        try {
            reviewDao = new ReviewDAO();
        } catch (Exception e) {
            System.out.println("Erro ao inicializar ReviewDAO: " + e.getMessage());
            return;
        }

        int op = -1;

        do {
            System.out.println("\n=== MENU AVALIAÇÃO ===");
            System.out.println("1 - Cadastrar avaliação");
            System.out.println("2 - Buscar avaliação");
            System.out.println("3 - Listar TODAS avaliações");
            System.out.println("4 - Listar avaliações por filme (HASH + ENCADEAMENTO)");
            System.out.println("5 - Listar avaliações por usuário (HASH + ENCADEAMENTO)");
            System.out.println("6 - Atualizar avaliação");
            System.out.println("7 - Excluir avaliação");
            System.out.println("8 - Ordenar avaliações por nota");
            System.out.println("9 - Exibir hash extensível");
            System.out.println("0 - Voltar");
            System.out.print("Opção: ");

            try {
                op = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Entrada inválida!");
                sc.nextLine();
                continue;
            }

            try {
                switch (op) {

                    case 1:
                        cadastrarReview(reviewDao, sc);
                        break;

                    case 2:
                        buscarReview(reviewDao, sc);
                        break;

                    case 3:
                        listarTodas(reviewDao);
                        break;

                    case 4:
                        listarPorFilme(reviewDao, sc);
                        break;

                    case 5:
                        listarPorUsuario(reviewDao, sc);
                        break;

                    case 6:
                        atualizarReview(reviewDao, sc);
                        break;

                    case 7:
                        excluirReview(reviewDao, sc);
                        break;

                    case 8:
                        ordenarAvaliacoes();
                        break;

                    case 9:
                        exibirHash(reviewDao);
                        break;

                    case 0:
                        System.out.println("Voltando...");
                        break;

                    default:
                        System.out.println("Opção inválida.");
                        break;
                }

            } catch (Exception e) {
                System.out.println("Erro na operação: " + e.getMessage());
            }

        } while (op != 0);
    }

    // ================= CREATE =================

    private static void cadastrarReview(ReviewDAO reviewDao, Scanner sc) {

        try {
            System.out.print("ID do Usuário: ");
            int uId = sc.nextInt();

            System.out.print("ID do Filme: ");
            int fId = sc.nextInt();

            System.out.print("Nota (0-10): ");
            byte rating = sc.nextByte();
            sc.nextLine();

            if (rating < 0 || rating > 10) {
                System.out.println("Nota inválida!");
                return;
            }

            System.out.print("Comentário: ");
            String note = sc.nextLine();

            Review r = new Review(uId, fId, rating, note);

            int id = reviewDao.create(r);

            System.out.println("Avaliação salva com ID: " + id);

        } catch (Exception e) {
            System.out.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

    // ================= READ =================

    private static void buscarReview(ReviewDAO reviewDao, Scanner sc) throws IOException {

        System.out.print("ID da avaliação: ");
        int id = sc.nextInt();
        sc.nextLine();

        Review r = reviewDao.read(id);

        if (r == null) {
            System.out.println("Avaliação não encontrada.");
        } else {
            mostrarReview(r);
        }
    }

    private static void mostrarReview(Review r) {
        System.out.println("\n--- AVALIAÇÃO ---");
        System.out.println("ID: " + r.getID());
        System.out.println("Usuário ID: " + r.getUserID());
        System.out.println("Filme ID: " + r.getFilmID());
        System.out.println("Nota: " + r.getRating());
        System.out.println("Comentário: " + r.getNote());
    }

    // ================= LISTAS COM HASH =================

    private static void listarPorFilme(ReviewDAO reviewDao, Scanner sc) throws IOException {

        System.out.print("ID do Filme: ");
        int filmID = sc.nextInt();
        sc.nextLine();

        System.out.println("\n>>> BUSCA VIA HASH + LISTA ENCADEADA <<<");

        reviewDao.listByFilm(filmID);
    }

    private static void listarPorUsuario(ReviewDAO reviewDao, Scanner sc) throws IOException {

        System.out.print("ID do Usuário: ");
        int userID = sc.nextInt();
        sc.nextLine();

        System.out.println("\n>>> BUSCA VIA HASH + LISTA ENCADEADA <<<");

        reviewDao.listByUser(userID);
    }

    private static void listarTodas(ReviewDAO reviewDao) throws IOException {
        reviewDao.listAll();
    }

    // ================= UPDATE =================

    private static void atualizarReview(ReviewDAO reviewDao, Scanner sc) throws Exception {

        System.out.print("ID da avaliação: ");
        int id = sc.nextInt();
        sc.nextLine();

        Review r = reviewDao.read(id);

        if (r == null) {
            System.out.println("Avaliação não encontrada.");
            return;
        }

        System.out.println("Dados atuais:");
        mostrarReview(r);

        System.out.print("Nova nota (-1 para manter): ");
        byte rating = sc.nextByte();
        sc.nextLine();

        if (rating != -1) {
            if (rating < 0 || rating > 10) {
                System.out.println("Nota inválida!");
                return;
            }
            r.setRating(rating);
        }

        System.out.print("Novo comentário (enter para manter): ");
        String note = sc.nextLine();

        if (!note.isEmpty()) {
            r.setNote(note);
        }

        if (reviewDao.update(r)) {
            System.out.println("Avaliação atualizada!");
        } else {
            System.out.println("Erro ao atualizar.");
        }
    }

    // ================= DELETE =================

    private static void excluirReview(ReviewDAO reviewDao, Scanner sc) throws IOException {

        System.out.print("ID da avaliação: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (reviewDao.delete(id)) {
            System.out.println("Avaliação excluída.");
        } else {
            System.out.println("Avaliação não encontrada.");
        }
    }

    // ================= SORT =================

    private static void ordenarAvaliacoes() {

        try {
            ExternalSortReview sort = new ExternalSortReview();
            sort.ordenar();

            System.out.println("\nAvaliações ordenadas com sucesso!");
            System.out.println("Arquivo: data/review_ordenado.bin");

        } catch (Exception e) {
            System.out.println("Erro ao ordenar: " + e.getMessage());
        }
    }

    // ================= HASH =================

    private static void exibirHash(ReviewDAO reviewDao) {

        try {
            System.out.println("\n--- HASH POR ID ---");
            reviewDao.exibirIndice();

            System.out.println("\n--- HASH POR FILME ---");
            reviewDao.exibirIndiceFilm();

            System.out.println("\n--- HASH POR USUÁRIO ---");
            reviewDao.exibirIndiceUser();

        } catch (Exception e) {
            System.out.println("Erro ao exibir hash: " + e.getMessage());
        }
    }
}