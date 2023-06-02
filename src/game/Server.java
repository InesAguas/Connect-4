package game;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;


/**Class server for socket connections
 * @author Ines Aguas
 *
 */
public class Server {
	
	private int port;
	private ServerSocket serverSocket;
	private Socket client;
	private PrintWriter out;
	private BufferedReader in;
	private String nickname;
	private Scanner keyboard = new Scanner(System.in);
	private Game game;
	private Database database;
	
	/**Class constructor
	 * @param port the port to connect to
	 * @param nickname the nickname of the player
	 * @param database the database the program is connected to
	 */
	public Server(int port, String nickname, Database database) {
		this.port = port;
		this.nickname = nickname;
		this.database = database;
		
		try {
			InetAddress address = InetAddress.getLocalHost();
			System.out.println("IP of the local host: " + address.getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	/**Method to start the connection with a client socket
	 * @return true if the connection was successful
	 */
	public Boolean startConnection() {
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(10000);
			client = serverSocket.accept();
			System.out.println("Connection made successfully!");
			database.insertLog(nickname, "connected to a client");
			serverSocket.setSoTimeout(0);
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			String message;
			do {
				sendMessage("<" + nickname + "> <hello>;");
				message = in.readLine();
				System.out.println(message);
			}while(!message.contains("<hello>"));
			
			game(); 
			
			return true;
		} catch (SocketTimeoutException ste) {
			System.out.println("No client connected.");
			endConnection();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**Method to start a new game
	 * @return a number indicating if there is a winner
	 */
	public int gameStart() {
		String message;
		int playing;

		try {
			game = new Game(8);
			database.insertLog(nickname, "started a game");
			game.setID(database.insertGame(8));
			Random number = new Random();
			int n = number.nextInt(2);
			out.println(game.getID());
			sendMessage("<" + nickname + "> <start> <" + Integer.toString(n) + ">;");

			if (n == 1) {
				database.insertPlayerGame("one", nickname, game.getID());
				message = in.readLine();
				System.out.println(message);
				playing = myMove();
				
				return playing;
			}
			
			database.insertPlayerGame("two", nickname, game.getID());
			
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		}

	}
	
	/**Method for the game loop
	 * 
	 */
	public void game() {

		int playing = 0;
		Boolean end = false;
		
		do {
			playing = gameStart();

			while(playing >= 0) {
				playing = otherMove();
				
				if(playing >= 0) {
					playing = myMove();
				}

			}
			
			if (playing == -1) {
				end = endGame(true);
			} else if(playing == -2) {
				end = endGame(false);
			}
			
		}while(!end);
		
		endConnection();

		

	}
	
	
	/**Method for the turn of the socket to play
	 * @return a number indicating if the game end and who the winner is
	 */
	private int otherMove() {
		String message;
		int n;
		do {
			try {
				message = in.readLine();
				System.out.println(message);
				if(message.contains("withdraw")) {
					sendMessage("<" + nickname + "> <withdraw> <ack>;");
					message = in.readLine();
					System.out.println(message);
					n = -1;
				} else {
					if(game.insertPiece(Character.getNumericValue(message.charAt(message.length() - 3)), 'O')) {
						sendMessage("<" + nickname + "> <result> <valid>;");
						message = in.readLine();
						System.out.println(message);
						game.printGame();
						n = 0;
						if(game.gameWinner('O')) {
							sendMessage("<" + nickname + "> <status> <lose>;");
							message = in.readLine();
							System.out.println(message);
							n = -2;
						} else if(game.draw()) {
							sendMessage("<" + nickname + "> <status> <draw>;");
							message = in.readLine();
							System.out.println(message);
							n = -2;
						}

					} else {
						sendMessage("<" + nickname + "> <result> <notvalid>;");
						n = 1;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				n = -2;
			}
		}while(n == 1);
		
		return n;
	}
	
	/**Method for the server's turn
	 * @return a number indicating if the game ends and who the winner is
	 */
	private int myMove() {
		int n;
		do {
			try {
				String message;
				int column = inputInt("Your move: ");
				if(column < 0) {
					sendMessage("<" + nickname + "> <withdraw>;");
					message = in.readLine();
					System.out.println(message);
					n = -2;
				} else {
					sendMessage("<" + nickname + "> <move> <" + Integer.toString(column) + ">;");
					message = in.readLine();
					System.out.println(message);
					if(message.contains("notvalid")) {
						System.out.println("Invalid play.");
						n = 1;
					} else {
						game.insertPiece(column, 'X');
						database.insertMove(game.getID(), column, nickname);
						if(game.gameWinner('X')) {
							game.printGame();
							sendMessage("<" + nickname + "> <status> <win>;");
							message = in.readLine();
							System.out.println(message);
							n = -1;
						} else if(game.draw()) {
							game.printGame();
							sendMessage("<" + nickname + "> <status> <draw>;");
							message = in.readLine();
							System.out.println(message);
							n = -2;
						} else {
							sendMessage("<" + nickname + "> <result> <ack>;");
							game.printGame();
							n = 0;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				n = -2;
			}
		}while (n == 1);
		
		return n;
	}
	
	/**Method for the ending of a game
	 * @param win true if the server is the winner
	 * @return false if the client wants to play another game
	 */
	private Boolean endGame(Boolean win) {
		String message;
		if(win && !nickname.isBlank()) {
			database.insertLog(nickname, "won a game");
			database.updatePlayer(nickname, 1, 1, game.totalTime());
			int wins = database.multipleWins(nickname);
			if(wins != 0) {
				System.out.println("Congratulations, " + nickname + " you reached " + wins + " total victories!");
			}
		} else if(!nickname.isBlank()) {
			database.insertLog(nickname, "lost a game");
			database.updatePlayer(nickname, 1, 0, game.totalTime());
		}
		
		try {
			System.out.println("Total time for the game: " + game.totalTime());
			sendMessage("<" + nickname + "> <new> <?>;");
			message = in.readLine();
			System.out.println(message);
			if(message.charAt(message.length()-3) == 'y') {
				return false;
			} else {
				sendMessage("<" + nickname + "> <bye>;");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	/**Method to end a connection
	 * 
	 */
	public void endConnection() {
		try {
			database.insertLog(nickname, "ended a connection to a client");
			if(in != null) 
				in.close();
			
			if(out != null) 
				out.close();
			
			if(client != null) 
				client.close();
				
			if(serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Method to send a message
	 * @param message the message to send
	 */
	private void sendMessage(String message) {
		System.out.println(message);
		out.println(message);
	}
	
	/**Method to ask for a string
	 * @param message the message to display
	 * @return the input of the user
	 */
	public String inputString(String message) {
		System.out.print(message);
		return keyboard.nextLine();
	}
	
	
	/**Method to ask for an integer
	 * @param message the message to display
	 * @return the input of the user
	 */
	public int inputInt(String message) {
		while(true)
		     try {
		       return Integer.valueOf(inputString(message));
		     } catch (NumberFormatException nfe) {
		       System.out.println("Invalid input. Please write a number.");
		     }
	}
}
