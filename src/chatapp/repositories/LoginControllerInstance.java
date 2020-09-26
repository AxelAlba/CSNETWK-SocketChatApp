package chatapp.repositories;

import chatapp.controllers.LoginController;

public class LoginControllerInstance {
    private static LoginController loginController = null;

    public static void setLoginController(LoginController c) {
        loginController = c;
    }

    public static LoginController getLoginController() {
            return loginController;
        }
}
