package CRUD;

import java.io.IOException;
import java.util.Scanner;

import CRUD.dao.ReviewDAO;
import CRUD.model.Review;

public class AvaliacaoMenu {

	public static void menuAvaliacao(Scanner sc) throws Exception {

		ReviewDAO reviewDao = new ReviewDAO();
		int op;

	    do {
	        System.out.println("\n=== MENU AVALIAÇÃO ===");
	        System.out.println("1 - Cadastrar avaliação");
	        System.out.println("2 - Buscar avaliação");
	        System.out.println("3 - Listar avaliações");
	        System.out.println("4 - Atualizar avaliação");
	        System.out.println("5 - Excluir avaliação");
	        System.out.println("0 - Voltar para o menu principal");
	        System.out.print("Opção: ");

	        op = sc.nextInt();
	        sc.nextLine();

	        switch (op) {
	            case 1: 
	            	cadastrarReview(reviewDao, sc); 
	            	break;

	            case 2: 
	            	buscarReview(reviewDao, sc); 
	            	break;

	            case 3:
	            	listarReview(reviewDao); 
	            	break;

	            case 4:
	            	atualizarReview(reviewDao, sc); 
	            	break;

	            case 5:
	            	excluirReview(reviewDao, sc);
	            	break;
	        }

	    } while (op != 0);
	}

	private static void cadastrarReview(ReviewDAO reviewDao, Scanner sc) throws IOException {

	    System.out.print("ID do Usuário: ");
	    int uId = sc.nextInt();

	    System.out.print("ID do Filme: ");
	    int fId = sc.nextInt();

	    System.out.print("Nota (1-5): ");
	    short rating = sc.nextShort();
	    sc.nextLine();

	    System.out.print("Comentário: ");
	    String note = sc.nextLine();

	    Review r = new Review(0, uId, fId, rating, note);

	    int id = reviewDao.create(r);

	    System.out.println("Avaliação salva com ID: " + id);
	}

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

	private static void listarReview(ReviewDAO reviewDao) throws IOException {
		reviewDao.listAll();
	}

	private static void atualizarReview(ReviewDAO reviewDao, Scanner sc) throws IOException {

	    System.out.print("ID da avaliação: ");
	    int id = sc.nextInt();
	    sc.nextLine();

	    Review r = reviewDao.read(id);

	    if (r == null) {
	        System.out.println("Avaliação não encontrada.");
	        return;
	    }

	    System.out.println("Dados atuais: Nota " + r.getRating() + " | Comentário: " + r.getNote());

	    System.out.print("Nova nota (0 para manter): ");
	    short rating = sc.nextShort();
	    sc.nextLine();

	    if (rating != 0) {
	        r.setRating(rating);
	    }

	    System.out.print("Novo comentário (enter para manter): ");
	    String note = sc.nextLine();

	    if (!note.isEmpty()) {
	        r.setNote(note);
	    }

	    if (reviewDao.update(r))
	        System.out.println("Avaliação atualizada!");
	    else
	        System.out.println("Erro ao atualizar.");
	}

	private static void excluirReview(ReviewDAO reviewDao, Scanner sc) throws IOException {

	    System.out.print("ID da avaliação: ");
	    int id = sc.nextInt();
	    sc.nextLine();

	    if (reviewDao.delete(id))
	        System.out.println("Avaliação excluída.");
	    else
	        System.out.println("Avaliação não encontrada.");
	}
}