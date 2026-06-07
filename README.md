# Aeds3 🎬

Sistema para avaliação de filmes.

## Vídeo de demonstração

Clique no link abaixo para assistir à demonstração do sistema:

[▶ Assistir vídeo](https://drive.google.com/file/d/1s7q_mh0dCd0qCZmTXur5v805a-sCRogE/view?usp=sharing)

## ▶️ Como executar

Baixe o arquivo `Tp_fase3.zip`.

Extraia o arquivo.

Após extrair, entre na pasta `crud`.

Abra o **Prompt de Comando (cmd)** dentro da pasta `crud`.

💡 Dica:
Clique na barra de endereço da pasta, digite `cmd` e pressione Enter.

Execute o comando:

`.\mvnw.cmd clean package`

Em seguida:

`cd target`

E por fim:

`java -jar crud-0.0.1-SNAPSHOT.jar`

Abra o navegador:

`http://localhost:8080`


## Requisitos

- Java 21 ou superior

---

## ⚠️ Observação importante

O sistema atualmente possui duas pastas `data`.

A pasta `data` da raiz está sendo utilizada apenas para testes.

Lembre-se de executar o comando `cd target`, conforme instruído anteriormente, para garantir que os arquivos sejam salvos na pasta correta.

Tudo o que for feito no sistema será armazenado na pasta `data` localizada dentro da pasta `target`.

---

## 👤 Tipos de usuários

O sistema possui dois tipos de usuários:

### 👥 Usuário

- Acesso à própria conta
- Criação de avaliações
- Visualização de avaliações
- Possibilidade de ordenar por meio da ordenação externa

### 🛠️ Administrador

Possui todas as permissões de um usuário (menos uso da ordenação externa).

Acesso completo aos CRUDs:

- Usuários
- Gêneros
- Filmes
- Avaliações

Possibilidade de ordenar por meio da Árvore B+.
Possibilidade de fazer backups compactados.

---

## 🔐 Controle de administradores

Apenas um administrador pode promover outro usuário a administrador.

Para evitar problemas, o sistema já vem com um administrador padrão.

👉 As credenciais desse administrador são exibidas automaticamente na primeira execução do sistema.

email: admin@admin.com

senha: 123
