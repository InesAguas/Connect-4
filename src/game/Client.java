package game;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**Class client for socket connections
 * @author Ines Aguas
 *
 */
public class Client {
	
	private String host;
	private int port;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private Scanner keyboard = new Scanner(System.in);
	private String nickname;
	private Game game;
	private Database database;
	
	
	/**Class constructor
	 * @param host IP to connect to
	 * @param port port to connect to
	 * @param nickname players nickname
	 * @param database the database the program is connected to
	 */
	public Client(String host, int port, String nickname, Database database) {
		this.host = host;
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
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			socket.setSoTimeout(10000);
			if(socket.isConnected()) {
				System.out.println("Connection made successfully.");
				database.insertLog(nickname, "connected to a server");
				}
			System.out.println(in.readLine());
			socket.setSoTimeout(0);
			sendMessage("<" + nickname + "> <hello>;");
			game();
		} catch (SocketTimeoutException ste) {
			System.out.println("Time for response expired.");
			database.insertLog(nickname, "connection expired");
			endConnection();
		} catch (UnknownHostException e) {
			System.out.println("Something is wrong with the IP address. Try another IP.");
		} catch (IOException e) {
			System.out.println("Couldn't connect to a server. Try another port or IP.");
		}
		return false;
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
			game.setID(Integer.parseInt(in.readLine()));
			message = in.readLine();
			System.out.println(message);
			if(message.charAt(message.length()-3) == '1') {
				database.insertPlayerGame("two", nickname, game.getID());
				sendMessage("<" + nickname + "> <start> <ok>;");
				playing = otherMove();
				
				return playing;
			}
			
			database.insertPlayerGame("one", nickname, game.getID());
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
				playing = myMove();
				
				if(playing >= 0) {
					playing = otherMove();
				}
			}
			
			if (playing == -1) {
				end = endGame(true);
			} else if(playing == -2) {
				end = endGame(false);
			}
		} while (!end);
		
		endConnection();

	}
	
	/**Method for the turn of the server to play
	 * @return a number indicating if the game end and who the winner is
	 */
	private int otherMove() {
		String message;
		int n;
		do {
			try {
				message = in.readLine();
				System.out.println(message);
				if(message.contains("<status> <lose>")) {
					sendMessage("<" + nickname + "> <status> <ok>;");
					n = -1;
				} else if(message.contains("withdraw")) {
					sendMessage("<" + nickname + "> <withdraw> <ack>;");
					n = -1;
				} else if(message.contains("<status> <draw>")) {
					sendMessage("<" + nickname + "> <status> <ok>;");
					n = -2;
				} else {
					if(game.insertPiece(Character.getNumericValue(message.charAt(message.length() - 3)), 'X')) {
						sendMessage("<" + nickname + "> <result> <valid>;");
						message = in.readLine();
						if(message.contains("<status> <win>")) {
							game.printGame();
							System.out.println(message);
							sendMessage("<" + nickname + "> <status> <ok>;");
							n = -2;
						} else if(message.contains("<status> <draw>")) {
							game.printGame();
							System.out.println(message);
							sendMessage("<" + nickname + "> <status> <ok>;");
							n = -2;
						} else {
							System.out.println(message);
							game.printGame();
							n = 0;
						}
					} else {
						sendMessage("<" + nickname + "> <result> <notvalid>;");
						n = 1;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				n =  -2;
			}
		}while(n == 1);
		
		return n;
		
	}
	
	/**Method for the socket's turn
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
					sendMessage("<" + nickname + "> <ready>;");
					n = -2;
				} else {
					sendMessage("<" + nickname + "> <move> <" + Integer.toString(column) + ">;");
					message = in.readLine();
					System.out.println(message);
					if(message.contains("notvalid")) {
						System.out.println("Invalid play.");
						n = 1;
					} else {
						sendMessage("<" + nickname + "> <result> <ack>;");
						database.insertMove(game.getID(), column, nickname);
						game.insertPiece(column, 'O');
						game.printGame();
						n = 0;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				n = -2;
			}
		}while(n == 1);
		
		return n;
		
	}
	
	/**Method for the ending of a game
	 * @param win true if the client is the winner
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
			
			message = in.readLine();
			System.out.println(message);
			char choice;
			do {
				choice = inputString("New game [y/n]: ").charAt(0);
				if(choice != 'y' && choice != 'n') {
					System.out.println("Invalid option.");
				}
			}while(choice != 'y' && choice != 'n');
			
			if(choice == 'y') {
				sendMessage("<" + nickname + "> <new> <y>;");
				return false;
			} else {
				sendMessage("<" + nickname + "> <new> <n>;");
				message = in.readLine();
				System.out.println(message);
				return true;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return true;
		}
	}
	
	/**Method to end a connection
	 * 
	 */
	private void endConnection() {
		database.insertLog(nickname, "ended a connection to a server");
		try {
			if(in != null) 
				in.close();
			
			if(out != null) 
				out.close();
			
			if(socket != null) 
				socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Method used to send a message 
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
