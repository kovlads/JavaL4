package io;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * commands:
 * ls
 * touch file.txt
 * cat file.txt
 * cd path
 * get filename
 *
 * */
public class CloudController implements Initializable {
    public TextField input;
    public TextArea output;
    private Network network;
    private String downloadDir = "d:\\Downloads\\";

    public void sendCommand(ActionEvent actionEvent) throws IOException {
        String text = input.getText();
        input.clear();
        network.write(text);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            network = Network.get();
            new Thread(() -> {
                try {
                    while (true) {
                        String message = network.read();
                        Platform.runLater(() -> output.appendText(message));
                        if (message.equals("/quit")) {
                            network.close();
                            break;
                        }
                        else if (message.startsWith("get")) {
                            //получаем ответ вида "get имя_файла размер_файла"
                            String[] str = message.split(" ", 3);
                            String fileName = str[1];
                            long fileSize = Integer.parseInt(str[2].trim());
                            long receiveBytes = 0;
                            //создаем файл
                            File file = new File(downloadDir + fileName);
                            OutputStream fos = new FileOutputStream(file, false);
                            byte[] buffer = new byte[8192];
                            int ptr = 0;
                            //счетчик пакетов
                            int counter = 0;
                            //сохранили содержимое TextArea
                            String messages = output.getText();
                            while (fileSize > receiveBytes) {
                                ptr = network.readBytes(buffer);
                                fos.write(buffer, 0, ptr);
                                receiveBytes += ptr;
                                //выводим % закачки каждый 10-й пакет или при полной загрузке
                                if (counter % 10 == 0 || receiveBytes == fileSize) {
                                    String progress = "downloaded " + (receiveBytes * 100) / fileSize + "%";
                                    Platform.runLater(() -> output.setText(messages + progress));
                                }
                                counter++;
                            }
                            fos.close();
                            Platform.runLater(() -> output.appendText("\ndownloaded " + fileName));
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
