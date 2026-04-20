package CRUD;

import java.io.IOException;
import java.util.Scanner;

import CRUD.dao.FilmDAO;
import CRUD.model.Film;

public class FilmeMenu {

    public static void opFilme(FilmDAO filmDao, Scanner sc) throws Exception {

        int op;

        do {
            System.out.println("\n=== MENU FILME ===");
            System.out.println("1 - Cadastrar filme");
            System.out.println("2 - Buscar filme");
            System.out.println("3 - Listar filmes");
            System.out.println("4 - Atualizar filme");
            System.out.println("5 - Excluir filme");
            System.out.println("6 - Exibir hash extensível");
            System.out.println("0 - Voltar para o menu principal");
            System.out.print("Opção: ");

            op = sc.nextInt();
            sc.nextLine();

            switch (op) {
                case 1:
                    cadastrarFilme(filmDao, sc);
                    break;

                case 2:
                    buscarFilme(filmDao, sc);
                    break;

                case 3:
                    listarFilme(filmDao);
                    break;

                case 4:
                    atualizarFilme(filmDao, sc);
                    break;

                case 5:
                    excluirFilme(filmDao, sc);
                    break;

                case 6:
                    filmDao.exibirIndice();
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

    private static void cadastrarFilme(FilmDAO filmDao, Scanner sc) throws IOException {
        System.out.print("Título: ");
        String titulo = sc.nextLine();

        System.out.print("Diretor: ");
        String diretor = sc.nextLine();

        System.out.print("Descrição: ");
        String descricao = sc.nextLine();

        System.out.print("Quantos gêneros deseja adicionar? ");
        int num = sc.nextInt();
        sc.nextLine();

        int[] genres = new int[num];
        for (int i = 0; i < num; i++) {
            System.out.print("Digite o ID do gênero " + (i + 1) + ": ");
            genres[i] = sc.nextInt();
            sc.nextLine();
        }

        Film f = new Film(0, titulo, diretor, descricao, genres);
        int id = filmDao.create(f);

        System.out.println("Salvo com ID: " + id);
    }

    private static void buscarFilme(FilmDAO filmDao, Scanner sc) throws IOException {
        System.out.print("ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        Film f = filmDao.read(id);

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
        System.out.println("Diretor: " + f.getDirector());
        System.out.println("Descrição: " + f.getDescription());

        System.out.print("Gênero(os) (IDs): ");
        int[] genres = f.getGenreID();

        if (genres.length > 0) {
            for (int i = 0; i < genres.length; i++) {
                System.out.print(genres[i] + (i < genres.length - 1 ? ", " : ""));
            }
        } else {
            System.out.print("Nenhum gênero cadastrado.");
        }
        System.out.println();
    }

    private static void listarFilme(FilmDAO filmDao) throws IOException {
        filmDao.listAll();
    }

    private static void atualizarFilme(FilmDAO filmDao, Scanner sc) throws IOException {
        System.out.print("ID do filme: ");
        int id = sc.nextInt();
        sc.nextLine();

        Film f = filmDao.read(id);

        if (f == null) {
            System.out.println("Filme não encontrado.");
            return;
        }

        System.out.println("\nDados atuais: " + f.getTitle() + " | Diretor: " + f.getDirector());
        System.out.println("Digite os novos dados (deixe vazio para manter o atual)");

        System.out.print("Título (" + f.getTitle() + "): ");
        String title = sc.nextLine();
        if (!title.isEmpty()) {
            f.setTitle(title);
        }

        System.out.print("Diretor (" + f.getDirector() + "): ");
        String director = sc.nextLine();
        if (!director.isEmpty()) {
            f.setDirector(director);
        }

        System.out.print("Descrição (" + f.getDescription() + "): ");
        String desc = sc.nextLine();
        if (!desc.isEmpty()) {
            f.setDescription(desc);
        }

        System.out.print("Quantos gêneros deseja informar? ");
        int num = sc.nextInt();
        sc.nextLine();

        int[] genres = new int[num];
        for (int i = 0; i < num; i++) {
            System.out.print("Digite o ID do gênero " + (i + 1) + ": ");
            genres[i] = sc.nextInt();
            sc.nextLine();
        }

        f.setGenreID(genres);

        boolean ok = filmDao.update(f);

        if (ok) {
            System.out.println("Filme atualizado com sucesso!");
        } else {
            System.out.println("Erro ao atualizar o filme.");
        }
    }

    private static void excluirFilme(FilmDAO filmDao, Scanner sc) throws IOException {
        System.out.print("ID do filme: ");
        int id = sc.nextInt();
        sc.nextLine();

        boolean ok = filmDao.delete(id);

        if (ok) {
            System.out.println("Filme excluído.");
        } else {
            System.out.println("Filme não encontrado.");
        }
    }
}