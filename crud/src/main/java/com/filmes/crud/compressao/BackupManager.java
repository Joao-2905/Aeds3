package com.filmes.crud.compressao;

import java.io.*;
import java.nio.file.Files;

public class BackupManager {

    public static void criarBackup(String pastaData, String arquivoBackup) throws IOException {

        File pasta = new File(pastaData);

        if (!pasta.exists() || !pasta.isDirectory()) {
            throw new IOException("Pasta não encontrada: " + pastaData);
        }

        File[] arquivos = pasta.listFiles((dir, nome) -> nome.toLowerCase().endsWith(".bin"));

        if (arquivos == null || arquivos.length == 0) {
            throw new IOException("Nenhum arquivo .bin encontrado.");
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(arquivoBackup))) {

            dos.writeInt(arquivos.length);

            for (File arquivo : arquivos) {

                byte[] dados = Files.readAllBytes(arquivo.toPath());

                dos.writeUTF(arquivo.getName());
                dos.writeLong(dados.length);
                dos.write(dados);

                System.out.println("Adicionado ao backup: " + arquivo.getName() + " (" + dados.length + " bytes)");
            }
        }

        System.out.println("\nBackup criado com sucesso:");
        System.out.println(arquivoBackup);
    }

    public static void restaurarBackup(String arquivoBackup, String pastaDestino) throws IOException {

        File pasta = new File(pastaDestino);

        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(arquivoBackup))) {

            int quantidadeArquivos = dis.readInt();

            for (int i = 0; i < quantidadeArquivos; i++) {

                String nomeArquivo = dis.readUTF();

                long tamanho = dis.readLong();

                byte[] dados = new byte[(int) tamanho];

                dis.readFully(dados);

                File destino = new File(pastaDestino + File.separator + nomeArquivo);

                Files.write(destino.toPath(), dados);

                System.out.println("Restaurado: " + nomeArquivo + " (" + tamanho + " bytes)");
            }
        }

        System.out.println("\nBackup restaurado com sucesso.");
    }

    public static void criarBackupHuffman(String pastaData) throws IOException {

        criarBackup(pastaData, "backup.dat");

        Huffman.compactar("backup.dat", "backup_huffman.dat");

        imprimirTaxa("backup.dat", "backup_huffman.dat", "Huffman");
    }

    public static void restaurarBackupHuffman(String pastaDestino) throws IOException {

        System.out.println("Iniciando restauração Huffman...");

        Huffman.descompactar("backup_huffman.dat", "backup_restaurado_huffman.dat");

        System.out.println("Backup Huffman descompactado.");

        restaurarBackup("backup_restaurado_huffman.dat",pastaDestino);

        System.out.println("Backup Huffman restaurado na pasta: " + pastaDestino);
    }

    public static void criarBackupLZW(String pastaData) throws IOException {

        criarBackup(pastaData, "backup.dat");

        LZW.compactar("backup.dat", "backup_lzw.dat");

        imprimirTaxa("backup.dat", "backup_lzw.dat", "LZW");
    }

    public static void restaurarBackupLZW(String pastaDestino) throws IOException {

        LZW.descompactar("backup_lzw.dat", "backup_restaurado_lzw.dat");

        restaurarBackup("backup_restaurado_lzw.dat", pastaDestino);
    }

    private static void imprimirTaxa(String original, String compactado, String algoritmo) {

        File arqOriginal = new File(original);
        File arqCompactado = new File(compactado);

        long tamanhoOriginal = arqOriginal.length();
        long tamanhoCompactado = arqCompactado.length();

        double taxa = (1.0 - ((double) tamanhoCompactado / tamanhoOriginal)) * 100.0;

        System.out.println("\nCompactação " + algoritmo + " concluída.");
        System.out.println("Arquivo original: " + original);
        System.out.println("Tamanho original: " + tamanhoOriginal + " bytes");
        System.out.println("Arquivo compactado: " + compactado);
        System.out.println("Tamanho compactado: " + tamanhoCompactado + " bytes");
        System.out.printf("Taxa de compressão: %.2f%%\n", taxa);
    }
}