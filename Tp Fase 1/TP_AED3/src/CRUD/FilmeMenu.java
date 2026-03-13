package CRUD;

import java.util.Scanner;

import CRUD.model.Film;
import CRUD.controller.FilmController;

public class FilmeMenu {

	public static void menuFilme(Scanner sc) throws Exception {

		FilmController controller = new FilmController();
		int op;

		do {

            System.out.println("\n=== MENU FILME ===");
            System.out.println("1 - Cadastrar filme");
            System.out.println("2 - Buscar filme");
            System.out.println("3 - Listar filmes");
            System.out.println("4 - Atualizar filme");
            System.out.println("5 - Excluir filme");
            System.out.println("0 - Voltar para o menu principal");
            System.out.print("Opção: ");

            op = sc.nextInt();
            sc.nextLine();

            switch (op) {
	
	            case 1:
	                cadastrarFilme(controller, sc);
	                break;
	
	            case 2:
	                buscarFilme(controller, sc);
	                break;
	
	            case 3:
	                listarFilme(controller);
	                break;
	
	            case 4:
	                atualizarFilme(controller, sc);
	                break;
	
	            case 5:
	                excluirFilme(controller, sc);
	                break;
            }

        } while (op != 0);
	}

	private static void cadastrarFilme(FilmController controller, Scanner sc) throws Exception {

		System.out.print("Título: ");
	    String title = sc.nextLine();

	    System.out.print("Descrição: ");
	    String description = sc.nextLine();

	    System.out.print("Data de lançamento (AAAAMMDD): ");
	    int releaseDate = sc.nextInt();
	    sc.nextLine();

	    System.out.print("Quantos diretores? ");
	    int qtd = sc.nextInt();
	    sc.nextLine();

	    String[] directors = new String[qtd];

	    for(int i = 0; i < qtd; i++) {
	    	System.out.print("Diretor " + (i+1) + ": ");
	    	directors[i] = sc.nextLine();
	    }

	    int id = controller.createFilm(title, description, releaseDate, directors);

	    System.out.println("Filme cadastrado com ID: " + id);
	}

	private static void buscarFilme(FilmController controller, Scanner sc) throws Exception {

		System.out.print("ID do filme: ");
        int id = sc.nextInt();
        sc.nextLine();

        Film f = controller.readFilm(id);

        if (f == null) {
            System.out.println("Filme não encontrado.");
        } else {
            mostrarFilme(f);
        }
	}

	private static void mostrarFilme(Film f) {

	    System.out.println("\n--- FILME ---");

	    System.out.println("ID: " + f.getID());
	    System.out.println("Título: " + f.getTitle());
	    System.out.println("Descrição: " + f.getDescription());
	    int d = f.getReleaseDate();

	    int ano = d / 10000;
	    int mes = (d / 100) % 100;
	    int dia = d % 100;

	    String dataFormatada = dia + "/" + mes + "/" + ano;

	    System.out.println("Data de lançamento: " + dataFormatada);

	    System.out.println("Rating médio: " + f.getRating());
	    System.out.println("Total de avaliações: " + f.getTotalReviews());

	    System.out.print("Diretores: ");

	    String[] directors = f.getDirectors();

	    if (directors != null && directors.length > 0) {

	        for (int i = 0; i < directors.length; i++) {

	            System.out.print(directors[i]);

	            if (i < directors.length - 1) {
	                System.out.print(", ");
	            }
	        }

	    } else {
	        System.out.print("Nenhum diretor cadastrado");
	    }

	    System.out.println();
	}

	private static void listarFilme(FilmController controller) throws Exception {
		controller.listFilms();
	}

	private static void atualizarFilme(FilmController controller, Scanner sc) throws Exception {

		System.out.print("ID do filme: ");
	    int id = sc.nextInt();
	    sc.nextLine();

	    Film f = controller.readFilm(id);

	    if (f == null) {
	        System.out.println("Filme não encontrado.");
	        return;
	    }

	    System.out.println("Título atual: " + f.getTitle());
	    System.out.print("Novo título (enter para manter): ");
	    String title = sc.nextLine();
	    if(!title.isEmpty()) f.setTitle(title);

	    System.out.println("Descrição atual: " + f.getDescription());
	    System.out.print("Nova descrição (enter para manter): ");
	    String desc = sc.nextLine();
	    if(!desc.isEmpty()) f.setDescription(desc);

	    System.out.println("Data atual: " + f.getReleaseDate());
	    System.out.print("Nova data (0 para manter): ");
	    int date = sc.nextInt();
	    sc.nextLine();

	    if(date != 0) {
	        f.setReleaseDate(date);
	    }

	    System.out.print("Alterar diretores? (s/n): ");
	    String resp = sc.nextLine();

	    if(resp.equalsIgnoreCase("s")) {

	    	System.out.print("Quantos diretores? ");
	    	int qtd = sc.nextInt();
	    	sc.nextLine();

	    	String[] directors = new String[qtd];

	    	for(int i = 0; i < qtd; i++) {
	    		System.out.print("Diretor " + (i+1) + ": ");
	    		directors[i] = sc.nextLine();
	    	}

	    	f.setDirectors(directors);
	    }

	    boolean ok = controller.updateFilm(f);

	    if(ok)
	    	System.out.println("Filme atualizado!");
	    else
	    	System.out.println("Erro ao atualizar.");
	}

	private static void excluirFilme(FilmController controller, Scanner sc) throws Exception {

		System.out.print("ID do filme: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (controller.deleteFilm(id))
            System.out.println("Filme excluído.");
        else
            System.out.println("Filme não encontrado.");
	}
}