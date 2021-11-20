import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/*
JDBC setup (on Emily's labthreesixfive)

-- MySQL setup:
drop table if exists lab7_reservations, lab7_rooms;

grant all on etruon08.lab7_rooms to jtran179@'%';
grant all on etruon08.lab7_reservations to maprasad@'%';

CREATE TABLE IF NOT EXISTS lab7_rooms (
    RoomCode char(5) PRIMARY KEY,
    RoomName varchar(30) NOT NULL,
    Beds int(11) NOT NULL,
    bedType varchar(8) NOT NULL,
    maxOcc int(11) NOT NULL,
    basePrice DECIMAL(6,2) NOT NULL,
    decor varchar(20) NOT NULL,
    UNIQUE (RoomName)
);

CREATE TABLE IF NOT EXISTS lab7_reservations (
    CODE int(11) PRIMARY KEY,
    Room char(5) NOT NULL,
    CheckIn date NOT NULL,
    Checkout date NOT NULL,
    Rate DECIMAL(6,2) NOT NULL,
    LastName varchar(15) NOT NULL,
    FirstName varchar(15) NOT NULL,
    Adults int(11) NOT NULL,
    Kids int(11) NOT NULL,
    FOREIGN KEY (Room) REFERENCES lab7_rooms (RoomCode)
);
    
INSERT INTO lab7_rooms SELECT * FROM INN.rooms;
    
-- Use DATE_ADD to shift reservation dates to current year
INSERT INTO lab7_reservations SELECT CODE, Room,
    DATE_ADD(CheckIn, INTERVAL 132 MONTH),
    DATE_ADD(Checkout, INTERVAL 132 MONTH),
    Rate, LastName, FirstName, Adults, Kids FROM INN.reservations;

-- Shell init:
export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.
export HP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/winter2020?autoReconnect=true\&useSSL=false
export HP_JDBC_USER=jmustang
export HP_JDBC_PW=...
 */

public class InnReservations {
    public static void main(String[] args) {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Welcome to our CSC 365 Inn Reservation System!");
			System.out.println("Please select an option: Rooms and Rates (1), Reservations (2), Reservation Changes (3), Reservation Cancellation (4), Detailed Reservation Information (5), Revenue (6) (0 = quit)");
			int demoNum = sc.nextInt(); 

			InnReservations ir = new InnReservations();
				// int demoNum = Integer.parseInt(args[0]);
			while (demoNum != 0) {
				switch (demoNum) {
				case 0: break;
				case 1: ir.fr1(); break;
				case 2: ir.fr2(); break;
				case 3: ir.fr3(); break;
				case 4: ir.fr4(); break;
				case 5: ir.demo5(); break;
				}
				System.out.println("Please select an option: Rooms and Rates (1), Reservations (2), Reservation Changes (3), Reservation Cancellation (4), Detailed Reservation Information (5), Revenue (6) (0 = quit)");
				demoNum = sc.nextInt(); 
			sc.close();
			}
				
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		} catch (Exception e2) {
				System.err.println("Exception: " + e2.getMessage());
			}
    }

    // FR1 - Establish JDBC connection, execute DDL statement
    private void fr1() throws SQLException {

        System.out.println("FR1: Rooms and Rates: Rooms will be listed based on popularity from highest to lowest.\r\n");
        
	// Step 0: Load MySQL JDBC Driver
	// No longer required as of JDBC 2.0  / Java 6
	try{
	    Class.forName("com.mysql.jdbc.Driver");
	    System.out.println("MySQL JDBC Driver loaded");
	} catch (ClassNotFoundException ex) {
	    System.err.println("Unable to load JDBC Driver");
	    System.exit(-1);
	}

	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
							   System.getenv("HP_JDBC_USER"),
							   System.getenv("HP_JDBC_PW"))) {
	    // Step 2: Construct SQL statement
	    // String sql = "ALTER TABLE hp_goods ADD COLUMN AvailUntil DATE";
        String sql = "SELECT * FROM res";

	    // Step 3: (omitted in this example) Start transaction

	    // Step 4: Send SQL statement to DBMS
	    try (Statement stmt = conn.createStatement();
		 ResultSet rs = stmt.executeQuery(sql)) {

		// Step 5: Receive results
		while (rs.next()) {
		    String roomCode = rs.getString("RoomCode");
		    String roomName = rs.getString("RoomName");
		    Float popularity = rs.getFloat("Popularity");
            String nextAvail = rs.getString("nextAvail");
		    String checkOut = rs.getString("CheckOut");
		    int totDays = rs.getInt("totDays");
            System.out.format("%s %s %.2f %s %s %d \n", 
			                    roomCode, roomName, popularity, 
								nextAvail, checkOut, totDays);
		}
	    }

	    // Step 6: (omitted in this example) Commit or rollback transaction
	}
	// Step 7: Close connection (handled by try-with-resources syntax)
    }
    

    // Demo2 - Establish JDBC connection, execute SELECT query, read & print result
    private void fr2() throws SQLException {

        System.out.println("FR2: Reservations\r\n");
        
	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
							   System.getenv("HP_JDBC_USER"),
							   System.getenv("HP_JDBC_PW"))) {
	    // Step 2: Construct SQL statement
	    Scanner sc = new Scanner(System.in);
		System.out.println("Please enter your first name: ");
		String firstName = sc.nextLine();
		System.out.println("Please enter your last name: ");
		String lastName = sc.nextLine();
		System.out.println("Please enter your room code: (Enter 'Any' to indicate no preference)");
		String roomCode = sc.nextLine();
		System.out.println("Please enter your desired bed type: (Enter 'Any' to indicate not preference)");
		String bedType = sc.nextLine();
		System.out.println("Please enter your anticipated check-in date (yyyy-mm-dd): ");
		String checkIn = sc.nextLine();
		System.out.println("Please enter your anticipated check-out date (yyyy-mm-dd): ");
		String checkOut = sc.nextLine();
		System.out.println("Please enter the number of adults: ");
		Integer numAdults = sc.nextInt();
		System.out.println("Please enter the number of children: ");
		Integer numChildren = sc.nextInt();
		System.out.println("You have entered: " + firstName + lastName);
		System.out.println(roomCode + " " + bedType + " " + checkIn + " " + checkOut + " " + numAdults + " " + numChildren);
		Integer totOcc = numAdults + numChildren;
		// String sql = "SELECT * FROM hp_goods";

	    // Step 3: (omitted in this example) Start transaction

	    // Step 4: Send SQL statement to DBMS

		// Check if occ exceeds maxOcc
		String getMaxOcc = "SELECT MAX(maxOcc) AS maxOccAllowed FROM lab7_rooms";
		conn.setAutoCommit(false);
	    try (PreparedStatement pstmt = conn.prepareStatement(getMaxOcc))
		{
			ResultSet rs = pstmt.executeQuery(getMaxOcc);
			while(rs.next()) {
				int maxOcc = rs.getInt("maxOccAllowed");
				if (totOcc > maxOcc){
					System.out.println("No suitable rooms are available");
					break;
					// RETURN TO MAIN MENU
				}
			}
			
		}
		String[] roomCodeArr = {"AOB", "CAS", "FNA", "HBB", "IBD", "IBS", "MWC", "RND", "RTE", "SAY", "TAA"};
		String[] bedTypeArr = {"Queen", "King", "Double"};

		if (roomCode == "Any")
		{
			Random r=new Random();        
			int randomNumber = r.nextInt(roomCodeArr.length);
			roomCode = roomCodeArr[randomNumber];
		}
		if (bedType == "Any")
		{
			Random r=new Random();        
			int randomNumber = r.nextInt(roomCodeArr.length);
			roomCode = bedTypeArr[randomNumber];
		}

		String checkResAvailSQL = 
		"SELECT COUNT(*) " +
		"FROM lab7_reservations " +
		"WHERE " +
		"(CheckIn >= ? AND CheckIn < ? AND RoomCode = ?) " +
		"OR " +
		"(CheckOut > ? AND CheckOut <= ? AND RoomCode = ?) " +
		"OR " +
		"(CheckIn <= ? AND CheckOut >= ? AND RoomCode = ?";


		conn.setAutoCommit(false);
	    try (PreparedStatement pstmt = conn.prepareStatement(checkResAvailSQL))
		{
			pstmt.setString(1, checkIn);
			pstmt.setString(2, checkOut);
			pstmt.setString(3, roomCode);

			pstmt.setString(4, checkIn);
			pstmt.setString(5, checkOut);
			pstmt.setString(6, roomCode);

			pstmt.setString(7, checkIn);
			pstmt.setString(8, checkOut);
			pstmt.setString(9, roomCode);
			ResultSet rs = pstmt.executeQuery(checkResAvailSQL);

			int conflictCount = -1;
			while(rs.next())
			{
				conflictCount = rs.getInt("COUNT(*)");
			}
			if (conflictCount > 0) {
				System.out.println("Conflict encountered");
			}
			else {
				System.out.println("No conflict encountered");
				// make reservation
			}
		}

		// Step 5: Receive results
		// while (rs.next()) {
		//     String flavor = rs.getString("Flavor");
		//     String food = rs.getString("Food");
		//     float price = rs.getFloat("price");
		//     System.out.format("%s %s ($%.2f) %n", flavor, food, price);
		// }
	    }

	    // Step 6: (omitted in this example) Commit or rollback transaction
	}
	// Step 7: Close connection (handled by try-with-resources syntax)


    // Demo3 - Establish JDBC connection, execute DML query (UPDATE)
    // -------------------------------------------
    // Never (ever) write database code like this!
    // -------------------------------------------
    private void fr3() throws SQLException {

    	System.out.println("FR3: Make changes to an existing reservation.\r\n");
        
		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
								System.getenv("HP_JDBC_USER"),
								System.getenv("HP_JDBC_PW"))) {
			// Step 2: Construct SQL statement
			Scanner scanner = new Scanner(System.in).useDelimiter("\n");
			System.out.print("Enter a reservation code: ");
			Integer resCode = scanner.nextInt();
			String checkExistsSQL = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE CODE = ?";
			conn.setAutoCommit(false);
			try (PreparedStatement pstmt = conn.prepareStatement(checkExistsSQL)) 
			{
				// Step 4: Send SQL statement to DBMS
				pstmt.setInt(1, resCode);
				ResultSet rs = pstmt.executeQuery();
				while(rs.next()) {
					int resExists = rs.getInt("COUNT(*)");
					if (resExists == 0) {
						System.out.println("Reservation does not exist.");
					}
					else {
						System.out.print("Update first name (1), last name (2), begin date (3), end date (4), number of children (5), or number of adults (6) of reservation? (0 to quit) ");
						Integer updateOption = scanner.nextInt();
						while (updateOption != 0) {
							if (updateOption == 1) {
								System.out.print("Enter new first name ('no change' if no change desired): ");
								String newFirst = scanner.next();
								if(!newFirst.equals("no change")) {
									String updateFirst = "UPDATE etruon08.lab7_reservations SET FirstName = ? WHERE CODE = ?";
									try (PreparedStatement pstmtFirst = conn.prepareStatement(updateFirst)) {
										pstmtFirst.setString(1, newFirst);
										pstmtFirst.setInt(2, resCode);
										pstmtFirst.executeUpdate();
										System.out.format("Updated Reservation #%d first name to %s%n", resCode, newFirst);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d first name not updated%n", resCode);
								}
							}
							if (updateOption == 2) {
								System.out.print("Enter new last name ('no change' if no change desired): ");
								String newLast = scanner.next();
								if (!newLast.equals("no change")) {
									String updateLast = "UPDATE etruon08.lab7_reservations SET LastName = ? WHERE CODE = ?";
									try (PreparedStatement pstmtLast = conn.prepareStatement(updateLast)) {
										pstmtLast.setString(1, newLast);
										pstmtLast.setInt(2, resCode);
										pstmtLast.executeUpdate();
										System.out.format("Updated Reservation #%d last name to %s%n", resCode, newLast);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d last name not updated%n", resCode);
								}
							}
							if (updateOption == 3) {
								System.out.print("Enter new check-in date (format 'yyyy-mm-dd' or 'no change' if no change desired): ");
								String newDateStr = scanner.next();
								if (!newDateStr.equals("no change")) {
									LocalDate newCheckin = LocalDate.parse(newDateStr);
									String RoomCode = "";
									String getRoom = "SELECT Room FROM etruon08.lab7_reservations WHERE CODE = ?";
									String findConflicts = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE ROOM = ? and CheckIn <= ? and Checkout > ? and CODE <> ?";
									try(PreparedStatement roomStmt = conn.prepareStatement(getRoom)) {
										roomStmt.setInt(1, resCode);
										ResultSet rs2 = roomStmt.executeQuery();
										while(rs2.next()) {
											RoomCode = rs2.getString("Room");
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}
									try(PreparedStatement validateCheckin = conn.prepareStatement(findConflicts)) {
										validateCheckin.setString(1, RoomCode);
										validateCheckin.setDate(2, java.sql.Date.valueOf(newCheckin));
										validateCheckin.setDate(3, java.sql.Date.valueOf(newCheckin));
										validateCheckin.setInt(4, resCode);
										ResultSet rs3 = validateCheckin.executeQuery();
										while(rs3.next()) {
											//String conflictRoom = rs3.getString("Room");
											//String conflictCode = rs3.getString("CODE");
											//System.out.print(conflictRoom + " " + conflictCode);
											Integer conflictCount = rs3.getInt("COUNT(*)");
											//System.out.println(conflictCount);
											if (conflictCount > 0) {
												System.out.println("Date conflicts with other reservations, please select a different date.");
											}
											else {
												String updateCheckin = "UPDATE etruon08.lab7_reservations SET CheckIn = ? WHERE CODE = ?";
												try (PreparedStatement pstmtCheckin = conn.prepareStatement(updateCheckin)) {
													pstmtCheckin.setDate(1, java.sql.Date.valueOf(newCheckin));
													pstmtCheckin.setInt(2, resCode);
													pstmtCheckin.executeUpdate();
													System.out.format("Updated Reservation #%d checkin date to %s%n", resCode, newDateStr);
													conn.commit();
												} catch (SQLException e) {
													conn.rollback();
													System.out.println(e.getMessage());
												}
											}
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}

								}
								else {
									System.out.format("Reservation #%d checkin date not updated%n", resCode);
								}
							}
							if (updateOption == 4) {
								System.out.print("Enter new check-out date (format 'yyyy-mm-dd' or 'no change' if no change desired): ");
								String newDateStr = scanner.next();
								if (!newDateStr.equals("no change")) {
									LocalDate newCheckout = LocalDate.parse(newDateStr);
									String RoomCode = "";
									String getRoom = "SELECT Room FROM etruon08.lab7_reservations WHERE CODE = ?";
									String findConflicts = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE ROOM = ? and CheckIn < ? and Checkout >= ? and CODE <> ?";
									try(PreparedStatement roomStmt = conn.prepareStatement(getRoom)) {
										roomStmt.setInt(1, resCode);
										ResultSet rs2 = roomStmt.executeQuery();
										while(rs2.next()) {
											RoomCode = rs2.getString("Room");
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}
									try(PreparedStatement validateCheckout = conn.prepareStatement(findConflicts)) {
										validateCheckout.setString(1, RoomCode);
										validateCheckout.setDate(2, java.sql.Date.valueOf(newCheckout));
										validateCheckout.setDate(3, java.sql.Date.valueOf(newCheckout));
										validateCheckout.setInt(4, resCode);
										ResultSet rs3 = validateCheckout.executeQuery();
										while(rs3.next()) {
											//String conflictRoom = rs3.getString("Room");
											//String conflictCode = rs3.getString("CODE");
											//System.out.print(conflictRoom + " " + conflictCode);
											Integer conflictCount = rs3.getInt("COUNT(*)");
											//System.out.println(conflictCount);
											if (conflictCount > 0) {
												System.out.println("Date conflicts with other reservations, please select a different date.");
											}
											else {
												String updateCheckout = "UPDATE etruon08.lab7_reservations SET Checkout = ? WHERE CODE = ?";
												try (PreparedStatement pstmtCheckout = conn.prepareStatement(updateCheckout)) {
													pstmtCheckout.setDate(1, java.sql.Date.valueOf(newCheckout));
													pstmtCheckout.setInt(2, resCode);
													pstmtCheckout.executeUpdate();
													System.out.format("Updated Reservation #%d checkout date to %s%n", resCode, newDateStr);
													conn.commit();
												} catch (SQLException e) {
													conn.rollback();
													System.out.println(e.getMessage());
												}
											}
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}

								}
								else {
									System.out.format("Reservation #%d checkout date not updated%n", resCode);
								}
							}
							if (updateOption == 5) {
								System.out.print("Enter new number of children ('no change' if no change desired): ");
								String NumChildrenStr = scanner.next();
								if(!NumChildrenStr.equals("no change")) {
									int newNumChildren = Integer.parseInt(NumChildrenStr);
									String updateNumChildren = "UPDATE etruon08.lab7_reservations SET Kids = ? WHERE CODE = ?";
									try (PreparedStatement pstmtNC = conn.prepareStatement(updateNumChildren)) {
										pstmtNC.setInt(1, newNumChildren);
										pstmtNC.setInt(2, resCode);
										pstmtNC.executeUpdate();
										System.out.format("Updated Reservation #%d number of children to %s%n", resCode, newNumChildren);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d number of children not updated%n", resCode);
								}
							}
							if (updateOption == 6) {
								System.out.print("Enter new number of adults ('no change' if no change desired): ");
								String NumAdultsStr = scanner.next();
								if(!NumAdultsStr.equals("no change")) {
									int newNumAdults = Integer.parseInt(NumAdultsStr);
									String updateNumAdults = "UPDATE etruon08.lab7_reservations SET Adults = ? WHERE CODE = ?";
									try (PreparedStatement pstmtNA = conn.prepareStatement(updateNumAdults)) {
										pstmtNA.setInt(1, newNumAdults);
										pstmtNA.setInt(2, resCode);
										pstmtNA.executeUpdate();
										System.out.format("Updated Reservation #%d number of adults to %s%n", resCode, newNumAdults);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d number of adults not updated%n", resCode);
								}
							}
							System.out.print("Update first name (1), last name (2), begin date (3), end date (4), number of children (5), or number of adults (6) of reservation? (0 to quit) ");
							updateOption = scanner.nextInt();
						}
					}
				}
				// Step 5: Handle results
				//System.out.format("Updated %d records for %s pastries%n", rowCount, flavor);
				// Step 6: Commit or rollback transaction
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
			}
		}
		// Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }

    // Demo4 - Establish JDBC connection, execute DML query (UPDATE) using PreparedStatement / transaction    
    private void fr4() throws SQLException {

        System.out.println("FR4: Cancel an existing reservation.\r\n");
        
		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
								System.getenv("HP_JDBC_USER"),
								System.getenv("HP_JDBC_PW"))) {
			// Step 2: Construct SQL statement
				Scanner scanner = new Scanner(System.in).useDelimiter("\n");
				System.out.print("Enter a reservation code: ");
				Integer resCode = scanner.nextInt();
				String checkExistsSQL = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE CODE = ?";
				conn.setAutoCommit(false);
				try (PreparedStatement pstmt = conn.prepareStatement(checkExistsSQL)) 
				{
					// Step 4: Send SQL statement to DBMS
					pstmt.setInt(1, resCode);
					ResultSet rs = pstmt.executeQuery();
					while(rs.next()) {
						int resExists = rs.getInt("COUNT(*)");
						if (resExists == 0) {
							System.out.println("Reservation does not exist.");
						}
						else {
							System.out.format("Are you sure you want to delete Reservation #%d? (y/n) ", resCode);
							String userResp = scanner.next();
							if (userResp.equals("n")) {
								break;
							}
							else {
								String deleteRes = "DELETE FROM etruon08.lab7_reservations WHERE CODE = ?";
								try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteRes)) {
									pstmtDelete.setInt(1, resCode);
									pstmtDelete.executeUpdate();
									conn.commit();
									System.out.format("Reservation #%d successfully deleted.%n", resCode);
								} catch (SQLException e) {
									System.out.println(e.getMessage());
									conn.rollback();
								}
							}
						}
					}
					conn.commit();
				} catch (SQLException e) {
					System.out.println(e.getMessage());
					conn.rollback();
				}
		}
	// Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }



    // Demo5 - Construct a query using PreparedStatement
    private void demo5() throws SQLException {

        System.out.println("demo5: Run SELECT query using PreparedStatement\r\n");
        
	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
							   System.getenv("HP_JDBC_USER"),
							   System.getenv("HP_JDBC_PW"))) {
	    Scanner scanner = new Scanner(System.in);
	    System.out.print("Find pastries with price <=: ");
	    Double price = Double.valueOf(scanner.nextLine());
	    System.out.print("Filter by flavor (or 'Any'): ");
	    String flavor = scanner.nextLine();

	    List<Object> params = new ArrayList<Object>();
	    params.add(price);
	    StringBuilder sb = new StringBuilder("SELECT * FROM hp_goods WHERE price <= ?");
	    if (!"any".equalsIgnoreCase(flavor)) {
		sb.append(" AND Flavor = ?");
		params.add(flavor);
	    }
	    
	    try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
		int i = 1;
		for (Object p : params) {
		    pstmt.setObject(i++, p);
		}

		try (ResultSet rs = pstmt.executeQuery()) {
		    System.out.println("Matching Pastries:");
		    int matchCount = 0;
		    while (rs.next()) {
			System.out.format("%s %s ($%.2f) %n", rs.getString("Flavor"), rs.getString("Food"), rs.getDouble("price"));
			matchCount++;
		    }
		    System.out.format("----------------------%nFound %d match%s %n", matchCount, matchCount == 1 ? "" : "es");
		}
	    }

	}
    }
    

}
