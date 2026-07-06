package com.moodflix.controller;

import com.moodflix.Main;
import com.moodflix.view.LandingPage;
import com.moodflix.view.LoginPage;
import com.moodflix.view.SignUpPage;
import javafx.scene.Scene;

public class LandingPageController {
    private final LandingPage view;

    public LandingPageController(LandingPage view) {
        this.view = view;
        setupActions();
    }

    private void setupActions() {
        view.getLoginButton().setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            LoginPageController loginController = new LoginPageController(loginPage);
            Main.setScene(new Scene(loginPage.getView()));
        });

        view.getSignupButton().setOnAction(event -> {
            SignUpPage signUpPage = new SignUpPage();
            SignUpPageController signUpController = new SignUpPageController(signUpPage);
            Main.setScene(new Scene(signUpPage.getView()));
        });
    }
}

