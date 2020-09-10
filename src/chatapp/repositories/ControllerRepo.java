package chatapp.repositories;

import chatapp.controllers.ChatController;

public class ControllerRepo {
    private static ChatController controller1, controller2;

    public static ChatController getController1() {
        return controller1;
    }

    public static ChatController getController2() {
        return controller2;
    }

    public static void setController1(ChatController c) {
        if (controller1 == null)
            controller1 = c;
    }

    public static void setController2(ChatController c) {
        if (controller2 == null)
            controller2 = c;
    }
}
