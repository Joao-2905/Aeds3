package CRUD;

import java.util.Scanner;

import CRUD.controller.GenreController;
import CRUD.model.Genre;

public class GeneroMenu {

    public static void menuGenero(Scanner sc) throws Exception {

        GenreController controller = new GenreController(); // 🔥 mudou
        int op;

        do {

            System.out.println("\n=== MENU GÊNERO ===");
            System.out.println("1 - Cadastrar gênero");
            System.out.println("2 - Buscar gênero");
            System.out.println("3 - Listar gêneros");
            System.out.println("4 - Atualizar gênero");
            System.out.println("5 - Excluir gênero");
            System.out.println("6 - Exibir hash extensível"); // 🔥 NOVO
            System.out.println("0 - Voltar para o menu principal");
            System.out.print("Opção: ");

            op = sc.nextInt();
            sc.nextLine();

            switch (op) {

                case 1:
                    cadastrarGenero(controller, sc);
                    break;

                case 2:
                    buscarGenero(controller, sc);
                    break;

                case 3:
                    listarGenero(controller);
                    break;

                case 4:
                    atualizarGenero(controller, sc);
                    break;

                case 5:
                    excluirGenero(controller, sc);
                    break;

                case 6:
                    exibirHash(controller); // 🔥 NOVO
                    break;

                case 0:
                    System.out.println("Voltando...");
                    break;

                default:
                    System.out.println("Opção inválida.");
            }

        } while (op != 0);
    }

    private static void cadastrarGenero(GenreController controller, Scanner sc) throws Exception {

        System.out.print("Nome do gênero: ");
        String nome = sc.nextLine();

        int id = controller.createGenre(nome);

        System.out.println("Gênero salvo com ID: " + id);
    }

    private static void buscarGenero(GenreController controller, Scanner sc) throws Exception {

        System.out.print("ID do gênero: ");
        int id = sc.nextInt();
        sc.nextLine();

        Genre g = controller.readGenre(id);

        if (g == null)
            System.out.println("Gênero não encontrado.");
        else
            mostrarGenero(g);
    }

    private static void mostrarGenero(Genre g) {

        System.out.println("\n--- GÊNERO ---");
        System.out.println("ID: " + g.getID());
        System.out.println("Nome: " + g.getName());
    }

    private static void listarGenero(GenreController controller) throws Exception {

        controller.listGenres();
    }

    private static void atualizarGenero(GenreController controller, Scanner sc) throws Exception {

        System.out.print("ID do gênero: ");
        int id = sc.nextInt();
        sc.nextLine();

        Genre g = controller.readGenre(id);

        if (g == null) {
            System.out.println("Gênero não encontrado.");
            return;
        }

        System.out.println("Nome atual: " + g.getName());

        System.out.print("Novo nome (enter para manter): ");
        String nome = sc.nextLine();

        if (!nome.isEmpty())
            g.setName(nome);

        boolean ok = controller.updateGenre(g);

        if (ok)
            System.out.println("Gênero atualizado!");
        else
            System.out.println("Erro ao atualizar.");
    }

    private static void excluirGenero(GenreController controller, Scanner sc) throws Exception {

        System.out.print("ID do gênero: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (controller.deleteGenre(id))
            System.out.println("Gênero excluído.");
        else
            System.out.println("Gênero não encontrado.");
    }

    private static void exibirHash(GenreController controller) throws Exception {
        controller.exibirIndice(); // 🔥 igual filme
    }
}