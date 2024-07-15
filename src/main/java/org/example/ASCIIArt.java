package org.example;

public class ASCIIArt {
    public void printBackendInformation() {
        String title = "Storage Backend";
        String host = "Host: "+System.getProperty("HOST");
        String port = "Port: " + System.getProperty("PORT");
        String storageBackend = "Storage Backend: "+System.getProperty("STORAGE_BACKEND");
        System.out.println("\n\n");
        printASCIIArt();
        int boxWidth = Math.max(Math.max(title.length(), host.length()), Math.max(port.length(), storageBackend.length())) + 4;

        printStars(boxWidth);
        printCenteredText(title, boxWidth);
        printCenteredText(host, boxWidth);
        printCenteredText(port, boxWidth);
        printCenteredText(storageBackend, boxWidth);
        printStars(boxWidth);
    }

    private void printStars(int boxWidth) {
        for (int i = 0; i < boxWidth; i++) {
            System.out.print("*");
        }
        System.out.println();
    }

    private void printCenteredText(String text, int boxWidth) {
        int leftPadding = (boxWidth - text.length()) / 2;
        int rightPadding = boxWidth - leftPadding - text.length();
        System.out.println("*" + " ".repeat(leftPadding) + text + " ".repeat(rightPadding) + "*");
    }

    public void printASCIIArt() {
        String ascii = """
                ───▄██▄─██▄───▄
                ─▄██████████▄███▄
                ─▌████████████▌
                ▐▐█░█▌░▀████▀░░
                ░▐▄▐▄░░░▐▄▐▄░░░░
                """;
        System.out.println(ascii);
        System.out.println("Welcome to the Storage Backend CLI!");
        System.out.println("Here is some information about the backend:");
        System.out.println("You may modify .env file to change the configuration.");
    }


}
