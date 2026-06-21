package com.filmes.crud.controller;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import com.filmes.crud.compressao.BackupManager;

@RestController
@RequestMapping("/backup")
@CrossOrigin("*")
public class BackupController {

    @GetMapping("/criar")
    public String criarBackup() {
        try {
            BackupManager.criarBackup("data", "backup.dat");
            return "Backup criado com sucesso: backup.dat";
        }catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("/restaurar")
    public String restaurarBackup() {
        try {
            BackupManager.restaurarBackup("backup.dat", "data_restaurada");
            return "Backup restaurado com sucesso.";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @GetMapping("/huffman/criar")
    public Map<String, Object> criarBackupHuffman() {
        try {
            BackupManager.criarBackupHuffman("data");
            return gerarRelatorioCompressao("Huffman","backup.dat","backup_huffman.dat");
        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> erro = new LinkedHashMap<>();
            erro.put("erro", true);
            erro.put("mensagem", e.getMessage());
            return erro;
        }
    }

    @GetMapping("/huffman/restaurar")
    public String restaurarBackupHuffman() {
        try {
            BackupManager.restaurarBackupHuffman("data_restaurada_huffman");
            return "Backup Huffman restaurado com sucesso em data_restaurada_huffman.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao restaurar Huffman: " + e.getMessage();
        }
    }

    @GetMapping("/lzw/criar")
    public Map<String, Object> criarBackupLZW() {
        try {
            BackupManager.criarBackupLZW("data");
            return gerarRelatorioCompressao("LZW","backup.dat", "backup_lzw.dat");
        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> erro = new LinkedHashMap<>();
            erro.put("erro", true);
            erro.put("mensagem", e.getMessage());
            return erro;
        }
    }

    @GetMapping("/lzw/restaurar")
    public String restaurarBackupLZW() {
        try {
            BackupManager.restaurarBackupLZW("data_restaurada_lzw");
            return "Backup LZW restaurado com sucesso.";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private Map<String, Object> gerarRelatorioCompressao(String algoritmo, String arquivoOriginal, String arquivoComprimido) {
        File original = new File(arquivoOriginal);
        File comprimido = new File(arquivoComprimido);

        long tamanhoOriginal = original.length();
        long tamanhoComprimido = comprimido.length();

        double taxa = 0;

        if (tamanhoOriginal > 0) {
            taxa = ((double)(tamanhoOriginal - tamanhoComprimido)/ tamanhoOriginal) * 100.0;
        }

        String interpretacao;

        if (taxa > 0) {
            interpretacao = "O arquivo comprimido ficou menor que o original. Houve ganho de compressão.";
        } else if (taxa == 0) {
            interpretacao = "O arquivo comprimido ficou com o mesmo tamanho do original.";
        } else {
            interpretacao = "O arquivo comprimido ficou maior que o original.";
        }

        Map<String, Object> resposta = new LinkedHashMap<>();

        resposta.put("algoritmo", algoritmo);
        resposta.put("arquivoOriginal", arquivoOriginal);
        resposta.put("arquivoComprimido", arquivoComprimido);
        resposta.put("tamanhoOriginalBytes", tamanhoOriginal);
        resposta.put("tamanhoComprimidoBytes", tamanhoComprimido);
        resposta.put("taxaCompressao", String.format("%.2f%%", taxa));
        resposta.put("calculo","((" + tamanhoOriginal + " - " + tamanhoComprimido + ") / "+ tamanhoOriginal + ") * 100 = " + String.format("%.2f%%", taxa));
        resposta.put("interpretacao", interpretacao);

        return resposta;
    }
}