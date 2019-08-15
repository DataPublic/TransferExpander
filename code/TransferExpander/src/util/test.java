package util;

public class test {
	
	public static int getLoca(String pathFileA,String pathFileB){
    	//¼ì²âÁ½¸ö×Ö·û´®µÄ¾àÀë
    	//C:/PHDONE/runtime-EclipseApplication/bootique-master/bootique/src/test/java/io/bootique/Bootique_ConfigurationIT.java;
    	//C:/PHDONE/runtime-EclipseApplication/filebot-master/source/net/filebot/ui/transfer/FileExportHandler.java;
    	pathFileA = pathFileA.split(";",-1)[0];
    	pathFileB = pathFileB.split(";",-1)[0];
    	
    	if(pathFileA.equals(pathFileB)){
    		return 0;
    	}
    	
    	int PackIndexA = pathFileA.lastIndexOf("/");
    	String pathPackA = pathFileA.substring(0,PackIndexA);
    	
    	int PackIndexB = pathFileB.lastIndexOf("/");
    	String pathPackB = pathFileB.substring(0,PackIndexB);
    	
    	if((pathPackA.equals(pathPackB)))
    	{
    		return 1;
    	}
    	
    	return 2;
    }
	public static void main(String[] args){
		String temp1="C:/PHDONE/runtime-EclipseApplication/bootique-master/Bootique_ConfigurationIT.java;";
    	String temp2 = "C:/PHDONE/runtime-EclipseApplication/bootique-master/FileExportHandler.java;";
    	int t = temp1.lastIndexOf("/");
    	String result = temp1.substring(0,t+1);
    			
    	System.out.println(result);
	}

}
