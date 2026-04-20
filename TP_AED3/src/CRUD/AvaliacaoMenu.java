package CRUD;

import java.util.Scanner;

import CRUD.dao.ReviewDAO;
import CRUD.model.Review;

public class AvaliacaoMenu {

    public static void opAvaliacao(ReviewDAO reviewDao, Scanner sc) throws Exception {

        int op;

        do {
            System.out.println("\n=== MENU AVALIAÇÃO ===");
            System.out.println("1 - Cadastrar avaliação");
            System.out.println("2 - Buscar avaliação");
            System.out.println("3 - Listar todas as avaliações");
            System.out.println("4 - Atualizar avaliação");
            System.out.println("5 - Excluir avaliação");
            System.out.println("6 - Exibir hash extensível");
            System.out.println("7 - Listar avaliações por filme");
            System.out.println("8 - Listar avaliações por usuário");
            System.out.println("0 - Voltar para o menu principal");
            System.out.print("Opção: ");

            op = sc.nextInt();
            sc.nextLine();

            switch (op) {
                case 1:
                    cadastrarAvaliacao(reviewDao, sc);
                    break;

                case 2:
                    buscarAvaliacao(reviewDao, sc);
                    break;

                case 3:
                    listarAvaliacoes(reviewDao);
                    break;

                case 4:
                    atualizarAvaliacao(reviewDao, sc);
                    break;

                case 5:
                    excluirAvaliacao(reviewDao, sc);
                    break;

                case 6:
                    reviewDao.exibirIndice();
                    break;

                case 7:
                    listarPorFilme(reviewDao, sc);
                    break;

                case 8:
                    listarPorUsuario(reviewDao, sc);
                    break;

                case 0:
                    System.out.println("Voltando...");
                    break;

                default:
                    System.out.println("Opção inválida.");
                    break;
            }

        } while (op != 0);
    }

    private static void cadastrarAvaliacao(ReviewDAO reviewDao, Scanner sc) {
        try {
            System.out.print("ID do usuário: ");
            int userID = sc.nextInt();
            sc.nextLine();

            System.out.print("ID do filme: ");
            int filmID = sc.nextInt();
            sc.nextLine();

            System.out.print("Nota (0 a 10): ");
            byte rating = sc.nextByte();
            sc.nextLine();

            System.out.print("Comentário: ");
            String note = sc.nextLine();

            Review r = new Review(userID, filmID, rating, note);
            int id = reviewDao.create(r);

            System.out.println("Avaliação salva com ID: " + id);

        } catch (Exception e) {
            System.out.println("Erro ao cadastrar avaliação: " + e.getMessage());
        }
    }

    private static void buscarAvaliacao(ReviewDAO reviewDao, Scanner sc) {
        try {
            System.out.print("ID da avaliação: ");
            int id = sc.nextInt();
            sc.nextLine();

            Review r = reviewDao.read(id);

            if (r == null) {
                System.out.println("Avaliação não encontrada.");
            } else {
                mostrarAvaliacao(r);
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar avaliação: " + e.getMessage());
        }
    }

    private static void mostrarAvaliacao(Review r) {
        System.out.println("\n--- AVALIAÇÃO ---");
        System.out.println("ID: " + r.getID());
        System.out.println("Usuário ID: " + r.getUserID());
        System.out.println("Filme ID: " + r.getFilmID());
        System.out.println("Nota: " + r.getRating());
        System.out.println("Comentário: " + r.getNote());
    }

    private static void listarAvaliacoes(ReviewDAO reviewDao) {
        try {
            reviewDao.listAll();
        } catch (Exception e) {
            System.out.println("Erro ao listar avaliações: " + e.getMessage());
        }
    }

    private static void atualizarAvaliacao(ReviewDAO reviewDao, Scanner sc) {
        try {
            System.out.print("ID da avaliação: ");
            int id = sc.nextInt();
            sc.nextLine();

            Review r = reviewDao.read(id);

            if (r == null) {
                System.out.println("Avaliação não encontrada.");
                return;
            }

            System.out.println("\nDados atuais:");
            mostrarAvaliacao(r);

            System.out.println("\nDigite os novos dados (enter para manter os atuais)");

            System.out.print("Usuário ID (" + r.getUserID() + "): ");
            String novoUser = sc.nextLine();
            if (!novoUser.isEmpty()) {
                r.setUserID(Integer.parseInt(novoUser));
            }

            System.out.print("Filme ID (" + r.getFilmID() + "): ");
            String novoFilm = sc.nextLine();
            if (!novoFilm.isEmpty()) {
                r.setFilmID(Integer.parseInt(novoFilm));
            }

            System.out.print("Nota (" + r.getRating() + "): ");
            String novaNota = sc.nextLine();
            if (!novaNota.isEmpty()) {
                r.setRating(Byte.parseByte(novaNota));
            }

            System.out.print("Comentário (" + r.getNote() + "): ");
            String novoComentario = sc.nextLine();
            if (!novoComentario.isEmpty()) {
                r.setNote(novoComentario);
            }

            boolean ok = reviewDao.update(r);

            if (ok) {
                System.out.println("Avaliação atualizada com sucesso.");
            } else {
                System.out.println("Erro ao atualizar avaliação.");
            }

        } catch (Exception e) {
            System.out.println("Erro ao atualizar avaliação: " + e.getMessage());
        }
    }

    private static void excluirAvaliacao(ReviewDAO reviewDao, Scanner sc) {
        try {
            System.out.print("ID da avaliação: ");
            int id = sc.nextInt();
            sc.nextLine();

            boolean ok = reviewDao.delete(id);

            if (ok) {
                System.out.println("Avaliação excluída.");
            } else {
                System.out.println("Avaliação não encontrada.");
            }

        } catch (Exception e) {
            System.out.println("Erro ao excluir avaliação: " + e.getMessage());
        }
    }

    private static void listarPorFilme(ReviewDAO reviewDao, Scanner sc) {
        try {
            System.out.print("ID do filme: ");
            int filmID = sc.nextInt();
            sc.nextLine();

            reviewDao.listByFilm(filmID);

        } catch (Exception e) {
            System.out.println("Erro ao listar avaliações por filme: " + e.getMessage());
        }
    }

    private static void listarPorUsuario(ReviewDAO reviewDao, Scanner sc) {
        try {
            System.out.print("ID do usuário: ");
            int userID = sc.nextInt();
            sc.nextLine();

            reviewDao.listByUser(userID);

        } catch (Exception e) {
            System.out.println("Erro ao listar avaliações por usuário: " + e.getMessage());
        }
    }
}