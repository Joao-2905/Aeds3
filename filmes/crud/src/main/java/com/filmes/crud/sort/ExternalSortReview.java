package com.filmes.crud.sort;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.filmes.crud.model.Review;
import com.filmes.crud.FileManeger;

public class ExternalSortReview {

    private final String FILE = "data/review.bin";
    private final String SORTED_FILE = "data/review_ordenado.bin";
    private final int RUN_SIZE = 4; // tamanho do bloco (igual prova)

    //  MÉTODO PRINCIPAL
    public void ordenar() throws Exception {
        garantirPastaData();

        int totalRuns = gerarRuns();

        if (totalRuns == 0) {
            new RandomAccessFile(SORTED_FILE, "rw").close();
            System.out.println("Nenhuma avaliação ativa encontrada para ordenar.");
            return;
        }

        intercalarRuns(totalRuns);
        limparArquivosTemporarios();

        System.out.println("Ordenação completa finalizada.");
    }

    // LISTAGEM ORDENADA CRESCENTE
    public ArrayList<Review> listarOrdenadoCrescente() throws Exception {
        ordenar();
        return lerArquivoOrdenado(false);
    }

    //  LISTAGEM ORDENADA DECRESCENTE
    public ArrayList<Review> listarOrdenadoDecrescente() throws Exception {
        ordenar();
        return lerArquivoOrdenado(true);
    }

    // =========================
    //  FASE 1 - GERAR RUNS
    // =========================
    private int gerarRuns() throws Exception {

        RandomAccessFile raf = FileManeger.open(FILE);

        if (raf.length() <= 4) {
            raf.close();
            return 0;
        }

        raf.seek(4);

        ArrayList<Review> buffer = new ArrayList<>();
        int runIndex = 0;

        while (raf.getFilePointer() < raf.length()) {

            long pos = raf.getFilePointer();

            int recordSize = raf.readInt();
            boolean active = raf.readBoolean();

            if (!active) {
                raf.seek(pos + 4 + recordSize);
                continue;
            }

            int id = raf.readInt();
            int userID = raf.readInt();
            int filmID = raf.readInt();
            byte rating = raf.readByte();

            short size = raf.readShort();
            byte[] noteBytes = new byte[size];
            raf.readFully(noteBytes);
            String note = new String(noteBytes, StandardCharsets.UTF_8);

            buffer.add(new Review(id, userID, filmID, rating, note));

            if (buffer.size() == RUN_SIZE) {
                salvarRun(buffer, runIndex++);
                buffer.clear();
            }

            raf.seek(pos + 4 + recordSize);
        }

        if (!buffer.isEmpty()) {
            salvarRun(buffer, runIndex++);
        }

        raf.close();
        return runIndex;
    }

    // 🔥 SALVAR RUN ORDENADA
    private void salvarRun(ArrayList<Review> buffer, int index) throws Exception {

        buffer.sort(Comparator
                .comparingInt(Review::getRating)
                .thenComparingInt(Review::getID));

        RandomAccessFile raf = new RandomAccessFile("data/run" + index + ".bin", "rw");
        raf.setLength(0);

        for (Review r : buffer) {
            escrever(raf, r);
        }

        raf.close();
        System.out.println("Run" + index + " criada.");
    }

    // =========================
    // 🔥 FASE 2 - INTERCALAÇÃO
    // =========================
    private void intercalarRuns(int totalRuns) throws Exception {

        Queue<String> fila = new LinkedList<>();

        // adiciona todos runs na fila
        for (int i = 0; i < totalRuns; i++) {
            fila.add("data/run" + i + ".bin");
        }

        int tempIndex = 0;

        // 🔥 merge até sobrar 1 arquivo
        while (fila.size() > 1) {

            String f1 = fila.poll();
            String f2 = fila.poll();

            String temp = "data/temp" + tempIndex++ + ".bin";

            merge2(f1, f2, temp);

            fila.add(temp);
        }

        // arquivo final
        String finalFile = fila.poll();

        File destino = new File(SORTED_FILE);
        File origem = new File(finalFile);

        if (destino.exists()) {
            destino.delete();
        }

        if (!origem.renameTo(destino)) {
            copiarArquivo(origem, destino);
            origem.delete();
        }

        System.out.println("Arquivo final gerado: review_ordenado.bin");
    }

    // MERGE DE 2 ARQUIVOS
    private void merge2(String file1, String file2, String output) throws Exception {

        RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
        RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
        RandomAccessFile out = new RandomAccessFile(output, "rw");

        out.setLength(0);

        Review r1 = lerProximo(raf1);
        Review r2 = lerProximo(raf2);

        while (r1 != null && r2 != null) {

            if (compararReviews(r1, r2) <= 0) {
                escrever(out, r1);
                r1 = lerProximo(raf1);
            } else {
                escrever(out, r2);
                r2 = lerProximo(raf2);
            }
        }

        while (r1 != null) {
            escrever(out, r1);
            r1 = lerProximo(raf1);
        }

        while (r2 != null) {
            escrever(out, r2);
            r2 = lerProximo(raf2);
        }

        raf1.close();
        raf2.close();
        out.close();
    }

    // COMPARAÇÃO USADA NA INTERCALAÇÃO
    private int compararReviews(Review r1, Review r2) {
        int cmp = Byte.compare(r1.getRating(), r2.getRating());

        if (cmp != 0) {
            return cmp;
        }

        return Integer.compare(r1.getID(), r2.getID());
    }

    // =========================
    // LEITURA
    // =========================
    private Review lerProximo(RandomAccessFile raf) throws Exception {

        if (raf.getFilePointer() >= raf.length()) {
            return null;
        }

        int id = raf.readInt();
        int userID = raf.readInt();
        int filmID = raf.readInt();
        byte rating = raf.readByte();

        short size = raf.readShort();
        byte[] noteBytes = new byte[size];
        raf.readFully(noteBytes);

        String note = new String(noteBytes, StandardCharsets.UTF_8);

        return new Review(id, userID, filmID, rating, note);
    }

    //  LER ARQUIVO FINAL ORDENADO
    private ArrayList<Review> lerArquivoOrdenado(boolean decrescente) throws Exception {

        ArrayList<Review> lista = new ArrayList<>();

        File arquivo = new File(SORTED_FILE);

        if (!arquivo.exists()) {
            return lista;
        }

        RandomAccessFile raf = new RandomAccessFile(arquivo, "r");

        Review r;

        while ((r = lerProximo(raf)) != null) {
            lista.add(r);
        }

        raf.close();

        if (decrescente) {
            Collections.reverse(lista);
        }

        return lista;
    }

    // =========================
    //  ESCRITA
    // =========================
    private void escrever(RandomAccessFile raf, Review r) throws Exception {

        raf.writeInt(r.getID());
        raf.writeInt(r.getUserID());
        raf.writeInt(r.getFilmID());
        raf.writeByte(r.getRating());

        byte[] noteBytes = r.getNote().getBytes(StandardCharsets.UTF_8);
        raf.writeShort(noteBytes.length);
        raf.write(noteBytes);
    }

    // =========================
    //  UTILITÁRIOS
    // =========================
    private void garantirPastaData() {
        File pasta = new File("data");

        if (!pasta.exists()) {
            pasta.mkdirs();
        }
    }

    private void copiarArquivo(File origem, File destino) throws Exception {
        FileInputStream in = new FileInputStream(origem);
        FileOutputStream out = new FileOutputStream(destino);

        byte[] buffer = new byte[1024];
        int bytesLidos;

        while ((bytesLidos = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesLidos);
        }

        in.close();
        out.close();
    }

    private void limparArquivosTemporarios() {
        File pasta = new File("data");

        File[] arquivos = pasta.listFiles();

        if (arquivos == null) {
            return;
        }

        for (File arquivo : arquivos) {
            String nome = arquivo.getName();

            if (nome.startsWith("run") && nome.endsWith(".bin")) {
                arquivo.delete();
            }

            if (nome.startsWith("temp") && nome.endsWith(".bin")) {
                arquivo.delete();
            }
        }
    }
}