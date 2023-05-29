package hdfs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class DataLoad {

	public static void main(String[] args) {

		System.out.println("Started ..");

		ResultSet resultset = getResultSet();

		try {

			System.out.println("Writing lines into local file");
			
			boolean sts = writeLineIntoFile(resultset);

			if (sts) {
				System.out.println("inside main if..");
				copyFileToHdfs();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("main over ..");

	}

	private static ResultSet getResultSet() {

		String jdbcUrl = "jdbc:mysql://localhost:3306/test";
		String username = "root";
		String password = "cloudera";

		String query = "SELECT * FROM salaries";

		try {

			System.out.println("Loading my sql driver...");

			Class.forName("com.mysql.jdbc.Driver");

			Connection conn = DriverManager.getConnection(jdbcUrl, username,
					password);

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(query);

			return rs;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	private static void copyFileToHdfs() throws IOException {

		System.out.println("inside copyFileToHdfs");

		Path localSrc = new Path("/home/cloudera/shared/LoadData/table/example.txt");
		Path dir = new Path("table6");

		Configuration conf = new Configuration();

		System.out.println("Conf  : " + conf);

		FileSystem fs = FileSystem.get(conf);

		System.out.println("fs  :" + fs.getConf());

		if (!fs.exists(dir)) {
			System.out.println("inside copy if..");
			fs.mkdirs(dir);
			System.out.println("Created table directory on HDFS");
		} else {
			// fs.delete(dir, true);
			System.out.println("inside copy else..");
		}

		Path dest1 = new Path("table6/" + "table.txt");

		fs.copyFromLocalFile(localSrc, dest1);

		fs.close();

		System.out.println("Copy done ..");

	}

	private static boolean writeLineIntoFile(ResultSet rs) throws SQLException {

		System.out.println("inside writeLineIntoFile");
		String filename = "/home/cloudera/shared/LoadData/table/example.txt";

		File file = new File(filename);
		String data = "";

		try {
			// Create the file if it doesn't exist
			if (!file.exists()) {
				file.createNewFile();
				System.out.println("File created: " + file.getName());
			}

			// Write lines to the file
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			System.out.println("fileW : " + fileWriter);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			while (rs.next()) {

				data = rs.getString(1) + "," + rs.getInt(2) + ","
						+ rs.getDouble(3) + "," + rs.getInt(4) + ","
						+ rs.getInt(5);

				bufferedWriter.write(data);
				bufferedWriter.newLine();

			}

			bufferedWriter.close();

			System.out.println("Data written to the file successfully.");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
