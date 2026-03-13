package CRUD;

import CRUD.controller.UserController;
import CRUD.model.User;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws Exception {

	    Scanner sc = new Scanner(System.in);
	    UserController userController = new UserController();

	    while(true) {

	        int op = telaInicial(sc);

	        if(op == 1) {

	            User usuarioLogado = telaLogin(userController, sc);

	            if(usuarioLogado != null) {
	                boolean logout = menuPrincipal(usuarioLogado, sc);

	                if(!logout) {
	                    break;
	                }
	            }

	        } else if(op == 2) {

	            cadastrarUsuario(userController, sc);

	        } else if(op == 0) {

	            break;
	        }
	    }

	    System.out.println("Sistema encerrado.");
	    sc.close();
	}

	private static int telaInicial(Scanner sc) {

	    System.out.println("\n=== SISTEMA ===");
	    System.out.println("1 - Login");
	    System.out.println("2 - Cadastrar usuário");
	    System.out.println("0 - Sair");
	    System.out.print("Opção: ");

	    int op = sc.nextInt();
	    sc.nextLine();

	    return op;
	}

    private static User telaLogin(UserController controller, Scanner sc) throws Exception {

        System.out.println("\n=== LOGIN ===");

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Senha: ");
        String senha = sc.nextLine();

        User user = controller.login(email, senha);

        if(user == null) {
            System.out.println("Email ou senha incorretos.");
            return null;
        }

        System.out.println("Login realizado com sucesso.");
        return user;
    }

    private static void cadastrarUsuario(UserController controller, Scanner sc) throws Exception {

        System.out.println("\n=== CADASTRO DE USUÁRIO ===");

        System.out.print("Nome: ");
        String nome = sc.nextLine();

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Senha: ");
        String senha = sc.nextLine();

        int id = controller.createUser(nome, email, senha, false);

        System.out.println("Usuário cadastrado com ID: " + id);
    }

    private static boolean menuPrincipal(User usuarioLogado, Scanner sc) throws Exception {

        int op;

        do {

            System.out.println("\n=== MENU PRINCIPAL ===");

            if(usuarioLogado.isAdministrator()) {

                System.out.println("1 - Administração de usuários");
                System.out.println("2 - Administração de gêneros");
                System.out.println("3 - Administração de filmes");
                System.out.println("4 - Minha conta");

            } else {

                System.out.println("1 - Minha conta");

            }

            System.out.println("9 - Logout");
            System.out.println("0 - Sair do sistema");

            System.out.print("Opção: ");
            op = sc.nextInt();
            sc.nextLine();

            if(usuarioLogado.isAdministrator()) {

            	switch(op) {

                case 1:
                    UserMenu.opUsuario(sc, usuarioLogado);
                    break;

                case 2:
                    GeneroMenu.menuGenero(sc);
                    break;

                case 3:
                    FilmeMenu.menuFilme(sc);
                    break;

                case 4:
                    AccountMenu.menuConta(usuarioLogado, sc);
                    break;
            }

            } else {

                switch(op) {

                    case 1:
                        AccountMenu.menuConta(usuarioLogado, sc);
                        break;
                }
            }

            if(op == 9) {
                System.out.println("Logout realizado.");
                return true;
            }

        } while(op != 0);

        return false;
    }
}