package core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Client for playing rock paper scissors on a server, does not include computation for the game.
 * 
 * Includes an interface for chosing moves and whether you've won or lost.
 * @author Carsen
 *
 */
public class RPSClient extends Application {
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	
	private Button rockButton, paperButton, scissorsButton;
	private Text resultText;
	
	/**
	 * Start point of the JavaFX thread.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * Connect to the server and initalize the interface.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		try {
			// Connect to the server
			Socket socket = new Socket("localhost", 8000);
			
			// Establish data streams
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
			
			rockButton = createButton("Rock");
			paperButton = createButton("Paper");
			scissorsButton = createButton("Scissors");
			
			resultText = new Text("Waiting for moves...");
			resultText.setFill(Color.WHITE);
			
			VBox root = new VBox(10, rockButton, paperButton, scissorsButton, resultText);
			root.setMinSize(300, 200);
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
			
			rockButton.setOnAction(e -> sendMove("rock"));
			paperButton.setOnAction(e -> sendMove("paper"));
			scissorsButton.setOnAction(e -> sendMove("scissors"));
			
			stage.setScene(scene);
			stage.setTitle("Rock Paper Scissors Client");
			stage.show();
		} catch (Exception e) {
			System.out.println("Error on client: " + e.toString());
		}
	}
	
	/**
	 * Helper method for quickly creating buttons with different labels and styling them to the external sheet.
	 * @param label
	 * @return
	 */
	private Button createButton(String label) {
		Button button = new Button(label);
		button.getStyleClass().add("game-button");
		return button;
	}
	
	/**
	 * Sends move information to the server.
	 * 
	 * Properly disables the buttons to prevent overflow to the server.
	 * @param move
	 */
	private void sendMove(String move) {
		try {
			rockButton.setDisable(true);
			paperButton.setDisable(true);
			scissorsButton.setDisable(true);
			
			toServer.writeUTF(move);
			toServer.flush();
			
			String result = fromServer.readUTF();
			if (result.equals("It's a tie!")) {
				resultText.setText(result);
			} else {
				resultText.setText(result + " won this round");
			}
			
			rockButton.setDisable(false);
			paperButton.setDisable(false);
			scissorsButton.setDisable(false);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
