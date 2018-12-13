/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import java.util.Random; // to create random numbers for id (?)
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);

		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1 - sabrina
		try{ 
			
			System.out.print("\tEnter customer's first name: ");
			String fname = in.readLine(); // take in fname

			System.out.print("\tEnter customer's last name: ");
			String lname = in.readLine();

			System.out.print("\tEnter customer's address: ");
			String address = in.readLine();

			System.out.print("\tEnter customer's phone number with the format (xxx)xxx-xxxx: "); 
			String phone = in.readLine();

			int dummyVar = esql.executeQuery("SELECT setval(\'customer_id_seq\', (SELECT MAX(id) FROM Customer));");
			int id = esql.getCurrSeqVal("customer_id_seq") + 1;

			String insertCustomer = "INSERT INTO Customer (id, fname, lname, phone, address) VALUES ( \'" + id + "\', \'" + fname + "\', \'" + lname + "\', \'" + phone + "\', \'" + address + "\')";

			esql.executeUpdate(insertCustomer);
			System.out.println ("     Customer " + id + " has been added.\n");

		} catch(Exception e){
			System.err.println ("error: " + e.getMessage());
		}
	}
	// still need to check user input
	
	public static void AddMechanic(MechanicShop esql){//2 - bri 
		try {
			
			System.out.print("\tEnter first name: ");
			String fname = in.readLine();
			
			System.out.print("\tEnter last name: ");
	        String lname = in.readLine();
			
			System.out.print("\tEnter years experience: ");
	        int exp = Integer.parseInt(in.readLine());

        	int dummyVar = esql.executeQuery("SELECT setval(\'mechanic_id_seq\', (SELECT MAX(id) FROM Mechanic));");
			int mech_id = esql.getCurrSeqVal("mechanic_id_seq") + 1;

			String insertMechanic = "INSERT INTO Mechanic (id, fname, lname, experience) VALUES (\'" + mech_id + "\', \'" + fname + "\', \'" + lname + "\', \'" + exp + "\');";

			esql.executeUpdate(insertMechanic);
			System.out.println ("     Mechanic " + mech_id + " has been added.\n");

		}catch (Exception e){
			System.err.println (e.getMessage());
		}
	}//end AddMechanic 
	// still need to check user input
	
	public static void AddCar(MechanicShop esql){//3 - sabrina
		try{
			System.out.print("\tEnter Customer ID: ");
			int cust_id = Integer.parseInt(in.readLine());

			System.out.print("\tEnter VIN number: ");
			Long vinNum = Long.parseLong(in.readLine());

			System.out.print("\tEnter make of car: ");
			String carMake = in.readLine();

			System.out.print("\tEnter model of car: ");
			String carModel = in.readLine();

			System.out.print("\tEnter year of car: ");
			String carYear = in.readLine(); // change to year

			String insertCar = "INSERT INTO Car (vin, make, model, year) VALUES (\'" + vinNum + "\', \'" + carMake + "\', \'" + carModel + "\', \'" + carYear + "\');";

			// update ownership of car
			int dummyVar = esql.executeQuery("SELECT setval(\'owns_id_seq\', (SELECT MAX(ownership_id) FROM Owns));");

			int own_id = esql.getCurrSeqVal("owns_id_seq") + 1;

			String addToOwns = "INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES (\'" + own_id + "\', \'" + cust_id + "\', \'" + vinNum + "\');";

			esql.executeUpdate(insertCar);
			esql.executeUpdate(addToOwns);
			System.out.println ("     Customer " + cust_id + "\'s car has been added.\n");
			
		}catch (Exception e){
			System.err.println (e.getMessage());
		}		
	}
	// still need to check user input

	public static void InsertServiceRequest(MechanicShop esql){//4 -  bri
		int id = -1;
		int input = -1;
		String vin = "";

		try{
			System.out.print("\tEnter your last name: ");
			String userlname = in.readLine();
		
			// searches Customer for matching entry	
			String query = "SELECT id, fname, lname, phone, address FROM Customer WHERE Customer.lname = ('" + userlname  + "')";
			
			// assigns possible customers into searchResult
			List<List<String>> searchResult = esql.executeQueryAndReturnResult(query);
                	
			// check how many customers were returned
			if (searchResult.size() == 1) { // one result returned, get ID
				//String custID  = "SELECT id FROM Customer WHERE Customer.lname = ('" +  userlname + "')";
				//int cust1ID = esql.executeQueryAndPrintResult(custID);
				id = Integer.parseInt(searchResult.get(0).get(0));
			}
			else if (searchResult.size() > 1) { // more than one result returned
				esql.executeQueryAndPrintResult(query);
				System.out.print("\tWhich one? (1, 2, 3, etc.): ");
				input = Integer.parseInt(in.readLine());
				id = Integer.parseInt(searchResult.get(input - 1).get(0));
				System.out.println("You chose customer #" + input);
				System.out.println("Customer id is: " + id);
			}
			else { // no customer found, ask user to add customer
				System.out.println("We couldn't find your customer, would you like  to add one?: ");
				System.out.println("1. Yes\n2. No");
				int response = Integer.parseInt(in.readLine());
				if  (response == 1) {
					AddCustomer(esql);
				}
				else if (response == 2) {
					return;
				}	
			}

			// display cars that need to be serviced
			
			query = "SELECT vin, make, model, year FROM Car C, Owns O WHERE O.car_vin = C.vin and O.customer_id = '";
			query += id + "'";
			List<List<String>> customer_vins = esql.executeQueryAndReturnResult(query);
			if (customer_vins.size() > 0) { 
				System.out.println("Which car is yours? (1, 2, 3, etc.)");
				int cars = esql.executeQueryAndPrintResult(query);
				input = Integer.parseInt(in.readLine()); // customer chooses car for service request
				System.out.println("You chose the car: " + customer_vins.get(input - 1).get(1) + " " + customer_vins.get(input - 1).get(2));
				vin = customer_vins.get(input - 1).get(0);
			}
			else { // add car
				AddCar(esql);
			}
			
			// get today's date
			String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
		
			// get odometer reading
			System.out.println("How many miles are on the odometer? ");
			input = Integer.parseInt(in.readLine());
			if (input <= 0) {
				System.out.println("Enter a number greater than 0. ");
			}
			else {
				int odometer_reading = input;
				System.out.println("You entered the mileage: " + odometer_reading);
			}

		}catch (Exception e){
			System.err.println (e.getMessage());
		}		
	}//end InsertServiceRequest
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5 - sabrina
		try{

			
		}catch (Exception e){
			System.err.println (e.getMessage());
		}		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6 - bri
		try{
			//String query = "SELECT date,comment,bill FROM Closed_Request WHERE bill < 100";		
			String query = "SELECT customer_id, CR.rid, bill FROM Closed_Request AS CR, Service_Request AS SR WHERE SR.rid = CR.rid and CR.bill < 100";

		int rowCount = esql.executeQueryAndPrintResult(query);
                System.out.println ("total row(s): " + rowCount);
                System.out.printf("%n");
				
		}catch (Exception e){
			System.err.println (e.getMessage());
		}
	}//end ListCustomersWithBill
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7 - sabrina
		try{
			String query = "SELECT DISTINCT Customer.fname, Customer.lname FROM Customer WHERE Customer.id IN (SELECT Owns.customer_id FROM Owns GROUP by Owns.customer_id HAVING COUNT(car_vin) > 20);";

	        int rowCount = esql.executeQueryAndPrintResult(query);
	        System.out.println ("total row(s): " + rowCount);
	        System.out.printf("%n");
			
		}catch (Exception e){
			System.err.println (e.getMessage());
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8 - bri
		try{
			String query = "SELECT DISTINCT make, model, year, odometer FROM Car AS C, Service_Request AS S WHERE year < 1995 and S.car_vin = C.vin and S.odometer < 50000"; 
			
			int rowCount = esql.executeQueryAndPrintResult(query);
			System.out.println ("total row(s): " + rowCount);
			System.out.printf("%n");

		}catch (Exception e){
			System.err.println (e.getMessage());
		}
	} //endListCarsBefore1995
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9 - sabrina
		//
		try{
			String query = "SELECT * FROM ( SELECT DISTINCT c.make, c.model, COUNT(s.car_vin) AS count_vin FROM service_request s JOIN car c on s.car_vin = c.vin GROUP BY c.make, c.model) AS count_sr_vin ORDER BY count_vin desc;"; // needs user input to select
			// need to get selection (list list?)

		}catch (Exception e){
			System.err.println (e.getMessage());
		}
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10 - bri
		try{		
			String query = "SELECT C.fname , C.lname, Total FROM Customer AS C, (SELECT sr.customer_id, SUM(CR.bill) AS Total FROM Closed_Request AS CR, Service_Request AS SR WHERE CR.rid = SR.rid GROUP BY SR.customer_id) AS A WHERE C.id=A.customer_id ORDER BY A.Total DESC";
			//List<List<String>> customers = esql.executeQueryAndReturnResult(query);
			//for (int i = 0; i < customers.size(); i++) {
				//System.out.println((i + 1) + ", " + customers.get(i).get(0
			int rowCount = esql.executeQueryAndPrintResult(query);			
			System.out.println("total row(s): " + rowCount);
			System.out.printf("%n");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}//endListCustomersInDescendingOrder
	
}
