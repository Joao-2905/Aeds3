package CRUD;

import CRUD.dao.UserDAO;
import CRUD.model.User;
import java.util.Scanner;

public class UserMenu {

    public static void opUsuario(UserDAO userDao, Scanner sc) throws Exception {

        int op;

        do {
            System.out.println("\n=== MENU USUARIO ===");
            System.out.println("1 - Cadastrar usuário");
            System.out.println("2 - Buscar usuário");
            System.out.println("3 - Listar usuários");
            System.out.println("4 - Atualizar usuário");
            System.out.println("5 - Excluir usuário");
            System.out.println("6 - Exibir hash extensível");
            System.out.println("0 - Voltar para o menu principal");
            System.out.print("Opção: ");

            op = sc.nextInt();
            sc.nextLine();

            switch (op) {
                case 1:
                    cadastrarUsuario(userDao, sc);
                    break;

                case 2:
                    buscarUsuario(userDao, sc);
                    break;

                case 3:
                    listarUsuario(userDao);
                    break;

                case 4:
                    atualizarUsuario(userDao, sc);
                    break;

                case 5:
                    excluirUsuario(userDao, sc);
                    break;

                case 6:
                    userDao.exibirIndice();
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

    private static void cadastrarUsuario(UserDAO userDao, Scanner sc) throws Exception {
        System.out.print("Nome: ");
        String name = sc.nextLine();

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.print("Senha: ");
        String password = sc.nextLine();

        User x = new User(0, name, email, password);
        int id = userDao.create(x);

        System.out.println("Salvo com ID: " + id);
    }

    private static void buscarUsuario(UserDAO userDao, Scanner sc) throws Exception {
        System.out.print("ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        User x = userDao.read(id);

        if (x == null) {
            System.out.println("Usuário não encontrado.");
        } else {
            mostrarUsuario(x);
        }
    }

    private static void mostrarUsuario(User x) {
        System.out.println("\n--- USER ---");
        System.out.println("ID: " + x.getID());
        System.out.println("Nome: " + x.getUsername());
        System.out.println("Email: " + x.getEmail());
        System.out.println("Senha: " + x.getPassword());
    }

    private static void listarUsuario(UserDAO userDao) throws Exception {
        userDao.listAll();
    }

    private static void atualizarUsuario(UserDAO userDao, Scanner sc) throws Exception {
        System.out.print("ID do usuário: ");
        int id = sc.nextInt();
        sc.nextLine();

        User x = userDao.read(id);

        if (x == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }

        System.out.println("\nUsuário atual:");
        mostrarUsuario(x);

        System.out.println("\nDigite os novos dados (enter para manter)");

        System.out.print("Nome (" + x.getUsername() + "): ");
        String name = sc.nextLine();
        if (!name.isEmpty()) {
            x.setUsername(name);
        }

        System.out.print("Email (" + x.getEmail() + "): ");
        String email = sc.nextLine();
        if (!email.isEmpty()) {
            x.setEmail(email);
        }

        System.out.print("Senha (" + x.getPassword() + "): ");
        String password = sc.nextLine();
        if (!password.isEmpty()) {
            x.setPassword(password);
        }

        boolean ok = userDao.update(x);

        if (ok) {
            System.out.println("Usuário atualizado.");
        } else {
            System.out.println("Erro ao atualizar.");
        }
    }

    private static void excluirUsuario(UserDAO userDao, Scanner sc) throws Exception {
        System.out.print("ID do usuário: ");
        int id = sc.nextInt();
        sc.nextLine();

        boolean ok = userDao.delete(id);

        if (ok) {
            System.out.println("Usuário excluído.");
        } else {
            System.out.println("Usuário não encontrado.");
        }
    }
}