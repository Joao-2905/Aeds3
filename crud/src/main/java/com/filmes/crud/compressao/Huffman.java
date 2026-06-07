package com.filmes.crud.compressao;

import java.io.*;
import java.util.*;

public class Huffman {

    private static class Node implements Comparable<Node> {
        byte valor;
        int freq;
        Node esq, dir;

        Node(byte valor, int freq) {
            this.valor = valor;
            this.freq = freq;
        }

        Node(Node esq, Node dir) {
            this.esq = esq;
            this.dir = dir;
            this.freq = esq.freq + dir.freq;
        }

        boolean folha() {
            return esq == null && dir == null;
        }

        @Override
        public int compareTo(Node outro) {
            return Integer.compare(this.freq, outro.freq);
        }
    }

    public static void compactar(String entrada, String saida) throws IOException {
        byte[] dados = lerArquivo(entrada);

        if (dados.length == 0) {
            throw new IOException("Arquivo vazio.");
        }

        int[] freq = new int[256];

        for (byte b : dados) {
            freq[b & 0xFF]++;
        }

        PriorityQueue<Node> fila = new PriorityQueue<>();

        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) {
                fila.add(new Node((byte) i, freq[i]));
            }
        }

        while (fila.size() > 1) {
            Node a = fila.poll();
            Node b = fila.poll();
            fila.add(new Node(a, b));
        }

        Node raiz = fila.poll();

        Map<Byte, String> codigos = new HashMap<>();
        gerarCodigos(raiz, "", codigos);

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(saida))) {

            dos.writeInt(dados.length);

            for (int f : freq) {
                dos.writeInt(f);
            }

            int byteAtual = 0;
            int quantidadeBits = 0;

            for (byte b : dados) {
                String codigo = codigos.get(b);

                for (int i = 0; i < codigo.length(); i++) {
                    byteAtual <<= 1;

                    if (codigo.charAt(i) == '1') {
                        byteAtual |= 1;
                    }

                    quantidadeBits++;

                    if (quantidadeBits == 8) {
                        dos.writeByte(byteAtual);
                        byteAtual = 0;
                        quantidadeBits = 0;
                    }
                }
            }

            if (quantidadeBits > 0) {
                byteAtual <<= (8 - quantidadeBits);
                dos.writeByte(byteAtual);
            }
        }

        System.out.println("Arquivo Huffman compactado: " + saida);
    }

    public static void descompactar(String entrada, String saida) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(entrada))) {

            int tamanhoOriginal = dis.readInt();

            int[] freq = new int[256];

            for (int i = 0; i < 256; i++) {
                freq[i] = dis.readInt();
            }

            PriorityQueue<Node> fila = new PriorityQueue<>();

            for (int i = 0; i < 256; i++) {
                if (freq[i] > 0) {
                    fila.add(new Node((byte) i, freq[i]));
                }
            }

            if (fila.isEmpty()) {
                throw new IOException("Arquivo Huffman inválido.");
            }

            while (fila.size() > 1) {
                Node a = fila.poll();
                Node b = fila.poll();
                fila.add(new Node(a, b));
            }

            Node raiz = fila.poll();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            if (raiz.folha()) {
                for (int i = 0; i < tamanhoOriginal; i++) {
                    baos.write(raiz.valor);
                }
            } else {
                Node atual = raiz;

                while (baos.size() < tamanhoOriginal) {
                    int byteLido;

                    try {
                        byteLido = dis.readUnsignedByte();
                    } catch (EOFException e) {
                        throw new IOException("Fim inesperado do arquivo Huffman. Restaurados " + baos.size() + " de " + tamanhoOriginal + " bytes.");
                    }

                    for (int i = 7; i >= 0 && baos.size() < tamanhoOriginal; i--) {
                        int bit = (byteLido >> i) & 1;

                        atual = bit == 0 ? atual.esq : atual.dir;

                        if (atual.folha()) {
                            baos.write(atual.valor);
                            atual = raiz;
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(saida)) {
                fos.write(baos.toByteArray());
            }
        }

        System.out.println("Arquivo Huffman descompactado: " + saida);
    }

    private static void gerarCodigos(Node no, String codigo, Map<Byte, String> codigos) {
        if (no.folha()) {
            codigos.put(no.valor, codigo.isEmpty() ? "0" : codigo);
            return;
        }

        gerarCodigos(no.esq, codigo + "0", codigos);
        gerarCodigos(no.dir, codigo + "1", codigos);
    }

    private static byte[] lerArquivo(String caminho) throws IOException {
        return java.nio.file.Files.readAllBytes(new File(caminho).toPath());
    }
}