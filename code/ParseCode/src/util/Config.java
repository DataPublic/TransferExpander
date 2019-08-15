package util;

import java.io.File;
import java.io.IOException;

public class Config {
	public static String projectName;
	public static String outFile = "/Users/huyamin/out.csv";
	static {
		File f=new File(Config.outFile);
		if(f.exists()) {
             f.delete();
		} else {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
