package com.filmes.crud.controller;

import com.filmes.crud.model.User;
import com.filmes.crud.dao.UserDAO;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserDAO dao;

    public AuthController() throws Exception {
        this.dao = new UserDAO();
    }

    @PostMapping("/login")
    public Object login(@RequestBody User loginData) {
        try {
            if (loginData == null) {
                return new ErrorResponse("Dados de login inválidos.");
            }

            if (loginData.getEmail() == null || loginData.getEmail().trim().isEmpty()) {
                return new ErrorResponse("E-mail é obrigatório.");
            }

            if (loginData.getPassword() == null || loginData.getPassword().trim().isEmpty()) {
                return new ErrorResponse("Senha é obrigatória.");
            }

            String email = loginData.getEmail().trim().toLowerCase();
            String senha = loginData.getPassword().trim();

            User user = dao.readByEmail(email);

            if (user == null || !user.getPassword().equals(senha)) {
                return new ErrorResponse("Email ou senha incorretos.");
            }

            return new LoginResponse(user);

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse("Erro interno no servidor.");
        }
    }

    @PostMapping("/register")
    public Object register(@RequestBody User newUser) {
        try {
            if (newUser == null) {
                return new ErrorResponse("Dados de cadastro inválidos.");
            }

            if (newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
                return new ErrorResponse("Username é obrigatório.");
            }

            if (newUser.getEmail() == null || newUser.getEmail().trim().isEmpty()) {
                return new ErrorResponse("E-mail é obrigatório.");
            }

            if (newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
                return new ErrorResponse("Senha é obrigatória.");
            }

            newUser.setUsername(newUser.getUsername().trim());
            newUser.setEmail(newUser.getEmail().trim().toLowerCase());
            newUser.setPassword(newUser.getPassword().trim());

            User existenteEmail = dao.readByEmail(newUser.getEmail());
            if (existenteEmail != null) {
                return new ErrorResponse("Já existe um usuário com esse e-mail.");
            }

            User existenteUsername = dao.readByUsername(newUser.getUsername());
            if (existenteUsername != null) {
                return new ErrorResponse("Já existe um usuário com esse username.");
            }

            if (!newUser.isAdministrator()) {
                newUser.setAdministrator(false);
            }

            int id = dao.create(newUser);
            User criado = dao.read(id);

            if (criado == null) {
                return new ErrorResponse("Usuário cadastrado, mas não pôde ser carregado.");
            }

            return new LoginResponse(criado);

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(
                e.getMessage() == null || e.getMessage().isBlank()
                    ? "Erro ao cadastrar usuário."
                    : e.getMessage()
            );
        }
    }

    static class LoginResponse {
        public int id;
        public String username;
        public String email;
        public boolean administrator;

        public LoginResponse(User user) {
            this.id = user.getID();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.administrator = user.isAdministrator();
        }
    }

    static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}