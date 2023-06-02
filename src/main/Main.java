package main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import game.*;

/**Main class, contains menus and user interaction
 * @author Ines Aguas
 *
 */
public class Main {
	
	private static String nickname;
	private static Scanner keyboard = new Scanner(System.in);
	private static Database database = null;
	private static Game game;
	private static Game saved = null;
	private static Date started;
	
	
	/**Main method
	 * @param args
	 */
	public static void main(String[] args) {
		started = new Date(System.currentTimeMillis());
		database = new Database();
		playerLogin();
		firstMenu();
	}
	
	/**Method for a player to login.
	 * Asks for a nickname, verifies if it's already in database. If not, inserts it into database.
	 * 
	 */
	public static void playerLogin() {
		nickname = inputString("Nickname (Press enter for anonymous): ");
		if(nickname.isBlank()) {
			System.out.println("You're playing as anonymous and your stats won't be saved.");
			nickname = "";
		} else {
			if(!database.playerExists(nickname)) {
				database.insertPlayer(nickname);
			}
			
			if(database.playerMostWins().compareToIgnoreCase(nickname) == 0) {
				System.out.println("Congratulations, you are currently the player with most wins!");
			}
		}
		
		database.insertLog(nickname, "logged in");
		
		System.out.println("Welcome " + nickname);
	}
	
	/**Method to show the first menu
	 * 
	 */
	public static void firstMenu() {
		int option;
		do {
			System.out.println("0 - Quit\n1 - Play against computer\n2 - Play locally\n3 - Play online\n4 - List players\n"
					+ "5 - View a game replay\n6 - View logs");
			option = inputInt("Option [0-6]: ");
			
			switch(option) {
			case 0: endApp(); database.insertLog(nickname, "closed program"); break;
			case 1: computerGame(); database.insertLog(nickname, "selected game against computer"); break;
			case 2: localGame(); database.insertLog(nickname, "selected local game"); break;
			case 3: onlineGame(); database.insertLog(nickname, "selected online game"); break;
			case 4: listPlayers(); database.insertLog(nickname, "listed players"); break;
			case 5: if(!nickname.isBlank()) {
				replayGame(); database.insertLog(nickname, "viewed a game replay");
			} else {
				System.out.println("Not available for anonymous players.");
			} break;
			case 6: database.listLogs(); database.insertLog(nickname, "viewed logs"); break;
			default: System.out.println("Invalid input.");
			}
		}while(option != 0);
	}
	
	/**Method to play against the computer
	 * 
	 */
	public static void computerGame() {
		int size = 8;
		int option;
		Boolean played;
		char choice;
		
		if(saved != null) {
			do {
				System.out.println("Continue saved game?");
				choice = inputString("[y/n]: ").charAt(0);
				if(choice == 'y' ) {
					game = saved;
					database.insertLog(nickname, "continued a game");
					saved = null;
				} else {
					do {
						size = inputInt("Game size [8-20]: ");
						if(size > 20 || size < 8) {
							System.out.println("Size must be between 8 and 20.");
						}
					} while(size > 20 || size < 8);
					
					game = new Game(size);
					game.setID(database.insertGame(size));
					database.insertPlayerGame("one", nickname, game.getID());
					database.insertLog(nickname, "started a game");
					saved = null;
				}
			}while(choice != 'y' && choice != 'n');
		} else {
			do {
				size = inputInt("Game size [8-20]: ");
				if(size > 20 || size < 8) {
					System.out.println("Size must be between 8 and 20.");
					
				}
			} while(size > 20 || size < 8);
			
			game = new Game(size);
			game.setID(database.insertGame(size));
			database.insertPlayerGame("one", nickname, game.getID());
			database.insertLog(nickname, "started a game");
		}
		
		game.printGame();
		
		do {
			option = inputInt("Chose a column [Type a negative number to end game and 0 for a hint]: ");
			if(option > 0) {
				played = game.insertPiece(option, 'X');
				if(played) {
					database.insertMove(game.getID(), option, nickname);
					game.printGame();
					if(game.gameWinner('X')) {
						System.out.println("Congrats, you're the winner!");
						played = false;
					} else if(game.draw()) {
						played = false;
						System.out.println("There was a draw.");
					} else {
						System.out.println("It's the computer's turn!");
						database.insertMove(game.getID(), game.computerPlay(), "");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						game.printGame();
						if(game.gameWinner('O')) {
							System.out.println("Too bad, you lost! Computer wins.");
							
							played = false;
						} else if(game.draw()) {
							played = false;
							System.out.println("There was a draw.");
						}
					}
				} else {
					System.out.println("Invalid column.");
					played = true;
				}
			} else if (option == 0) {
				game.giveHint();
				played = true;
			} else {
				do {
					System.out.println("Save game? (If you save you can return to your game later)");
					choice = inputString("[y/n]: ").charAt(0);
					if(choice == 'y') {
						saved = game;
					} else if(choice == 'n') {
						if(!nickname.isBlank()) {
							database.updatePlayer(nickname, 1, 0, game.totalTime());
						}
					}else {
						System.out.println("Invalid input.");
					}
				}while(choice != 'y' && choice != 'n');
				
				played = false;
			}
		}while(played);
		
		if(saved == null) {
			System.out.println("Total time for the game: " + game.totalTime());
		}
		
		if(game.gameWinner('X')) {
			if(!nickname.isBlank()) {
				database.insertLog(nickname, "won a game");
				database.updatePlayer(nickname, 1, 1, game.totalTime());
				int wins = database.multipleWins(nickname);
				if(wins != 0) {
					System.out.println("Congratulations, " + nickname + " you reached " + wins + " total victories!");
				}
			}
		} else {
			if(!nickname.isBlank()) {
				database.insertLog(nickname, "lost a game");
				database.updatePlayer(nickname, 1, 0, game.totalTime());
			}
		}	
	}
	
	
	
	/**Method to play locally with another player
	 * 
	 */
	public static void localGame() {
		String player1;
		String player2;
		String secondNickname;
		do {
			secondNickname = inputString("Nickname (Press enter for anonymous): ");
			if(secondNickname.compareToIgnoreCase(nickname) == 0 && !secondNickname.isBlank()) {
				System.out.println("You can't have the same name as player 1");
			}
		}while(secondNickname.compareToIgnoreCase(nickname) == 0 && !secondNickname.isBlank());
		if(secondNickname.isBlank()) {
			System.out.println("You're playing as anonymous and your stats won't be saved.");
			secondNickname = "";
			player2 = "player2";
		} else {
			if(!database.playerExists(secondNickname)) {
				database.insertPlayer(secondNickname);
			}
			player2 = secondNickname;
		}
		
		if(nickname.isBlank()) {
			player1 = "player1";
		} else {
			player1 = nickname;
		}
		
		int size;
		do {
			size = inputInt("Game size [8-20]: ");
		} while(size > 20 || size < 8);
		game = new Game(size);
		
		game.setID(database.insertGame(size));
		database.insertPlayerGame("one", nickname, game.getID());
		database.insertPlayerGame("two", secondNickname, game.getID());
		database.insertLog(nickname, "started a game");
		database.insertLog(secondNickname, "started a game");
		
		int player = 1;
		int winner = -1;
		int option;
		Boolean played;
		
		game.printGame();
		
		do {
			if(player == 1) {
				do {
					System.out.println(player1 + "'s turn");
					option = inputInt("Chose a column [Type a negative number to end game]: ");
					if(option >= 0) {
						played = game.insertPiece(option, 'X');
						if(played) {
							database.insertMove(game.getID(), option, nickname);
							game.printGame();
							player = 2;
							if(game.gameWinner('X')) {
								System.out.println("Congrats, " + player1 + " you won!");
								winner = 1;
							} else if(game.draw()) {
								System.out.println("There was a draw!");
								winner = 0;
							}
						} else {
							System.out.println("Invalid column.");
						}
					} else {
						System.out.println("Congrats, " + player2 + " you won! " + player1 + " gives up.");
						winner = 2;
						played = true;
					}
				}while(!played);
			} else {
				do {
					System.out.println(player2 + "'s turn");
					option = inputInt("Chose a column [Type a negative number to end game]: ");
					if(option >= 0) {
						played = game.insertPiece(option, 'O');
						if(played) {
							database.insertMove(game.getID(), option, secondNickname);
							game.printGame();
							player = 1;
							if(game.gameWinner('O')) {
								System.out.println("Congrats, " + player2 + " you won!");
								winner = 2;
							} else if(game.draw()) {
								System.out.println("There was a draw!");
								winner = 0;
							}
						} else {
							System.out.println("Invalid column.");
						}
					} else {
						System.out.println("Congrats, " + player1 + " you won! " + player2 + " gives up.");
						winner = 1;
						played = true;
					}
				}while(!played);
			}
		}while(option >= 0 && winner < 0);
		
		System.out.println("Total time for the game: " + game.totalTime());
		
		if(!nickname.isBlank()) {
			if(winner == 1) {
				database.insertLog(nickname, "won a game");
				database.updatePlayer(nickname, 1, 1, game.totalTime());
				int wins = database.multipleWins(nickname);
				if(wins != 0) {
					System.out.println("Congratulations, " + nickname + " you reached " + wins + " total victories!");
				}
			} else {
				database.insertLog(nickname, "lost a game");
				database.updatePlayer(nickname, 1, 0, game.totalTime());
			}
		}
		
		if(!secondNickname.isBlank()) {
			if(winner == 2) {
				database.insertLog(secondNickname, "won a game");
				database.updatePlayer(secondNickname, 1, 1, game.totalTime());
				int wins = database.multipleWins(secondNickname);
				if(wins != 0) {
					System.out.println("Congratulations, " + secondNickname + " you reached " + wins + " total victories!");
				}
			} else {
				database.insertLog(secondNickname, "lost a game");
				database.updatePlayer(secondNickname, 1, 0, game.totalTime());
			}
		}
		
	}
	
	/**Method to play online
	 * 
	 */
	public static void onlineGame() {
		System.out.println("Enter 1 to be the server, and 2 to be the client");
		int choice = inputInt("Choice [1/2]: ");
		if(choice == 1) {
			int port = inputInt("Indicate a port to connect to: ");
			Server server = new Server(port, nickname, database);
			server.startConnection();
		} else if (choice == 2) {
			String IP = inputString("IP of the server to connect to: ");
			int port = inputInt("Indicate a port to connect to: ");
			Client client = new Client(IP, port, nickname, database);
			client.startConnection();
		} else {
			System.out.println("Invalid option. Try again");
		}

	}
	
	/**Method to list players in the database
	 * 
	 */
	public static void listPlayers() {
		String search = inputString("Nickname to search for (press enter to view all): ");
		
		int option = 0;
		String order = "";
		
		do {
			System.out.println("0 - Go back\n1 - Order by nickname\n2 - Order by total number of games\n"
					+ "3 - Order by number of victories\n4 - Order by total game time");
			option = inputInt("Opcao: ");
			if(option > 0 && option < 5) {
				System.out.println("Ascending or descending order? (Ascending will be chosen by default)");
				char asc = inputString("[a/d]").charAt(0);
				if(asc == 'd' || asc == 'D') {
					order = "DESC";
				} else {
					order = "ASC";
				}
			}
			switch(option) {
			case 0: System.out.println("Going back..."); break;
			case 1: database.listPlayers("SELECT * FROM players WHERE p_nickname LIKE '%" + search + "%' ORDER BY p_nickname " + order); break;
			case 2: database.listPlayers("SELECT * FROM players WHERE p_nickname LIKE '%" + search + "%' ORDER BY p_total_games " + order); break;
			case 3: database.listPlayers("SELECT * FROM players WHERE p_nickname LIKE '%" + search + "%' ORDER BY p_victories " + order);; break;
			case 4: database.listPlayers("SELECT * FROM players WHERE p_nickname LIKE '%" + search + "%' ORDER BY p_total_time " + order);; break;
			default: System.out.println("Opcao invalida");
			}
		}while(option < 0 || option > 4);
	}
	
	/**Method to replay the moves of a game
	 * 
	 */
	public static void replayGame() {
		if(database.listGamesFromPlayer(nickname)) {
			int ID = inputInt("ID of game to replay: ");
			Game replay = database.selectGame(nickname, ID);
			if(replay == null) {
				System.out.println("Invalid ID. Try again.\n");
			} else {
				if(!database.replayGame(replay)) {
					System.out.println("No moves registered for this game.");
				}
				
			}
		} else {
			System.out.println("You haven't played any games yet");
		}
	}
	
	/**Method to close application. Tells the total time the app was running and the date it started/ended.
	 * 
	 */
	public static void endApp() {
		Date ended = new Date(System.currentTimeMillis());
		long diff = ended.getTime() - started.getTime();
		
		int hours = (int) (diff / (1000 * 60 * 60));
		diff -= hours*1000*60*60;
		int minutes = (int) (diff/(1000 * 60));
		diff -= minutes*1000*60;
		int seconds = (int) (diff/1000);
		
		DateFormat shortDateFormat = new SimpleDateFormat ( "EEEE; yyyy-MM-dd HH:mm:ss" );
		
		System.out.println("Beginning of process: " +  shortDateFormat.format(started));
		System.out.println("End of process: " + shortDateFormat.format(ended));
		System.out.println("Total execution time: " + (ended.getTime() - started.getTime()) + " Milliseconds (" 
				+ seconds + " Seconds; " + minutes + " Minutes; " + hours + " Hours)");
		System.out.println("Goodbye " + nickname);
	}
	
	/**Method to ask for a string
	 * @param message the message to display
	 * @return the input of the user
	 */
	public static String inputString(String message) {
		System.out.print(message);
		return keyboard.nextLine();
	}
	
	
	/**Method to ask for an integer
	 * @param message the message to display
	 * @return the input of the user
	 */
	public static int inputInt(String message) {
		while(true)
		     try {
		       return Integer.valueOf(inputString(message));
		     } catch (NumberFormatException nfe) {
		       System.out.println("Invalid input. Please write a number.");
		     }
	}

}
