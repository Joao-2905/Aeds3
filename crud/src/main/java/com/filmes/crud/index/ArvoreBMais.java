package com.filmes.crud.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArvoreBMais {

    private final int ordem;
    private final String arquivoIndice;
    private NoBMais raiz;

    public ArvoreBMais(int ordem, String nomeIndice) throws IOException {
        if (ordem < 3) {
            ordem = 3;
        }

        this.ordem = ordem;
        this.arquivoIndice = "data/" + nomeIndice + "_bmais.bin";
        this.raiz = new NoBMais(true);

        carregarDoArquivo();
    }

    public void inserir(int chave, int idRegistro) throws IOException {
        if (buscar(chave).contains(idRegistro)) {
            return;
        }

        if (raiz.chaves.size() == ordem - 1) {
            NoBMais novaRaiz = new NoBMais(false);
            novaRaiz.filhos.add(raiz);
            dividirFilho(novaRaiz, 0);
            raiz = novaRaiz;
        }

        inserirNaoCheio(raiz, chave, idRegistro);
        salvarEmArquivo();
    }

    public List<Integer> buscar(int chave) {
        return buscarNaArvore(raiz, chave);
    }

    public List<Integer> buscarIntervalo(int inicio, int fim) {
        List<Integer> resultado = new ArrayList<>();

        if (inicio > fim) {
            int temp = inicio;
            inicio = fim;
            fim = temp;
        }

        NoBMais folha = encontrarPrimeiraFolha(raiz);

        while (folha != null) {
            for (int i = 0; i < folha.chaves.size(); i++) {
                int chave = folha.chaves.get(i);

                if (chave >= inicio && chave <= fim) {
                    resultado.addAll(folha.valores.get(i));
                }

                if (chave > fim) {
                    return resultado;
                }
            }

            folha = folha.proximo;
        }

        return resultado;
    }

    public List<Integer> listarCrescente() {
        List<Integer> resultado = new ArrayList<>();

        NoBMais folha = encontrarPrimeiraFolha(raiz);

        while (folha != null) {
            for (List<Integer> ids : folha.valores) {
                resultado.addAll(ids);
            }

            folha = folha.proximo;
        }

        return resultado;
    }

    public List<Integer> listarDecrescente() {
        List<RegistroBMais> registros = listarRegistrosOrdenados();
        List<Integer> resultado = new ArrayList<>();

        for (int i = registros.size() - 1; i >= 0; i--) {
            resultado.add(registros.get(i).getIdRegistro());
        }

        return resultado;
    }

    public List<RegistroBMais> listarRegistrosOrdenados() {
        List<RegistroBMais> registros = new ArrayList<>();

        NoBMais folha = encontrarPrimeiraFolha(raiz);

        while (folha != null) {
            for (int i = 0; i < folha.chaves.size(); i++) {
                int chave = folha.chaves.get(i);
                List<Integer> ids = folha.valores.get(i);

                for (Integer id : ids) {
                    registros.add(new RegistroBMais(chave, id));
                }
            }

            folha = folha.proximo;
        }

        return registros;
    }

    public void limpar() throws IOException {
        raiz = new NoBMais(true);

        File file = new File(arquivoIndice);
        if (file.exists()) {
            file.delete();
        }

        salvarEmArquivo();
    }

    public void exibir() {
        System.out.println("\n--- ÁRVORE B+ ---");
        exibirNo(raiz, 0);
    }

    private void inserirNaoCheio(NoBMais no, int chave, int idRegistro) {
        if (no.folha) {
            inserirNaFolha(no, chave, idRegistro);
            return;
        }

        int i = no.chaves.size() - 1;

        while (i >= 0 && chave < no.chaves.get(i)) {
            i--;
        }

        i++;

        NoBMais filho = no.filhos.get(i);

        if (filho.chaves.size() == ordem - 1) {
            dividirFilho(no, i);

            if (chave >= no.chaves.get(i)) {
                i++;
            }
        }

        inserirNaoCheio(no.filhos.get(i), chave, idRegistro);
    }

    private void inserirNaFolha(NoBMais folha, int chave, int idRegistro) {
        int pos = 0;

        while (pos < folha.chaves.size() && folha.chaves.get(pos) < chave) {
            pos++;
        }

        if (pos < folha.chaves.size() && folha.chaves.get(pos) == chave) {
            if (!folha.valores.get(pos).contains(idRegistro)) {
                folha.valores.get(pos).add(idRegistro);
            }
            return;
        }

        folha.chaves.add(pos, chave);

        List<Integer> ids = new ArrayList<>();
        ids.add(idRegistro);

        folha.valores.add(pos, ids);
    }

    private void dividirFilho(NoBMais pai, int indiceFilho) {
        NoBMais filho = pai.filhos.get(indiceFilho);
        NoBMais novo = new NoBMais(filho.folha);

        int meio = ordem / 2;

        if (filho.folha) {
            for (int i = meio; i < filho.chaves.size(); i++) {
                novo.chaves.add(filho.chaves.get(i));
                novo.valores.add(filho.valores.get(i));
            }

            while (filho.chaves.size() > meio) {
                filho.chaves.remove(filho.chaves.size() - 1);
                filho.valores.remove(filho.valores.size() - 1);
            }

            novo.proximo = filho.proximo;
            filho.proximo = novo;

            pai.chaves.add(indiceFilho, novo.chaves.get(0));
            pai.filhos.add(indiceFilho + 1, novo);

        } else {
            int chavePromovida = filho.chaves.get(meio);

            for (int i = meio + 1; i < filho.chaves.size(); i++) {
                novo.chaves.add(filho.chaves.get(i));
            }

            for (int i = meio + 1; i < filho.filhos.size(); i++) {
                novo.filhos.add(filho.filhos.get(i));
            }

            while (filho.chaves.size() > meio) {
                filho.chaves.remove(filho.chaves.size() - 1);
            }

            while (filho.filhos.size() > meio + 1) {
                filho.filhos.remove(filho.filhos.size() - 1);
            }

            pai.chaves.add(indiceFilho, chavePromovida);
            pai.filhos.add(indiceFilho + 1, novo);
        }
    }

    private List<Integer> buscarNaArvore(NoBMais no, int chave) {
        int i = 0;

        while (i < no.chaves.size() && chave >= no.chaves.get(i)) {
            if (no.folha && chave == no.chaves.get(i)) {
                return new ArrayList<>(no.valores.get(i));
            }

            i++;
        }

        if (no.folha) {
            return new ArrayList<>();
        }

        return buscarNaArvore(no.filhos.get(i), chave);
    }

    private NoBMais encontrarPrimeiraFolha(NoBMais no) {
        if (no == null) {
            return null;
        }

        while (!no.folha) {
            no = no.filhos.get(0);
        }

        return no;
    }

    private void salvarEmArquivo() throws IOException {
        File pasta = new File("data");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        List<RegistroBMais> registros = listarRegistrosOrdenados();

        RandomAccessFile raf = new RandomAccessFile(arquivoIndice, "rw");
        raf.setLength(0);

        raf.writeInt(registros.size());

        for (RegistroBMais r : registros) {
            raf.writeInt(r.getChave());
            raf.writeInt(r.getIdRegistro());
        }

        raf.close();
    }

    private void carregarDoArquivo() throws IOException {
        File file = new File(arquivoIndice);

        if (!file.exists() || file.length() == 0) {
            return;
        }

        RandomAccessFile raf = new RandomAccessFile(file, "r");

        if (raf.length() < 4) {
            raf.close();
            return;
        }

        int quantidade = raf.readInt();

        List<RegistroBMais> registros = new ArrayList<>();

        for (int i = 0; i < quantidade; i++) {
            int chave = raf.readInt();
            int idRegistro = raf.readInt();

            registros.add(new RegistroBMais(chave, idRegistro));
        }

        raf.close();

        Collections.sort(registros, (a, b) -> {
            if (a.getChave() != b.getChave()) {
                return Integer.compare(a.getChave(), b.getChave());
            }

            return Integer.compare(a.getIdRegistro(), b.getIdRegistro());
        });

        raiz = new NoBMais(true);

        for (RegistroBMais r : registros) {
            inserirNaoCheioComSplit(r.getChave(), r.getIdRegistro());
        }
    }

    private void inserirNaoCheioComSplit(int chave, int idRegistro) {
        if (raiz.chaves.size() == ordem - 1) {
            NoBMais novaRaiz = new NoBMais(false);
            novaRaiz.filhos.add(raiz);
            dividirFilho(novaRaiz, 0);
            raiz = novaRaiz;
        }

        inserirNaoCheio(raiz, chave, idRegistro);
    }

    private void exibirNo(NoBMais no, int nivel) {
        String espacos = "  ".repeat(nivel);

        if (no.folha) {
            System.out.println(espacos + "Folha: " + no.chaves);
        } else {
            System.out.println(espacos + "Interno: " + no.chaves);

            for (NoBMais filho : no.filhos) {
                exibirNo(filho, nivel + 1);
            }
        }
    }
}