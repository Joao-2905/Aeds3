package CRUD;

import java.util.Scanner;

import CRUD.dao.ReviewDAO;
import CRUD.model.Review;
import CRUD.model.User;

public class UserReviewMenu {

    public static void menu(User usuarioLogado, Scanner sc) throws Exception {

        ReviewDAO reviewDAO = new ReviewDAO();

        int op;

        do {
            System.out.println("\n=== MINHAS AVALIAÇÕES ===");
            System.out.println("1 - Avaliar filme");
            System.out.println("2 - Ver minhas avaliações");
            System.out.println("3 - Ver avaliações de um filme");
            System.out.println("0 - Voltar");
            System.out.print("Opção: ");

            op = sc.nextInt();
            sc.nextLine();

            switch(op) {

                case 1:
                    avaliar(usuarioLogado, reviewDAO, sc);
                    break;

                case 2:
                    reviewDAO.listByUser(usuarioLogado.getID()); // 🔥 automático
                    break;

                case 3:
                    System.out.print("ID do Filme: ");
                    int filmID = sc.nextInt();
                    sc.nextLine();

                    reviewDAO.listByFilm(filmID);
                    break;
            }

        } while(op != 0);
    }

    private static void avaliar(User usuarioLogado, ReviewDAO reviewDAO, Scanner sc) {

        try {
            System.out.print("ID do Filme: ");
            int filmID = sc.nextInt();

            System.out.print("Nota (0-10): ");
            byte rating = sc.nextByte();
            sc.nextLine();

            if (rating < 0 || rating > 10) {
                System.out.println("Nota inválida!");
                return;
            }

            System.out.print("Comentário: ");
            String note = sc.nextLine();

            // 🔥 NÃO PEDE USER ID
            Review r = new Review(usuarioLogado.getID(), filmID, rating, note);

            int id = reviewDAO.create(r);

            System.out.println("Avaliação criada com ID: " + id);

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}