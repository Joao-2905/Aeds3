package CRUD;

import CRUD.dao.*;

import java.util.*;

public class Main {
	
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		
		UserDAO userDao = new UserDAO();
		FilmDAO  filmDao = new FilmDAO();
		GenreDAO genreDao = new GenreDAO();
		ReviewDAO reviewDao = new ReviewDAO();
		
		opcao1(userDao, filmDao, genreDao, reviewDao, sc);

        sc.close();
	}
	
	private static void opcao1 (UserDAO userDao, FilmDAO  filmDao, GenreDAO genreDao, ReviewDAO reviewDao, Scanner sc) throws Exception {
		
		int op;
		
		do {

            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1 - Manipular usuários");
            System.out.println("2 - Manipular filmes");
            System.out.println("3 - Manipular gêneros");
            System.out.println("4 - Manipular avaliações");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");
            
            op = sc.nextInt();
            sc.nextLine();
            
            switch (op) {

	            case 1:
	                UserMenu.opUsuario(userDao, sc);
	                break;
	
	            case 2:
	                FilmeMenu.opFilme(filmDao, sc);
	                break;
	
	            case 3:
	                GeneroMenu.opGenero(genreDao, sc);
	                break;
	
	            case 4:
	            	AvaliacaoMenu.opAvaliacao(reviewDao, sc);
	                break;
            }
            
        } while (op != 0);
	}	
}