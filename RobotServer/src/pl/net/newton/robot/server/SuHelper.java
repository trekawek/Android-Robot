package pl.net.newton.robot.server;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SuHelper {
	public static List<String> execAsAdmin(String... commands) throws Exception {
		List<String> res = new ArrayList<String>();
		
		Process process = Runtime.getRuntime().exec("su");
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		for (String single : commands) {
		   os.writeBytes(single + "\n");
		   os.flush();
		   //res.add(osRes.readLine());
		}
		os.writeBytes("exit\n");
		os.flush();
		//process.destroy();
		process.waitFor();

		return res;
	}
	
	public static void makeFifo(String path) throws Exception {
		execAsAdmin("mkfifo " + path);
		chmod(path, "666");
	}
	
	public static void chmod(String path, String privileges) throws Exception {
		execAsAdmin("chmod " + privileges + " " + path);
	}
}
