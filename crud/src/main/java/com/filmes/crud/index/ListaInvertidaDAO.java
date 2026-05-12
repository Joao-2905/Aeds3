package com.filmes.crud.index;

import java.io.*;
import java.util.*;

public class ListaInvertidaDAO {

    private final String filePath;

    public ListaInvertidaDAO(String nomeIndice) throws IOException {
        this.filePath = "data/" + nomeIndice + "_lista_invertida.bin";
        initFile();
    }

    private void initFile() throws IOException {
        File pasta = new File("data");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        File arquivo = new File(filePath);

        if (!arquivo.exists()) {
            RandomAccessFile raf = new RandomAccessFile(arquivo, "rw");
            raf.writeInt(0); // quantidade de chaves
            raf.close();
        }
    }

    public void inserir(int chave, int idRegistro) throws IOException {
        Map<Integer, List<Integer>> indice = carregarIndice();

        List<Integer> lista = indice.get(chave);

        if (lista == null) {
            lista = new ArrayList<>();
            indice.put(chave, lista);
        }

        if (!lista.contains(idRegistro)) {
            lista.add(idRegistro);
        }

        salvarIndice(indice);
    }

    public List<Integer> buscar(int chave) throws IOException {
        Map<Integer, List<Integer>> indice = carregarIndice();

        List<Integer> lista = indice.get(chave);

        if (lista == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(lista);
    }

    public boolean remover(int chave, int idRegistro) throws IOException {
        Map<Integer, List<Integer>> indice = carregarIndice();

        List<Integer> lista = indice.get(chave);

        if (lista == null) {
            return false;
        }

        boolean removeu = lista.remove(Integer.valueOf(idRegistro));

        if (lista.isEmpty()) {
            indice.remove(chave);
        }

        salvarIndice(indice);

        return removeu;
    }

    public void atualizar(int chaveAntiga, int chaveNova, int idRegistro) throws IOException {
        remover(chaveAntiga, idRegistro);
        inserir(chaveNova, idRegistro);
    }

    public void limpar() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        raf.setLength(0);
        raf.writeInt(0);
        raf.close();
    }

    public Map<Integer, List<Integer>> listarTudo() throws IOException {
        return carregarIndice();
    }

    private Map<Integer, List<Integer>> carregarIndice() throws IOException {
        Map<Integer, List<Integer>> indice = new HashMap<>();

        RandomAccessFile raf = new RandomAccessFile(filePath, "r");

        if (raf.length() == 0) {
            raf.close();
            return indice;
        }

        raf.seek(0);

        int quantidadeChaves = raf.readInt();

        for (int i = 0; i < quantidadeChaves; i++) {
            int chave = raf.readInt();
            int quantidadeIds = raf.readInt();

            List<Integer> lista = new ArrayList<>();

            for (int j = 0; j < quantidadeIds; j++) {
                lista.add(raf.readInt());
            }

            indice.put(chave, lista);
        }

        raf.close();
        return indice;
    }

    private void salvarIndice(Map<Integer, List<Integer>> indice) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

        raf.setLength(0);
        raf.seek(0);

        raf.writeInt(indice.size());

        for (Map.Entry<Integer, List<Integer>> entrada : indice.entrySet()) {
            int chave = entrada.getKey();
            List<Integer> lista = entrada.getValue();

            raf.writeInt(chave);
            raf.writeInt(lista.size());

            for (int id : lista) {
                raf.writeInt(id);
            }
        }

        raf.close();
    }
}