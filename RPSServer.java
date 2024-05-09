package core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Multi-threaded server class for serving two clients playing rock paper scissors.
 * 
 * Includes an interface for showing the logs of the clients.
 * @author Carsen
 *
 */
public class RPSServer extends Application {
	private List<String> moves = new ArrayList<>();
	private CountDownLatch latch = new CountDownLatch(2);
	
	private TextArea logText = new TextArea();
	
	/**
	 * Start point of the JavaFX thread.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * Initalizes the user interface for the server, then delegates a thread to starting the server.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		StackPane root = new StackPane();
		root.getChildren().add(logText);
		
		Scene scene = new Scene(root, 400, 300);
		
		stage.setTitle("Server Interface");
		stage.setScene(scene);
		stage.show();
		
		new Thread(this::startServer).start();
	}
	
	/**
	 * Start the server on port 8000 and automatically connect to new requests.
	 * 
	 * Update user interface of the server and delegate the new client to a seperate thread.
	 */
	private void startServer() {
		try {
			// Create a new server socket
			ServerSocket serverSocket = new ServerSocket(8000);
			log("Server started at " + new Date() + '\n');
			
			int clientNumber = 0;
			
			while (true) {
				// Listen for new connections to the server, accept them if they come in.
				Socket socket = serverSocket.accept();
				log("Starting thread for client " + clientNumber++ + " at " + new Date() + '\n');
				
				// Take the new connection, and delegate it to a thread.
				RPS task = new RPS(socket);
				new Thread(task).start();
				
			}
		} catch (Exception e) {
			log("Error while establishing connection to client: " + e.toString());
		}
	}
	
	/**
	 * Append a line of text inside the UI.
	 * @param message
	 */
	private void log(String message) {
		logText.appendText(message + '\n');
	}
	
	/**
	 * Thread task given to manage clients.
	 * @author Carsen
	 *
	 */
	class RPS implements Runnable {
		private Socket socket;
		
		/**
		 * Pass the socket object from the server
		 * @param socket
		 */
		public RPS(Socket socket) {
			this.socket = socket;
		}
		
		/**
		 * Compute who wins in a rock, paper, scissors match.  Is not case sensitive, spelling sensitive.
		 * @param play1
		 * @param play2
		 * @return "It's a tie!" or the play that won.
		 */
		private String play(String play1, String play2) {
			play1 = play1.toLowerCase();
			play2 = play2.toLowerCase();
			
		    if (play1.equals(play2)) {
		        return "It's a tie!";
		    } else if ((play1.equals("rock") && play2.equals("scissors")) || (play1.equals("paper") && play2.equals("rock")) || (play1.equals("scissors") && play2.equals("paper"))) {
		        return play1;
		    } else {
		        return play2;
		    }
		}

		/**
		 * Establish data streams to and from the server.
		 * 
		 * Waits for both client threads to give their inputs before computing the winner, then serves the information to both clients.
		 */
		@Override
		public void run() {
			try {
				// Create data input and output streams to the client.
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
				
				while (true) {
					String move = inputFromClient.readUTF();
					log("Recieved a move: " + move + '\n');
					moves.add(move);
					latch.countDown();
					
					latch.await();
					
					String result = play(moves.get(0), moves.get(1));
					
					outputToClient.writeUTF(result);
					outputToClient.flush();
					
					moves.clear();
					
					latch = new CountDownLatch(2);
				}
				
			} catch (Exception e) {
				log("Error while processing client's request: " + e.toString());
			}
		}
	}
}
