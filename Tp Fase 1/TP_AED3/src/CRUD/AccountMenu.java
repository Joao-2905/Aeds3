package CRUD;

import CRUD.controller.UserController;
import CRUD.model.User;

import java.util.Scanner;

public class AccountMenu {

    public static void menuConta(User usuarioLogado, Scanner sc) throws Exception {

        UserController controller = new UserController();

        int op;

        do {

            System.out.println("\n=== MINHA CONTA ===");
            System.out.println("1 - Alterar senha");
            System.out.println("2 - Ver meus dados");
            System.out.println("0 - Voltar");

            System.out.print("Opção: ");
            op = sc.nextInt();
            sc.nextLine();

            switch(op) {

                case 1:

                    System.out.print("Nova senha: ");
                    String novaSenha = sc.nextLine();

                    if(controller.changePassword(usuarioLogado, novaSenha)) {
                        usuarioLogado.setPassword(novaSenha);
                        System.out.println("Senha alterada com sucesso.");
                    } else {
                        System.out.println("Erro ao alterar senha.");
                    }

                    break;

                case 2:

                    System.out.println("\n=== DADOS DO USUÁRIO ===");
                    System.out.println("ID: " + usuarioLogado.getID());
                    System.out.println("Username: " + usuarioLogado.getUsername());
                    System.out.println("Email: " + usuarioLogado.getEmail());
                    System.out.println("Senha: " + usuarioLogado.getPassword());
                    System.out.println("Administrador: " + usuarioLogado.isAdministrator());

                    break;
            }

        } while(op != 0);
    }
}