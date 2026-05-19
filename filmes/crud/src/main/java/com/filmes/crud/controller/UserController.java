package com.filmes.crud.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.filmes.crud.model.User;
import com.filmes.crud.service.UserService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController() throws Exception {
        this.userService = new UserService();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listar() {
        try {
            List<UserResponse> resposta = new ArrayList<>();
            List<User> usuarios = userService.listUsersToList();

            for (User user : usuarios) {
                if (user != null) {
                    resposta.add(new UserResponse(user));
                }
            }

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            User user = userService.readUser(id);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Usuário não encontrado."));
            }

            return ResponseEntity.ok(new UserResponse(user));

        } catch (Exception e) {
            e.printStackTrace();

            String mensagem = mensagemOuPadrao(
                e,
                "Erro interno ao buscar usuário."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagem));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody User user) {
        try {

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Dados do usuário inválidos."));
            }

            prepararUserRequest(user);

            int id = userService.createUser(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isAdministrator()
            );

            if (id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Não foi possível cadastrar o usuário."));
            }

            User criado = userService.readUser(id);

            if (criado == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Usuário foi criado, mas não pôde ser lido de volta."));
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new UserResponse(criado));

        } catch (Exception e) {
            e.printStackTrace();

            String mensagem = mensagemOuPadrao(
                e,
                "Erro interno ao cadastrar usuário."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagem));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody User user) {
        try {

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Dados do usuário inválidos."));
            }

            prepararUserRequest(user);

            boolean ok = userService.updateUserComplete(
                id,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isAdministrator()
            );

            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Não foi possível atualizar o usuário."));
            }

            User atualizado = userService.readUser(id);

            if (atualizado == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Usuário atualizado, mas não pôde ser lido."));
            }

            return ResponseEntity.ok(new UserResponse(atualizado));

        } catch (Exception e) {
            e.printStackTrace();

            String mensagem = mensagemOuPadrao(
                e,
                "Erro interno ao atualizar usuário."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagem));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable int id) {
        try {

            boolean ok = userService.deleteUser(id);

            if (ok) {
                return ResponseEntity.ok(
                    new SuccessResponse("Usuário excluído com sucesso.")
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Não foi possível excluir o usuário."));

        } catch (Exception e) {
            e.printStackTrace();

            String mensagem = mensagemOuPadrao(
                e,
                "Erro interno ao excluir usuário."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagem));
        }
    }

    @GetMapping("/perfil/{id}")
    public ResponseEntity<?> buscarPerfil(@PathVariable int id) {
        try {

            User user = userService.readUser(id);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Usuário não encontrado."));
            }

            return ResponseEntity.ok(new ProfileResponse(user));

        } catch (Exception e) {
            e.printStackTrace();

            String mensagem = mensagemOuPadrao(
                e,
                "Erro ao carregar perfil."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagem));
        }
    }

    @PutMapping("/perfil/{id}")
    public ResponseEntity<?> atualizarPerfil(@PathVariable int id, @RequestBody User user) {
        try {

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Dados do perfil inválidos."));
            }

            prepararUserRequest(user);

            boolean ok = userService.updateOwnProfile(
                id,
                user.getEmail(),
                user.getPassword()
            );

            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Não foi possível atualizar o perfil."));
            }

            User atualizado = userService.readUser(id);

            if (atualizado == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Perfil atualizado, mas não pôde ser lido."));
            }

            return ResponseEntity.ok(new ProfileResponse(atualizado));

        } catch (Exception e) {
            e.printStackTrace();

            String mensagem = mensagemOuPadrao(
                e,
                "Erro ao atualizar perfil."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagem));
        }
    }

    @DeleteMapping("/perfil/{id}")
    public ResponseEntity<?> excluirPerfil(@PathVariable int id) {
        try {

            boolean ok = userService.deleteOwnProfile(id);

            if (ok) {
                return ResponseEntity.ok(
                    new SuccessResponse("Conta excluída com sucesso.")
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Não foi possível excluir a conta."));

        } catch (Exception e) {
            e.printStackTrace();

            String mensagem = mensagemOuPadrao(
                e,
                "Erro ao excluir conta."
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagem));
        }
    }

    private void prepararUserRequest(User user) {

        if (user == null) {
            return;
        }

        if (user.getUsername() != null) {
            user.setUsername(
                user.getUsername().trim()
            );
        }

        if (user.getEmail() != null) {
            user.setEmail(
                user.getEmail().trim().toLowerCase()
            );
        }

        if (user.getPassword() != null) {
            user.setPassword(
                user.getPassword().trim()
            );
        }
    }

    private String mensagemOuPadrao(Exception e, String padrao) {

        String mensagem = e.getMessage();

        if (mensagem == null || mensagem.isBlank()) {
            return padrao;
        }

        return mensagem;
    }

    static class UserResponse {

        public int id;
        public String username;
        public String email;
        public boolean administrator;

        public UserResponse(User user) {
            this.id = user.getID();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.administrator = user.isAdministrator();
        }
    }

    static class ProfileResponse {

        public int id;
        public String username;
        public String email;
        public String password;
        public boolean administrator;

        public ProfileResponse(User user) {
            this.id = user.getID();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.password = user.getPassword();
            this.administrator = user.isAdministrator();
        }
    }

    static class ErrorResponse {

        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    static class SuccessResponse {

        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }
}