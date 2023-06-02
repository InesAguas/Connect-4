package game;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**Class game
 * @author Ines Aguas
 *
 */
public class Game {
	
	private int size;
	private char board[][];
	private Date startTime;
	private ArrayList<Integer> playsX;
	private ArrayList<Long> timeX;
	private ArrayList<Integer> playsO;
	private ArrayList<Long> timeO;
	private Date lastPlay;
	private int ID;
	
	
	/**Class constructor
	 * @param size the size of the board
	 */
	public Game(int size) {
		this.size = size;
		this.board = new char[size][size];
		this.startTime = new java.util.Date(System.currentTimeMillis());
		this.lastPlay = startTime;
		playsX = new ArrayList<Integer>();
		timeX = new ArrayList<Long>();
		playsO = new ArrayList<Integer>();
		timeO = new ArrayList<Long>();
	}
	
	/**Method to print the game to the screen
	 * 
	 */
	public void printGame() {
		for(int i = 0; i < size; i++) {
			String number = String.format("%02d", i+1);
			System.out.print(" " + number);
		}
		System.out.print("\n");
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				System.out.print("[" + board[i][j] + "]");
			}
			System.out.print("\n");
		}
	}
	
	/**Method to insert a piece into the board
	 * @param column the column to insert the piece into
	 * @param symbol the symbol to insert
	 * @return returns true if the column is valid
	 */
	public Boolean insertPiece(int column, char symbol) {
		if(column > 0 && column <= size) {
			int line = searchCell(column-1);
			if(line > size) {
				return false;
			} else {
				if(symbol == 'X') {
					playsX.add(column);
					calculateTimePlaysX();
				} else if(symbol == 'O'){
					playsO.add(column);
					calculateTimePlaysO();
				} else {
					symbol = 'O';
				}
				board[line][column-1] = symbol;
				return true;
			}
		} else {
			return false;
		}
	}
	
	/**Method to search a cell that is empty
	 * @param column the column to search the cell on
	 * @return the line that is empty in that column. Returns 30 if no cell is empty
	 */
	private int searchCell(int column) {
		for(int i = size - 1; i >= 0; i--) {
			if(board[i][column] == 0) {
				return i;
			}
		}
		return 30;
	}
	
	/**Method for the computer to play
	 * 
	 */
	public int computerPlay() {
		Boolean played = false;
		int column = bestPlay('O');
		
		do {
			if(column != 0) {
				played = insertPiece(column, 'C');
			}
			if(!played) {
				Random number = new Random();
				column = number.nextInt(size) + 1;
				played = insertPiece(column, 'C');
			}
		}while(!played);
		
		return column;
	}
	
	/**Method to determine if there is a winner
	 * @param symbol symbol to search for
	 * @return true if the person with that symbol wins
	 */
	public Boolean gameWinner(char symbol) {
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				if(board[i][j] == symbol) {
					//verifies next 3 elements in the row
					if(i+3 < size && board[i+1][j] == symbol && board[i+2][j] == symbol && board[i+3][j] == symbol) {
						return true;
					}
					//verifies next 3 elements in the column
					if(j+3 < size && board[i][j+1] == symbol && board[i][j+2] == symbol && board[i][j+3] == symbol) {
						return true;
					}
					//verifies next 3 elements diagonally to the bottom right
					if(i+3 < size && j+3 < size &&board[i+1][j+1] == symbol && board[i+2][j+2] == symbol && board[i+3][j+3] == symbol) {
						return true;
					}
					//verifies next 3 elements diagonally to the top right
					if(i-3 > 0 && j+3 < size && board[i-1][j+1] == symbol && board[i-2][j+2] == symbol && board[i-3][j+3] == symbol) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**Method to determine the best column to play. Verifies if there's 3 or 2 of the same symbol in the same row, column or diagonal.
	 * @param symbol symbol to search for
	 * @return the best column for a play. returns 0 if all plays seem equally good.
	 */
	public int bestPlay(char symbol) {
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				if(board[i][j] == symbol) {
					//verifies next 2 elements in the row are the same and 4th is empty
					if(i+3 < size && board[i+1][j] == symbol && board[i+2][j] == symbol && board[i+3][j] == 0) {
						return j+1;
					}
					//verifies next 2 elements in the column are the same and 4th is empty
					if(j+3 < size && board[i][j+1] == symbol && board[i][j+2] == symbol && board[i][j+3] == 0) {
						return j+4;
					}
					//verifies next 2 elements diagonally to the bottom right are the same and 4th is empty
					if(i+3 < size && j+3 < size && board[i+1][j+1] == symbol && board[i+2][j+2] == symbol && board[i+3][j+3] == 0) {
						return j+4;
					}
					//verifies next 2 elements diagonally to the top right are the same and 4th is empty
					if(i-3 > 0 && j+3 < size && board[i-1][j+1] == symbol && board[i-2][j+2] == symbol && board[i-3][j+3] == 0) {
						return j+4;
					}
					//verifies next element in the row is the same and third is empty
					if(i+2 < size && board[i+1][j] == symbol && board[i+2][j] == 0) {
						return j+1;
					}
					//verifies next element in the column is the same and third is empty
					if(j+2 < size && board[i][j+1] == symbol && board[i][j+2] == 0) {
						return j+3;
					}
					//verifies next element diagonally to the bottom right is the same and third is empty
					if(i+2 < size && j+2 < size && board[i+1][j+1] == symbol && board[i+2][j+2] == 0) {
						return j+3;
					}
					//verifies next element diagonally to the top right is the same and third is empty
					if(i-2 > 0 && j+2 < size && board[i-1][j+1] == symbol && board[i-2][j+2] == 0) {
						return j+3;
					}
				}
			}
		}
		return 0;
	}
	
	/**Method to determine if there is a draw
	 * @return true if there is no empty cell
	 */
	public Boolean draw() {
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				if(board[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**Method to calculate the times for the plays of the player with the X symbol
	 * 
	 */
	public void calculateTimePlaysX() {
		String time;
		long totalTime = 0;
		Date currentTime = new Date(System.currentTimeMillis());
		timeX.add(currentTime.getTime() - lastPlay.getTime());
		
		
		//time taken for the current move
		time = calculateTime(timeX.get(timeX.size() - 1));
		System.out.println("Time for this move: " + time);

		//total time taken for moves
		for(long i : timeX) {
			totalTime += i;
		}
		time = calculateTime(totalTime);
		System.out.println("Total time: " + time);
		
		//average time for moves
		totalTime = totalTime/timeX.size();
		time = calculateTime(totalTime);
		System.out.println("Average time: " + time);
		
		//Number of moves
		System.out.println("Total moves: " + timeX.size());
		
		lastPlay = currentTime;
	}
	
	/**Method to calculate the times for the plays of the player with the Y symbol
	 * 
	 */
	public void calculateTimePlaysO() {
		String time;
		long totalTime = 0;
		Date currentTime = new Date(System.currentTimeMillis());
		timeO.add(currentTime.getTime() - lastPlay.getTime());
		
		
		//time taken for the current move
		time = calculateTime(timeO.get(timeO.size() - 1));
		System.out.println("Time for this move: " + time);

		//total time taken for moves
		for(long i : timeO) {
			totalTime += i;
		}
		time = calculateTime(totalTime);
		System.out.println("Total time: " + time);
		
		//average time for moves
		totalTime = totalTime/timeO.size();
		time = calculateTime(totalTime);
		System.out.println("Average time: " + time);
		
		//Number of moves
		System.out.println("Total moves: " + timeO.size());
		
		lastPlay = currentTime;
	}
	
	/**Method to add a play to a game. This method is used for game replays.
	 * @param column column to add the play to
	 */
	public void addPlay(int column) {
		if(playsX.size() <= playsO.size()) {
			playsX.add(column);
			int line = searchCell(column-1);
			board[line][column-1] = 'X';
		} else {
			playsO.add(column);
			int line = searchCell(column-1);
			board[line][column-1] = 'O';
		}
	}
	
	/**Method to give a hint to a player when playing against the computer.
	 * 
	 */
	public void giveHint() {
		int c = bestPlay('X');
		if(c != 0) {
			System.out.println("Column " + c + " would be a good play!");
		} else {
			System.out.println("No hint available. All plays seem equally good");
		}
	}
	
	/**Method to determine the total time of the game
	 * @return the total time for the game
	 */
	public String totalTime() {
		return calculateTime(lastPlay.getTime() - startTime.getTime());
	}
	
	/**Method to set the ID of a game.
	 * @param ID the ID of the game
	 */
	public void setID(int ID) {
		this.ID = ID;
	}
	
	/**Method to get the ID of a game
	 * @return the ID of the game
	 */
	public int getID() {
		return ID;
	}
	
	/**Method to calculate times, from long to a string
	 * @param diff the long to calculate time from
	 * @return a string with the time in seconds, minutes and hours
	 */
	public String calculateTime(long diff) {
		int hours = (int) (diff / (1000 * 60 * 60));
		diff -= hours*1000*60*60;
		int minutes = (int) (diff/(1000 * 60));
		diff -= minutes*1000*60;
		int seconds = (int) (diff/1000);
		
		int time = hours * 10000 + minutes * 100 + seconds;
		
		String finalTime = String.format("%06d", time);
		finalTime = finalTime.substring(0, finalTime.length() - 4) + ":" + finalTime.substring(finalTime.length() - 4, finalTime.length() - 2) + ":" + finalTime.substring(finalTime.length() - 2, finalTime.length());
		return finalTime;
	}
	
}
