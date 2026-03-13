package CRUD;

import CRUD.controller.UserController;
import CRUD.model.User;

import java.util.Scanner;

public class UserMenu {

    public static void opUsuario(Scanner sc, User usuarioLogado) throws Exception {

        if(!usuarioLogado.isAdministrator()) {
            System.out.println("Apenas administradores podem acessar este menu.");
            return;
        }

        UserController controller = new UserController();

        int op;

        do {

            System.out.println("\n=== ADMINISTRAÇÃO DE USUÁRIOS ===");

            System.out.println("1 - Listar usuários");
            System.out.println("2 - Criar usuário");
            System.out.println("3 - Editar usuário");
            System.out.println("4 - Remover usuário");
            System.out.println("5 - Promover a administrador");
            System.out.println("6 - Remover administrador");
            System.out.println("0 - Voltar");

            System.out.print("Opção: ");
            op = sc.nextInt();
            sc.nextLine();

            switch(op) {

                case 1:
                    controller.listUsers();
                    break;

                case 2:

                    System.out.print("Username: ");
                    String username = sc.nextLine();

                    System.out.print("Email: ");
                    String email = sc.nextLine();

                    System.out.print("Senha: ");
                    String senha = sc.nextLine();

                    controller.createUser(username, email, senha, false);

                    break;

                case 3:

                    System.out.print("ID do usuário: ");
                    int id = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Novo username: ");
                    String newUsername = sc.nextLine();

                    System.out.print("Novo email: ");
                    String newEmail = sc.nextLine();

                    controller.updateUser(id, newUsername, newEmail);

                    break;

                case 4:

                    System.out.print("ID do usuário: ");
                    int delID = sc.nextInt();
                    sc.nextLine();

                    controller.deleteUser(delID);

                    break;

                case 5:

                    System.out.print("ID do usuário: ");
                    int promoteID = sc.nextInt();
                    sc.nextLine();

                    controller.promoteAdmin(promoteID);

                    break;

                case 6:

                    System.out.print("ID do usuário: ");
                    int removeID = sc.nextInt();
                    sc.nextLine();

                    controller.removeAdmin(removeID, usuarioLogado);

                    break;
            }

        } while(op != 0);
    }
}