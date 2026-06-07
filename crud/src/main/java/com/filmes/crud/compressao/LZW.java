package com.filmes.crud.compressao;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class LZW {

    public static void compactar(String entrada, String saida) throws IOException {
        byte[] dados = Files.readAllBytes(new File(entrada).toPath());

        Map<String, Integer> dicionario = new HashMap<>();

        for (int i = 0; i < 256; i++) {
            dicionario.put("" + (char) i, i);
        }

        int codigoAtual = 256;

        String w = "";

        ArrayList<Integer> codigos = new ArrayList<>();

        for (byte b : dados) {
            char c = (char) (b & 0xFF);
            String wc = w + c;

            if (dicionario.containsKey(wc)) {
                w = wc;
            } else {
                codigos.add(dicionario.get(w));
                dicionario.put(wc, codigoAtual++);
                w = "" + c;
            }
        }

        if (!w.equals("")) {
            codigos.add(dicionario.get(w));
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(saida))) {
            dos.writeInt(dados.length);
            dos.writeInt(codigos.size());

            for (int codigo : codigos) {
                dos.writeInt(codigo);
            }
        }
    }

    public static void descompactar(String entrada, String saida) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(entrada))) {

            dis.readInt();

            int quantidadeCodigos = dis.readInt();

            if (quantidadeCodigos == 0) {
                Files.write(new File(saida).toPath(), new byte[0]);
                return;
            }

            ArrayList<Integer> codigos = new ArrayList<>();

            for (int i = 0; i < quantidadeCodigos; i++) {
                codigos.add(dis.readInt());
            }

            Map<Integer, String> dicionario = new HashMap<>();

            for (int i = 0; i < 256; i++) {
                dicionario.put(i, "" + (char) i);
            }

            int codigoAtual = 256;

            String w = dicionario.get(codigos.get(0));

            ByteArrayOutputStream resultado = new ByteArrayOutputStream();

            escreverStringComoBytes(resultado, w);

            for (int i = 1; i < codigos.size(); i++) {
                int k = codigos.get(i);

                String entradaDic;

                if (dicionario.containsKey(k)) {
                    entradaDic = dicionario.get(k);
                } else if (k == codigoAtual) {
                    entradaDic = w + w.charAt(0);
                } else {
                    throw new IOException("Código LZW inválido: " + k);
                }

                escreverStringComoBytes(resultado, entradaDic);

                dicionario.put(codigoAtual++, w + entradaDic.charAt(0));

                w = entradaDic;
            }

            Files.write(new File(saida).toPath(), resultado.toByteArray());
        }
    }

    private static void escreverStringComoBytes(ByteArrayOutputStream baos, String texto) {
        for (int i = 0; i < texto.length(); i++) {
            baos.write((byte) texto.charAt(i));
        }
    }
}