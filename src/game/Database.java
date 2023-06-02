package game;

import java.sql.*;
import java.util.Scanner;

/**Class database
 * @author Ines Aguas
 *
 */
public class Database {
	
	Connection conn;
	Statement st;
	ResultSet rs;
	private Scanner keyboard = new Scanner(System.in);
	
	/**Class constructor
	 * 
	 */
	public Database() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/game1", "root" , "Inocas99.");
		} catch (ClassNotFoundException e) {
			System.out.println("There was a problem with the database.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("There was a problem with the database.");
			e.printStackTrace();
		}
	}
	
	/**Method to check if a nickname exists in the database
	 * @param nickname the nickname to search for
	 * @return true if it already exists
	 */
	public Boolean playerExists(String nickname) {
		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT * FROM PLAYERS WHERE p_nickname = '" + nickname + "'");
			if(rs == null || !rs.next()) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			System.out.println("There was a problem with the database");
			e.printStackTrace();
		}
		return false;
	}
	
	/**Method to insert a player into the database
	 * @param nickname the nickname of the player
	 */
	public void insertPlayer(String nickname) {
		try {
			st = conn.createStatement();
			st.executeUpdate("INSERT INTO PLAYERS (P_NICKNAME) VALUES ('" + nickname + "')");
		} catch (SQLException e) {
			System.out.println("There was a problem with the database");
			e.printStackTrace();
		}
	}
	

	/**Method to update a player's information
	 * @param nickname the nickname of the player to update
	 * @param games the number of games to add
	 * @param victories the number of victories to add
	 * @param total_time the total time to add
	 */
	public void updatePlayer(String nickname, int games, int victories, String total_time) {
		try {
			st = conn.createStatement();
			st.executeUpdate("UPDATE players"
					+ "	SET p_total_games = p_total_games + " + games + ","
					+ " p_victories = p_victories + " + victories + ","
					+ " p_total_time = ADDTIME(p_total_time, '" + total_time + "')"
					+ " WHERE p_nickname = '" + nickname + "';");
		} catch (SQLException e) {
			System.out.println("There was a problem with the database");
			e.printStackTrace();
		}
	}
	
	/**Method to check if the players victories are a multiple of 10
	 * @param nickname the nickname to search for
	 * @return true if the victories are a multiple of 10
	 */
	public int multipleWins(String nickname) {
			try {
				st = conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT p_victories FROM players WHERE p_nickname = '" + nickname + "';");
				if(rs == null || !rs.next()) {
					return 0;
				} else {
					int victories = rs.getInt("p_victories");
					if(victories%10 == 0) {
						return victories;
					} else {
						return 0;
					}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
	}
	
	
	/**Method to list players
	 * @param query the query to execute
	 */
	public void listPlayers(String query) {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			if(rs == null || !rs.next()) {
				System.out.println("No results found.");
			} else {
				int page = 0;
				do {
					int min = (page)*10;
					int max =  10;
					rs = st.executeQuery(query + " LIMIT " + min + ", " + max);	
					if(rs.next()) {
						System.out.println("----- PAGE " + (page+1) + " -----");
						do {
							System.out.println("Nickname: " + rs.getString("p_nickname"));
							System.out.println("Games played: " + rs.getString("p_total_games"));
							System.out.println("Victories: " + rs.getString("p_victories"));
							System.out.println("Total time played: " + rs.getString("p_total_time"));
							System.out.println("--------------------------------------------------------------------");
						}while(rs.next());
					} else {
						System.out.println("Last page reached.");
					}
					
					System.out.println("0 - Go back\n1 - Previous page\n2 - Next page");
					int option = inputInt("[0/1/2]");
					switch(option) {
						case 0: page = -1; break;
						case 1: 
							if(page > 0) {
								page--;
							} else {
								System.out.println("Already on the first page.");
							} break;
						case 2: 
							page++; break;
						default: System.out.println("Invalid option");
					}
					
				}while(page >= 0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**Method to insert a game into the database.
	 * @param size the size of the board.
	 * @return the ID of the game
	 */
	public int insertGame(int size) {
		try {
			st = conn.createStatement();
			String query = "INSERT INTO games (g_size) VALUES (" + size + ");";
			st.executeUpdate(query);
			ResultSet rs = st.executeQuery("SELECT * FROM games ORDER BY g_id DESC");
			if(rs == null || !rs.next()) {
				return 0;
			} else {
				return rs.getInt("g_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**Method to insert a player into a game on the database
	 * @param number the number of the player (player one or player two)
	 * @param nickname the nickname of the player
	 * @param ID the game ID
	 */
	public void insertPlayerGame(String number, String nickname, int ID) {
		try {
			st = conn.createStatement();
			String query = "UPDATE GAMES "
					+ " SET g_player_" + number + " = '" + nickname + "'"
							+ " WHERE g_id = " + ID + ";";
			st.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**Method to insert a move into the database
	 * @param ID game ID
	 * @param column column of the move
	 * @param nickname player nickname
	 */
	public void insertMove(int ID, int column, String nickname) {
		try {
			st = conn.createStatement();
			st.executeUpdate("INSERT INTO MOVES(g_id, m_column, m_player)"
					+ " VALUES (" + ID + ", " + column + ", '" + nickname + "');");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**Method to list games that have a certain player
	 * @param nickname player nickname
	 * @return returns true if the player has any games saved
	 */
	public Boolean listGamesFromPlayer(String nickname) {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM games"
					+ " WHERE g_player_one = '" + nickname + "'"
					+ " OR g_player_two = '" + nickname + "';");
			
			if(rs == null || !rs.next()) {
				return false;
			} else {
				do {
					System.out.println("Game ID: " + rs.getString("g_ID"));
					System.out.println("Player one: " + rs.getString("g_player_one"));
					System.out.println("Player two: " + rs.getString("g_player_two"));
					System.out.println("Board size: " + rs.getString("g_size"));
					System.out.println("--------------------------------------------------------------------");
				}while(rs.next());
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**Method to select a game from the database
	 * @param nickname player nickname
	 * @param ID game ID
	 * @return the game found. returns null if no game with that ID and player exists
	 */
	public Game selectGame(String nickname, int ID) {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM games WHERE (g_player_one = '" + nickname + "' OR g_player_two = '" + nickname + "') AND g_id = " + ID + ";");
			if(rs == null || !rs.next()) {
				return null;
			} else {
				Game game = new Game(rs.getInt("g_size"));
				game.setID(ID);
				return game;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**Method used to replay the moves of a game.
	 * @param game The game to replay
	 * @return true if there's moves registered for this game
	 */
	public Boolean replayGame(Game game) {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM moves WHERE g_id = " + game.getID() + " ORDER BY m_id ASC");
			if(rs == null || !rs.next()) {
				return false;
			} else {
				do {
					game.addPlay(rs.getInt("m_column"));
					String nickname = rs.getString("m_player");
					if(nickname == null) {
						nickname = "Computer/Anonymous ";
					}
					System.out.println(nickname + " played column " + rs.getInt("m_column"));
					game.printGame();
					inputString("Press enter to continue: ");
				}while(rs.next());
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**Method to find the player with most wins
	 * @return the name of the player with the most wins
	 */
	public String playerMostWins() {
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM players ORDER BY p_victories DESC");
			if(rs == null || !rs.next()) {
				return null;
			} else {
				return rs.getString("p_nickname");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**Method to insert a log into the database
	 * @param player the player that made the action
	 * @param action the action that was made
	 */
	public void insertLog(String player, String action) {
		try {
			st = conn.createStatement();
			st.executeUpdate("INSERT INTO LOGS (l_date, l_time, l_player, l_action)"
					+ " VALUES (CURDATE(), NOW(), '" + player + "', '" + action +"');");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**Method to list logs from the database
	 * 
	 */
	public void listLogs() {
		try {
			st = conn.createStatement();
			String nickname = inputString("Nickname to search for (press enter to view all): ");
			String query = "SELECT * FROM logs WHERE l_player LIKE '%" + nickname + "%' ORDER BY l_id DESC";
			System.out.println(query);
			ResultSet rs = st.executeQuery(query);
			
			if(rs == null || !rs.next()) {
				System.out.println("No results found.");
			} else {
				int page = 0;
				do {
					int min = (page)*10;
					int max =  10;
					rs = st.executeQuery(query + " LIMIT " + min + ", " + max);
					if(rs.next()) {
						System.out.println("----- PAGE " + (page+1) + " -----");
						do {
							System.out.println("<" + rs.getDate("l_date") + "> <" + rs.getTime("l_time") + "> <" + rs.getString("l_player") + "> <" + rs.getString("l_action") + ">");
							System.out.println("--------------------------------------------------------------------");
						}while(rs.next());
					} else {
						System.out.println("Last page reached");
						page--;
					}
					
					System.out.println("0 - Go back\n1 - Previous page\n2 - Next page");
					int option = inputInt("[0/1/2]");
					switch(option) {
						case 0: page = -1; break;
						case 1: 
							if(page > 0) {
								page--;
							} else {
								System.out.println("Already on the first page.");
							} break;
						case 2: 
							page++; break;
						default: System.out.println("Invalid option");
					}
					
				}while(page >= 0);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
