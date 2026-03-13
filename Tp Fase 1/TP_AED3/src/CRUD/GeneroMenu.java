package CRUD;

import java.io.IOException;
import java.util.Scanner;

import CRUD.dao.GenreDAO;
import CRUD.model.Genre;

public class GeneroMenu {

    public static void menuGenero(Scanner sc) throws Exception {

        GenreDAO genreDao = new GenreDAO();
        int op;

        do {

            System.out.println("\n=== MENU GÊNERO ===");
            System.out.println("1 - Cadastrar gênero");
            System.out.println("2 - Buscar gênero");
            System.out.println("3 - Listar gêneros");
            System.out.println("4 - Atualizar gênero");
            System.out.println("5 - Excluir gênero");
            System.out.println("0 - Voltar para o menu principal");
            System.out.print("Opção: ");

            op = sc.nextInt();
            sc.nextLine();

            switch (op) {

                case 1:
                    cadastrarGenero(genreDao, sc);
                    break;

                case 2:
                    buscarGenero(genreDao, sc);
                    break;

                case 3:
                    listarGenero(genreDao);
                    break;

                case 4:
                    atualizarGenero(genreDao, sc);
                    break;

                case 5:
                    excluirGenero(genreDao, sc);
                    break;
            }

        } while (op != 0);
    }

    private static void cadastrarGenero(GenreDAO genreDao, Scanner sc) throws IOException {

        System.out.print("Nome do gênero: ");
        String nome = sc.nextLine();

        Genre g = new Genre(0, nome);

        int id = genreDao.create(g);

        System.out.println("Gênero salvo com ID: " + id);
    }

    private static void buscarGenero(GenreDAO genreDao, Scanner sc) throws IOException {

        System.out.print("ID do gênero: ");
        int id = sc.nextInt();
        sc.nextLine();

        Genre g = genreDao.read(id);

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

    private static void listarGenero(GenreDAO genreDao) throws IOException {

        genreDao.listAll();
    }

    private static void atualizarGenero(GenreDAO genreDao, Scanner sc) throws IOException {

        System.out.print("ID do gênero: ");
        int id = sc.nextInt();
        sc.nextLine();

        Genre g = genreDao.read(id);

        if (g == null) {
            System.out.println("Gênero não encontrado.");
            return;
        }

        System.out.println("Nome atual: " + g.getName());

        System.out.print("Novo nome (enter para manter): ");
        String nome = sc.nextLine();

        if (!nome.isEmpty())
            g.setName(nome);

        boolean ok = genreDao.update(g);

        if (ok)
            System.out.println("Gênero atualizado!");
        else
            System.out.println("Erro ao atualizar.");
    }

    private static void excluirGenero(GenreDAO genreDao, Scanner sc) throws IOException {

        System.out.print("ID do gênero: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (genreDao.delete(id))
            System.out.println("Gênero excluído.");
        else
            System.out.println("Gênero não encontrado.");
    }
}