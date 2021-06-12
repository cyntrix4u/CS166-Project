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

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	static final int DEFAULT_LIMIT = 256;
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
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
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
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
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.out.println("Exception thrown!");
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

	public static boolean isValid(String userResponse){
		userResponse = userResponse.toLowerCase();
		if(userResponse.equals("y") || userResponse.equals("yes")){
			System.out.println("Input accepted!");
			return true;
		}else if(userResponse.equals("n") || userResponse.equals("no")) {
			System.out.println("Try inputting again!");
			return false;
		}
		System.out.println("Invalid input entered!");
		System.out.println("Try inputting again!");
		return false;	
	}

	/**
	 * Function to take in String input from user
	 * @param prompt Prompt that will be printed before taking in input
	 * @return user input string, will always be single token EX: firstName, hospitalName, etc
	 */
	public static String readStrInput(String prompt, int limit){
		String input="";
		boolean done = false;
		do{
			try{
				System.out.print(prompt + ": ");
				input = in.readLine();

				// Check user input
				if(input.isEmpty()|| input.length()>limit){
					throw new Exception("ERROR! Invalid input, please try again...");
				}

				done = true;
			}catch(Exception e){
				System.out.println(e.getMessage());

			}
		}while(!done);
		return input;
	}

	public static int readIntInput(String prompt){
		int input=0;
		boolean done = false;
		do{
			try{
				System.out.print(prompt + ": ");
				input = Integer.parseInt(in.readLine());

				// Check user input
				if(input<0/* || add additional conditions here */){
					throw new Exception("ERROR! Invalid input, please try again...");
				}

				done = true;
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}while(!done);
		return input;
	}


	public static void AddDoctor(DBproject esql) throws SQLException{//1
		//ex: 4,Rita Brock,Allergist,23
		String sqlQuery = "";
		int doctorID = 0;
		String name = "";
		String specialty = "";
		int did = 0;
		boolean valid = false;

		List<List<String>> temp = new ArrayList<List<String>>();
		boolean ans = false;

		do{
			doctorID = readIntInput("Enter doctor ID");
			name = readStrInput("Enter doctor's name",128);
			specialty = readStrInput("Enter doctor's specialty",128);
			did = readIntInput("Enter department ID");


			System.out.println("Doctor ID: "+ doctorID + "\nName: " + name + "\nSpecialty: " + specialty + "\nDept ID: "+ did);
			valid = isValid(readStrInput("Is this correct? (y/n)",3));
		}while(!valid);

		sqlQuery = String.format("SELECT doctor_id FROM doctor WHERE doctor_id=%d", doctorID);
		temp = esql.executeQueryAndReturnResult(sqlQuery);
		//System.out.println(temp);
		ans = temp.isEmpty();
		if(ans == false) {
			System.out.println("There is already an existing doctor with that ID!\nCan't add this doctor.\n");
		}
		else {
			//DONE DOCTOR build sql statement
			sqlQuery = String.format("INSERT INTO %s\nVALUES (%d, '%s', '%s', %d);","DOCTOR",doctorID,name,specialty,did);
			esql.executeUpdate(sqlQuery);
		}	
	}

	public static void AddPatient(DBproject esql) throws SQLException{//2
		//ex: 29,Melonie Helmers,F,52,7584 S. Thatcher Lane Indiana,1
		String sqlQuery = "";
		boolean valid = false;
		int patientID = 0;
		String name = "";
		char gender = 0; // m/M or f/F
		int age = 0;
		String address = "";
		int numberOfApts = 0;

		List<List<String>> temp = new ArrayList<List<String>>();
		boolean ans = false;

		do{
			patientID = readIntInput("Enter patient ID");
			name = readStrInput("Enter patient's name",128);
			gender = readStrInput("Enter patient's gender (M/F)", 1).toUpperCase().charAt(0);
			age = readIntInput("Enter patient's age");
			address = readStrInput("Enter patient's address",256);
			numberOfApts = readIntInput("Enter number of appointments for patient");

			System.out.println("Patient ID: " + patientID + ", name: " + name + ", gender: " + gender + ", \nage: " + age
			+ ", address: " + address + ", number of appointments: " + numberOfApts);
			valid = isValid(readStrInput("Is this correct? (y/n)",3));
		}while(!valid);

		sqlQuery = String.format("SELECT patient_id FROM patient WHERE patient_id=%d", patientID);
		temp = esql.executeQueryAndReturnResult(sqlQuery);
		//System.out.println(temp);
		ans = temp.isEmpty();
		if(ans == false) {
			System.out.println("There is already an existing patient with that ID!\nCan't add this patient.\n");
		}	
		else {
			//DONE PATIENT build sql statement 
		sqlQuery = String.format("INSERT INTO %s\nVALUES (%d, '%s', '%c', %d, '%s', %d);","PATIENT",patientID,name,gender,age, address, numberOfApts);
		esql.executeUpdate(sqlQuery);
		}	
	}

	public static void AddAppointment(DBproject esql) throws SQLException{//3
		//ex: 24,10/20/2021,10:00-17:00,AC
		String sqlQuery = "";
		boolean valid = false;
		int appointmentID = 0;
		String date = "";
		String timeSlot = ""; // hh:mm-hh:mm
		String status = ""; // PA, AV, WL, AC

		List<List<String>> temp = new ArrayList<List<String>>();
		boolean ans = false;

		do{
			appointmentID = readIntInput("Enter appointment ID");
			date = readStrInput("Enter date (mm/dd/yyyy)", 10);
			timeSlot = readStrInput("Enter time slot (hh:mm-hh:mm)", 11);
			status = readStrInput("Enter appointment status (PA/AV/WL/AC)", 2);
			if(!status.equals("PA") && !status.equals("AV") && !status.equals("WL") && !status.equals("AC")){
				System.out.println("Invalid Status! Please try again...");
				continue;
			}
			System.out.println("Appointment ID: " + appointmentID + ", date: " + date + ", time slot: " + timeSlot + ", status: " + status);
			
			valid = isValid(readStrInput("Is this correct? (y/n)",3));
		}while(!valid);

		sqlQuery = String.format("SELECT appnt_ID FROM appointment WHERE appnt_ID=%d", appointmentID);
		temp = esql.executeQueryAndReturnResult(sqlQuery);
		//System.out.println(temp);
		ans = temp.isEmpty();
		if(ans == false) {
			System.out.println("There is already an existing appointment with that ID!\nCan't add this appointment.\n");
		}	
		else {
			//DONE APPOINTMENT build sql statement 
			sqlQuery = String.format("INSERT INTO %s\nVALUES (%d, '%s', '%s', '%s');","APPOINTMENT",appointmentID,date,timeSlot,status);
			esql.executeUpdate(sqlQuery);
		}
	}


	public static void MakeAppointment(DBproject esql) throws SQLException{//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) throws SQLException{//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		String sqlQuery = "";
		boolean valid = false;
		int doctorID = 0;
		String date1 = "";
		String date2 = "";

		do{
			doctorID = readIntInput("Enter doctor ID");
			date1 = readStrInput("Enter start date (mm/dd/yyyy)", 10);
			date2 = readStrInput("Enter end date (mm/dd/yyyy)", 10);
			
			System.out.println("Doctor ID: " + doctorID + ", date range: " + date1 + "->" + date2);
			valid = isValid(readStrInput("Is this correct? (y/n)",3));
		}while(!valid);

		//DONE APPOINTMENT build sql statement 
		sqlQuery = String.format("SELECT appnt_ID, adate, time_slot " +
		"FROM DOCTOR D, HAS_APPOINTMENT HA, APPOINTMENT A " +
		"WHERE D.doctor_ID==HA.doctor_id AND HA.appt_id==A.appnt_ID AND D.doctor_ID==%d AND adate IN BETWEEN '%s' AND '%s';", doctorID,date1,date2);

		esql.executeQueryAndPrintResult(sqlQuery);
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) throws SQLException{//6
		// For a department name and a specific date, find the list of available appointments of the department
		String sqlQuery = "";
		boolean valid = false;
		String deptName = "";
		String date = "";

		do{
			deptName = readStrInput("Enter department name", 32);
			date = readStrInput("Enter date (yyyy-mm-dd)", 10);
			
			System.out.println("Department Name: " + deptName + ", date: "+date);
			valid = isValid(readStrInput("Is this correct? (y/n)",3));
		}while(!valid);

		//DONE APPOINTMENT build sql statement 
		sqlQuery = "SELECT appnt_ID, adate, time_slot, status" +
		"FROM APPOINTMENT A, HAS_APPOINTMENT HA,  DOCTOR Do, DEPARTMENT D" +
		String.format("WHERE A.appnt_ID==HA.appt_id AND HA.doctor_id==Do.doctor_ID AND D.dept_ID==Do.did AND D.name=='%s' AND A.adate=='%s' AND A.status=='AV';", deptName,date);

		esql.executeQueryAndPrintResult(sqlQuery);
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) throws SQLException{//7
		// Count number of different types of appointments per doctors and list them in descending order
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) throws SQLException{//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
	}
}
