package CRUD.sort;

import CRUD.model.Review;
import CRUD.util.FileManeger;

import java.io.*;
import java.util.*;

public class ExternalSortReview {

    private final String FILE = "data/review.bin";
    private final int RUN_SIZE = 4; // tamanho do bloco (igual prova)

    // 🔥 MÉTODO PRINCIPAL
    public void ordenar() throws Exception {
        int totalRuns = gerarRuns();
        intercalarRuns(totalRuns);
        System.out.println("Ordenação completa finalizada.");
    }

    // =========================
    // 🔥 FASE 1 - GERAR RUNS
    // =========================
    private int gerarRuns() throws Exception {

        RandomAccessFile raf = FileManeger.open(FILE);
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
            String note = new String(noteBytes, "UTF-8");

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

        buffer.sort(Comparator.comparingInt(Review::getRating));

        RandomAccessFile raf = new RandomAccessFile("data/run" + index + ".bin", "rw");

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

        // 🔥 arquivo final
        String finalFile = fila.poll();

        File destino = new File("data/review_ordenado.bin");
        File origem = new File(finalFile);

        if (destino.exists()) destino.delete();
        origem.renameTo(destino);

        System.out.println("Arquivo final gerado: review_ordenado.bin");
    }

    // 🔥 MERGE DE 2 ARQUIVOS
    private void merge2(String file1, String file2, String output) throws Exception {

        RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
        RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
        RandomAccessFile out = new RandomAccessFile(output, "rw");

        Review r1 = lerProximo(raf1);
        Review r2 = lerProximo(raf2);

        while (r1 != null && r2 != null) {

            if (r1.getRating() <= r2.getRating()) {
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

    // =========================
    // 🔥 LEITURA
    // =========================
    private Review lerProximo(RandomAccessFile raf) throws Exception {

        if (raf.getFilePointer() >= raf.length())
            return null;

        int id = raf.readInt();
        int userID = raf.readInt();
        int filmID = raf.readInt();
        byte rating = raf.readByte();

        short size = raf.readShort();
        byte[] noteBytes = new byte[size];
        raf.readFully(noteBytes);

        String note = new String(noteBytes, "UTF-8");

        return new Review(id, userID, filmID, rating, note);
    }

    // =========================
    // 🔥 ESCRITA
    // =========================
    private void escrever(RandomAccessFile raf, Review r) throws Exception {

        raf.writeInt(r.getID());
        raf.writeInt(r.getUserID());
        raf.writeInt(r.getFilmID());
        raf.writeByte(r.getRating());

        byte[] noteBytes = r.getNote().getBytes("UTF-8");
        raf.writeShort(noteBytes.length);
        raf.write(noteBytes);
    }
}