package main;

import util.*;

import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


// based on trainSelfExpansionFile
public class Step2_ComputePriorityAddNewRules {
    public static int idIndex = 0;
    public static int filesIndex = 1;
    public static int abbrIndex = 2;
    public static int nameIndex = 3;
    public static int typeOfIdentifierIndex = 4;
    public static int subclassIndex = 5;
    public static int subsubclassIndex = 9;
    public static int parentsIndex = 13;
    public static int ancestorIndex = 17;
    public static int methodsIndex = 21;
    public static int fieldsIndex = 25;
    public static int commentIndex = 29;
    public static int typeIndex = 33;
    public static int enclosingClassIndex = 37;
    public static int assignmentIndex = 41;
    public static int methodInvocatedIndex = 45;
    public static int parameterArgumentIndex = 49;
    public static int parameterIndex = 53;
    public static int enclosingMethodIndex = 57;
    public static int argumentIndex = 61;
    public static int expansionIndex = 65;

    static ArrayList<String[]> eData = new ArrayList<>();
    public static ArrayList<String[]> lines = new ArrayList<>();
    public static ArrayList<String> SelectedRules=new ArrayList<String>();
    public static ArrayList<Double> metrics=new ArrayList<Double>();
    
    public static Map<String,String> result = new HashMap();

    public static void main(String[] args) throws Exception {
       // generateTrainData();
        for (int o = 1; o <= 1; o++) {
            long begintime =System.currentTimeMillis();
            GlobleVariable.trainProjectName = "train" + o;
            GlobleVariable.trainSelfExpansionFile = "C:/PHDONExia/ICSE2019/ExpData/trainProprity/train2_selfExpansion.csv";
            GlobleVariable.priorityFile = "data/jiang/" + GlobleVariable.trainProjectName + "_priority.csv";
            //System.setOut(new PrintStream(new File(GlobleVariable.priorityFile)));
            ArrayList<String> file = Util.readFile(GlobleVariable.trainSelfExpansionFile);

            //完成数据的读入，将文件中的数据读入到ArrayList中
            
            for (int i = 1; i < file.size(); i++) {
                String[] strs = file.get(i).split(",", -1);
                lines.add(strs);
            }

           // System.out.println("typeOfIdentifier,relation,H,#Type,#TypeRelation,#TypeRelationH,#rightExpan,precision,metric");
            String[] abbrTypeName = {"ClassName", "MethodName",
                    "FieldName", "ParameterName", "VariableName"};
            String[] relationName = {"subclass","subsubclass","parents","ancestor","methods","fields","comment","type","enclosingClass","assignment","methodInvocated","parameterArgument","parameter","enclosingMethod", "argument", "LinsenAbbr", "ComputerAbbr"};
            String[] heuName = {"H1", "H2", "H3"};

            
            load_dataset();
            
            
       //     f(lines,abbrTypeName,relationName,heuName);
            
    
            long endtime =System.currentTimeMillis();

            long costTime = (endtime - begintime);
            System.err.println(costTime);
           
        }
             
    }
    
    public static int getLoca(String pathFileA,String pathFileB){
    	//检测两个字符串的距离
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
    
    public static double transScore(int abbrLength,int abbrLoca, ArrayList<String[]> data){
    	//计算一行数据中，缩写词

    	double currentScore; //当前规则得分
    	String currentRule; //当前规则
    	int expNum = 0; //可扩展数量
    	int corrNum = 0; //正确数量
    	
    	for(int i = 0;i < data.size();i++){
    		String currentAbbr = data.get(i)[abbrIndex]; //当前缩写词
    		String currentPath = data.get(i)[filesIndex]; //当前文件路径
    		String currentExpa = data.get(i)[expansionIndex];//当前扩展词
    		int currentAbbrlength = currentAbbr.length();
    		if(currentAbbrlength > 2){
    			currentAbbrlength = 3;
    		}
    		if(currentAbbrlength==abbrLength){//满足长度规则
    			
    			for(int j =0;j<eData.size();j++){
    				
    				String targetAbbr = eData.get(j)[abbrIndex];//缩写词
    				String targetExpa = eData.get(j)[expansionIndex];//扩展词
    				String targetPath = eData.get(j)[filesIndex];//路径
    				
    				if(currentAbbr.equals(targetAbbr) && getLoca(currentPath,targetPath)==abbrLoca ){ //满足路径规则
    					if(!targetExpa.equals("")){
    						//可扩展数量
    						expNum ++;
    						if(currentExpa.replaceAll(" ", "").equalsIgnoreCase(targetExpa.replaceAll(" ",""))){
    							corrNum ++;
    						}// end if
    					}//end if
    				}// end for 路径
    			}//end for eData
    		}//end for 长度
    	
    	}//end for data
    	
        double p = 1.0*corrNum/expNum;
        double m = p * Util.sigmoid(corrNum);//计算度量值
        
        if (Double.isNaN(p)) {
//          System.out.print("0,");
        	}
        if (Double.isNaN(m)) {
//          System.out.println("0");
          return 0;
      } else {
//          System.out.println(m);
          return m;
      }
    }
    
    public static String score(ArrayList<String[]> data,String[] abbrTypeName,String[] relationName,String[] heuName) throws SQLException{
    	
    	//第一部分，根据原来的规则算分
    	//[1,2,3,4,6]
    	
    	int[] relationIndex = {subclassIndex,subsubclassIndex,parentsIndex,ancestorIndex,methodsIndex,fieldsIndex,commentIndex,typeIndex,enclosingClassIndex,assignmentIndex,methodInvocatedIndex,parameterArgumentIndex,parameterIndex,enclosingMethodIndex, argumentIndex};
    	double maxm=-1;
    	String r="";
        for (int i = 0; i < abbrTypeName.length; i++) {
        	// 这是算分的
            for (int j = 0; j < relationIndex.length; j++) {
                for (int k = 0; k < heuName.length; k++) {
//                    System.out.print(abbrTypeName[i] + "," + relationName[j] + "," + heuName[k] + ",");
                    double tm=handleTypeRelationH(abbrTypeName[i], relationIndex[j], heuName[k], data);
                    
                    if(!SelectedRules.contains(abbrTypeName[i] + "," + relationName[j] + "," + heuName[k] + ",") && tm>maxm){
                    	maxm=tm;
                    	r=abbrTypeName[i] + "," + relationName[j] + "," + heuName[k] + ",";
                    	finalindex=relationIndex[j];
                    }
                }
            }
            // linsen abbr
//            System.out.print(abbrTypeName[i] + "," + relationName[relationIndex.length] + ",,");
            double tm=handleAbbr(abbrTypeName[i], "Linsen", data);
            if(!SelectedRules.contains(abbrTypeName[i] + "," + relationName[relationIndex.length]+ ",,") && tm>maxm){
            	maxm=tm;
            	r=abbrTypeName[i] + "," + relationName[relationIndex.length]+ ",,";
            }

            // computer abbr
//            System.out.print(abbrTypeName[i] + "," + relationName[relationIndex.length+1] + ",,");
            tm=handleAbbr(abbrTypeName[i], "Computer", data);
            if(!SelectedRules.contains(abbrTypeName[i] + "," + relationName[relationIndex.length+1]+ ",,") && tm>maxm){
            	maxm=tm;
            	r=abbrTypeName[i] + "," + relationName[relationIndex.length+1]+ ",,";
            }
        }
    //     metrics.add(new Double(maxm));  //metrics最终没有使用？
//         SelectedRules.add(r);
//         System.out.println(maxm+"   "+r);
         String kgRule = r;
         double kgScore = maxm;
    	
    	
    	//第二部分，根据迁移规则算分
    	//[5,4,3,2,1]
    	String transRule = "";
    	double transScore = -1;
    	double currentScore; //当前数据得分
    	int abbrLength = 4;
    	int abbrLoca = 3;
    	double weight = 0;
    	for(int i  = 1;i < abbrLength;i++){
    		for(int l = 0;l < abbrLoca;l++){
    		
    			if(l==0){
    				weight=1;
    			}else if(l==1){
    				weight=1;
    			}else{
    				weight=1;
    			}
    			
    			
    			if(!SelectedRules.contains(i+","+l)){
	    			currentScore = transScore(i,l,data)*weight;
//	    			System.out.println("-------------------");
	        		if(currentScore > transScore){
	        			transScore = currentScore;
	        			transRule = i+","+l;
	        		}
    			}
    		}
    		
    	}
//    	System.out.println(transScore+ " "+transRule);
    	
    	//增加语义关系迁移规则
    	String semanticRule="InSameKG";
    	double semanticScore=-1;
    	double currentSemanticScore = 0;
    	    	
    	if(!SelectedRules.contains(semanticRule)){
    		currentSemanticScore = SemanticScore(data);
    		if(currentSemanticScore>semanticScore){
    			semanticScore= currentSemanticScore;
    			
    		}
    	}
    	
    	//两个分比较一下，选最好的
    	//三个分比较一下，选择得分最高的一条规则
    	String final_result = "";
    	double final_score = -1;
    	if (transScore > kgScore){
    		
    		final_score= transScore;
    		final_result =transRule;
    		
//    		SelectedRules.add(transRule);
//    		System.out.println(transScore+ "!"+transRule);
//    		result.put(transRule, "!"+transScore);
//    		return transRule;
    		
    	}else{
    		final_score=kgScore;
    		final_result=kgRule;
    	}
    	
    	if(final_score>semanticScore){
    		SelectedRules.add(final_result);
    		System.out.println(final_score+ "!"+final_result);
    		result.put(final_result, "!"+final_score);
    		return final_result;
    	}
    	
    	
    	SelectedRules.add(semanticRule);
    	System.out.println(currentSemanticScore+"!"+semanticRule);
    	result.put(semanticRule, "!"+currentSemanticScore);
    	return semanticRule;
    }
    
    static ArrayList<String[]> abbr_line = new ArrayList<>();
    static ArrayList<String[]> kg_line = new ArrayList<>();
    
    
    public static void load_dataset() throws SQLException{
    	//将变量表abbreviation转存为Arraylist
    	ResultSet abbr_rs = connectDatabase("select * from abbreviationmaven");
		
    	ArrayList<String> abbr_list = new ArrayList<String>();
    	while(abbr_rs.next()){
    		String abbr_id = abbr_rs.getString(1);
    		String abbr_name = abbr_rs.getString(2);
    		//String abbr_type = abbr_rs.getString(3);
    		String variable_id = abbr_rs.getString(3);
    		String variable_name = abbr_rs.getString(4);
    		//String expan_id = abbr_rs.getString(6);
    		//String expan_name = abbr_rs.getString(7);
    		//String relation = abbr_rs.getString(8);
    		String variableType= abbr_rs.getString(5);
    		String location = abbr_rs.getString(6);
    		
    		abbr_list.add(abbr_id+ ","+ abbr_name +","+ variable_id + ","+ variable_name+","+ variableType+","+ location);
    	}
    	
		for(int i=0;i<abbr_list.size();i++)
		{
			String[] strs = abbr_list.get(i).split(",",-1);
			abbr_line.add(strs);
		}
	
		System.out.println(abbr_line.size());
		//将知识图谱表kg转存为ArrayList
		
		ResultSet kg_rs = connectDatabase("select * from kgmaven");
		ArrayList<String> kg_list = new ArrayList<String>();

    	while(kg_rs.next()){
    		String start = kg_rs.getString(1);
    		String sname = kg_rs.getString(2);
    		String end = kg_rs.getString(3);
    		String ename = kg_rs.getString(4);
    		String relation = kg_rs.getString(5);
    		kg_list.add(start+ ","+ sname +","+ end + ","+ ename +","+ relation);
    	}
    	
		for(int i=0;i<kg_list.size();i++)
		{
			String[] strs = kg_list.get(i).split(",",-1);
			kg_line.add(strs);
		}
		System.out.println(kg_line.size());
    }
    public static double SemanticScore(ArrayList<String[]> data)  {

    	
		//--------------------------------------------------------------------------
		int expNum =0;
		int correctNum=0;
		for(int i=0;i<data.size();i++){
			String current_abbr = data.get(i)[abbrIndex];
			String location = data.get(i)[idIndex];
			String current_expan = data.get(i)[expansionIndex];
			
			for(int j=0;j<eData.size();j++){
				String target_abbr= eData.get(j)[abbrIndex];
				String target_location= eData.get(j)[idIndex];
				String target_expan = eData.get(j)[expansionIndex];
				
				if(current_abbr.equals(target_abbr) && InSameKG(current_abbr, target_abbr,location,target_location)){
					if(!target_expan.equals("")){
						expNum++;
						if(current_expan.replaceAll(" ", "").equalsIgnoreCase(target_expan.replaceAll(" ",""))){
							
							correctNum++;
						}//end if 
					}//end if
				
				}//end if
			}//end for
		}//end for data
		
		double p=1.0*correctNum/expNum;
		double m=p*Util.sigmoid(correctNum);
		  if (Double.isNaN(p)) {
//	          System.out.print("0,");
	        	}
	        if (Double.isNaN(m)) {
//	          System.out.println("0");
	          return 0;
	      } else {
//	          System.out.println(m);
	          return m;
	      }
		
	}

	public static boolean InSameKG(String current_abbr, String target_abbr, String location, String target_location) {
		
		ArrayList<String> left_variable= new ArrayList<>();
		ArrayList<String> right_variable= new ArrayList<>();
		
		for(int i=0;i<abbr_line.size();i++){
			String[] abbr_line_data = abbr_line.get(i);
			if(abbr_line_data[1].equalsIgnoreCase(current_abbr)){
//				if(abbr_line_data[1].equalsIgnoreCase(current_abbr)&& abbr_line_data[5].equalsIgnoreCase(location)){
				left_variable.add(abbr_line_data[2]+"!!");
			}
			if(abbr_line_data[1].equalsIgnoreCase(target_abbr)){
//				if(abbr_line_data[1].equalsIgnoreCase(target_abbr)&& abbr_line_data[5].equalsIgnoreCase(target_location)){
				right_variable.add(abbr_line_data[2]+"!!");
			}
			
		}
		
		for(int i=0;i<kg_line.size();i++){
			String[] kg_line_data = kg_line.get(i);
			
			for(int j=0;j<left_variable.size();j++){
				for(int k=0;k<right_variable.size();k++){
					
					for(int i_left= 0;i_left<left_variable.get(j).split("!!").length;i_left++){
						for(int i_right = 0;i_right<right_variable.get(k).split("!!").length;i_right++){
							
							if(kg_line_data[0].equals(left_variable.get(j).split("!!")[i_left]) && kg_line_data[2].equals(right_variable.get(k).split("!!")[i_right])){
								return true;
							}
						}
					}

				}
			}
		}
		return false;
	}

	public static ResultSet connectDatabase(String command_sql) throws SQLException {
		String url = "jdbc:mysql://localhost:3306/icse"; // URL
		String user = "root"; //User
		String password = "root"; //Password
		
		Connection connection = null;
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			try {
				connection = DriverManager.getConnection(url, user, password);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(!connection.isClosed())
				{
					System.out.println("Succeeded connecting to the Database!");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String sql = command_sql;
			PreparedStatement ps= null;
			try {
				ps = connection.prepareStatement(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			ResultSet rs = ps.executeQuery(sql);
			return rs;
			
	}

	public static String g(ArrayList<String[]> data,String[] abbrTypeName,String[] relationName,String[] heuName){
    	int[] relationIndex = {subclassIndex,subsubclassIndex,parentsIndex,ancestorIndex,methodsIndex,fieldsIndex,commentIndex,typeIndex,enclosingClassIndex,assignmentIndex,methodInvocatedIndex,parameterArgumentIndex,parameterIndex,enclosingMethodIndex, argumentIndex};
    	double maxm=-1;
    	String r="";
        for (int i = 0; i < abbrTypeName.length; i++) {
        	// 这是算分的
            for (int j = 0; j < relationIndex.length; j++) {
                for (int k = 0; k < heuName.length; k++) {
//                    System.out.print(abbrTypeName[i] + "," + relationName[j] + "," + heuName[k] + ",");
                    double tm=handleTypeRelationH(abbrTypeName[i], relationIndex[j], heuName[k], data);
                    
                    if(!SelectedRules.contains(abbrTypeName[i] + "," + relationName[j] + "," + heuName[k] + ",") && tm>maxm){
                    	maxm=tm;
                    	r=abbrTypeName[i] + "," + relationName[j] + "," + heuName[k] + ",";
                    	finalindex=relationIndex[j];
                    }
                }

            }
            // linsen abbr
//            System.out.print(abbrTypeName[i] + "," + relationName[relationIndex.length] + ",,");
            double tm=handleAbbr(abbrTypeName[i], "Linsen", data);
            if(!SelectedRules.contains(abbrTypeName[i] + "," + relationName[relationIndex.length]+ ",,") && tm>maxm){
            	maxm=tm;
            	r=abbrTypeName[i] + "," + relationName[relationIndex.length]+ ",,";
            }

            // computer abbr
//            System.out.print(abbrTypeName[i] + "," + relationName[relationIndex.length+1] + ",,");
            tm=handleAbbr(abbrTypeName[i], "Computer", data);
            if(!SelectedRules.contains(abbrTypeName[i] + "," + relationName[relationIndex.length+1]+ ",,") && tm>maxm){
            	maxm=tm;
            	r=abbrTypeName[i] + "," + relationName[relationIndex.length+1]+ ",,";
            }
        }
         metrics.add(new Double(maxm));
         SelectedRules.add(r);
         System.out.println(maxm+"   "+r);
         return r;
    }
    
    public static ArrayList<String[]> kgExpa(ArrayList<String[]> data,String r){
    //用知识图谱扩展	
    	ArrayList<String[]> returnData = new ArrayList<>();
    	
    	
    	return returnData;
    }
    public static ArrayList<String[]> transExpa(){
    	ArrayList<String[]> returnData = new ArrayList<>();
    	
    	 
    	return returnData;
    }
    
    public static ArrayList<String[]> expaData(ArrayList<String[]> data,String r){
    	//循环处理匹配数据
    	ArrayList<String[]> returnData = new ArrayList<>();
    	ArrayList<String[]> dData = new ArrayList<>();
    	
    	int typeRelationNum = 0;
    	String[] rules = r.split(",",-1);
    	
    	//if(1==1){    
    	if(!Util.isNum(rules[0].charAt(0)) && (!rules[0].equals("InSameKG"))){    
    	// 
   		 //这一部分是用原来的规则，扩充eData
	    	 for (int i = 0; i < data.size(); i++) {
	
	    		 String[] line = data.get(i);
	    		 
	    		 if(rules[1].equals("LinsenAbbr")){
	    			 
	    			 if (Dic.abbrDicHashMap.containsKey(line[abbrIndex].toLowerCase()))
	    			 {
	    				 returnData.add(data.get(i));
	    			 }
	    			 
	    		 }
	    		 else if(rules[1].equals("ComputerAbbr")){
	    			 if (Dic.computerAbbrDicHashMap.containsKey(line[abbrIndex].toLowerCase()))
	    			 {
	    				 returnData.add(data.get(i));
	    			 }
	    			 
	    		 }
	    		
	         	if (line[typeOfIdentifierIndex].equals(rules[0])) {
	 
	        	   switch (rules[2]) {
	               case "H1":
	                   if (line[finalindex+1].length() > 0) {
	                       returnData.add(line);
	                   }
	                   
	                   break;
	               case "H2":
	                   if (line[finalindex+2].length() > 0) {
	                       returnData.add(line);
	                   }
	                   
	                   break;
	               case "H3":
		                   if (line[finalindex+3].length() > 0) {
		                       returnData.add(line);
		                   }
		                   break;
		        	   }
		           }
		        }
    	}
    	else if(rules[0].equals("InSameKG")){
    		
    	for(int i=0;i<data.size();i++){
    		String current_abbr =data.get(i)[abbrIndex];
    		String current_expan=data.get(i)[expansionIndex];
    		String current_path = data.get(i)[idIndex];
    		
    		for(int j=0;j<eData.size();j++){
    			String target_abbr = eData.get(j)[abbrIndex];
    			String target_expan = eData.get(j)[expansionIndex];
    			String target_path = eData.get(j)[idIndex];
    			
    			if(current_abbr.equals(target_expan) && InSameKG(current_abbr, target_abbr, current_path, target_path)){
    				
    				if(!target_expan.equals("")){
    					returnData.add(data.get(i));
    				}
    				
    			}
    			
    		}
    		
    		
    	}
    	
    	}else{
       	 //用迁移规则，扩充eData
//    		int abbrLength = 0;
//    		int abbrLoca = 0;
    		int abbrLength=Integer.parseInt(r.split(",",-1)[0]);
    		int abbrLoca=Integer.parseInt(r.split(",",-1)[1]);
    		
        	for(int i = 0;i < data.size();i++){
        		String currentAbbr = data.get(i)[abbrIndex]; //当前缩写词
        		String currentPath = data.get(i)[filesIndex]; //当前文件路径
        		String currentExpa = data.get(i)[expansionIndex];//当前扩展词
        		int currentAbbrlength = currentAbbr.length();
        		if(currentAbbrlength > 2){
        			currentAbbrlength = 3;
        		}
        		if(currentAbbrlength==abbrLength){//满足长度规则
        			
        			for(int j =0;j<eData.size();j++){
        				
        				String targetAbbr = eData.get(j)[abbrIndex];//缩写词
        				String targetExpa = eData.get(j)[expansionIndex];//扩展词
        				String targetPath = eData.get(j)[filesIndex];//路径
        				
        				if(currentAbbr.equals(targetAbbr) && getLoca(currentPath,targetPath)==abbrLoca ){ //满足路径规则
        					if(!targetExpa.equals("")){
        						//可扩展数量
        						returnData.add(data.get(i));
        					}//end if
        				}// end for 路径
        			}//end for eData
        		}//end for 长度
        	
        	}//end for data
    		
    	}
    	

    	return returnData;
    }
    
    public static ArrayList<String[]> deleteData(ArrayList<String[]> data,String r){
    	//摘出来dData
    	ArrayList<String[]> returnData = expaData(data,r);
    	ArrayList<String[]> dData = new ArrayList<>();
    	
    	int typeRelationNum = 0;
    	String[] rules = r.split("!",-1);//没有用到？？
    	for(int i=0;i<data.size();i++)
    	{
    		boolean exist = false;
    		for(int j=0;j<returnData.size();j++)
    		{
    			exist = Arrays.equals(data.get(i), returnData.get(j));
    			if(exist == true){
    				eData.add(data.get(i));
    				break;//是否需要break
    			}
    		}
    		if(exist == false){
    			dData.add(data.get(i));
    		}
    	}

		return dData;
    
    }
         
    
    
    public static void f(ArrayList<String[]> data,String[] abbrTypeName,String[] relationName,String[] heuName) throws SQLException{
    	if(SelectedRules.size()>=245) return ;
    	finalindex=-1;
    	//先计算得分，找得分最高的规则
    	String mutilRule = score(data,abbrTypeName,relationName,heuName);
    	
    	
    	//删除这条规则对应的数据
    	data = deleteData(data,mutilRule);
    	
    	
    	
    	//递归处理
    	f(data,abbrTypeName,relationName,heuName);
    	
//    	String r=g(data,abbrTypeName,relationName,heuName); //计算度量值，返回规则
//    	data=deleteData(data,r);
//    	//System.out.println(eData.size()+ "     "+"******");
//    	f(data,abbrTypeName,relationName,heuName);
    }
    
    public static void generateTrainData() throws Exception {
        System.setOut(new PrintStream(new File("data/train" + 0 + "_selfExpansion.csv")));
        ArrayList<String> temp = Util.readFile("data/data" + 1 + "_selfExpansion.csv");
        System.out.println(temp.get(0));

        for (int i = 1; i <= 1; i++) {
            ArrayList<String> file = Util.readFile("data/data" + i + "_selfExpansion.csv");
            for (int k = 1; k < file.size(); k++) {
                System.out.println(file.get(k));
            }
        }
    }
    //static ArrayList<String[]> expan = new ArrayList<>();
    static int finalindex;
    private static double handleAbbr(String typeOfIdentifier, String type, ArrayList<String[]> data) {
        int typeNum = 0;
        int typeRelationNum = 0;
        int rightNum = 0;
        
        for (int i = 0; i < data.size(); i++) {
            String[] line = data.get(i);
            if (line[typeOfIdentifierIndex].equalsIgnoreCase(typeOfIdentifier)) {
                typeNum++;
                String abbr = line[abbrIndex];
                String expansion = line[expansionIndex];
                if (type.equals("Linsen")) {
                    if (Dic.abbrDicHashMap.containsKey(abbr.toLowerCase())) {
                        typeRelationNum++;
                        String dicExpansion = Dic.abbrDicHashMap.get(abbr.toLowerCase());
                        if (dicExpansion.equalsIgnoreCase(expansion)) {
                            rightNum++;
//                            System.out.println(rightNum);
//                            expan.add(line);
                        }
                    }
                } else if (type.equals("Computer")) {
                    if (Dic.computerAbbrDicHashMap.containsKey(abbr.toLowerCase())) {
                        typeRelationNum++;
                        String dicExpansion = Dic.computerAbbrDicHashMap.get(abbr.toLowerCase());
                        if (Util.equalComputerExpansion(expansion, dicExpansion)) {
                            rightNum++;
//                            System.out.println(rightNum);
//                            expan.add(line);
                        }
                    }
                }
            }
        }

        double p = 1.0*rightNum/typeRelationNum;
        double m = p * Util.sigmoid(rightNum);
//        System.out.print(typeNum + ",");
//        System.out.print(typeRelationNum + ",");
//        System.out.print(",");
//        System.out.print(rightNum + ",");
        if (Double.isNaN(p)) {
//            System.out.print("0,");
        } else {
//            System.out.print(p + ",");
        }
        if (Double.isNaN(m)) {
//            System.out.println("0");
            return 0;
        } else {
//            System.out.println(m);
            return m;
        }
    }

    private static double handleTypeRelationH(String typeOfIdentifier, int relationIndex, String H, ArrayList<String[]> data) {
        int typeNum = 0;
        int typeRelationNum = 0;
        int typeRelationHNum = 0;
        int rightNum = 0;

        ArrayList<String[]> expan = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String[] line = data.get(i);
            
            if (line[typeOfIdentifierIndex].equals(typeOfIdentifier)) {
                typeNum++;
                if (line[relationIndex].length() > 0 ) {
                    typeRelationNum++;
                } else {
                    continue;
                }

                switch (H) {
                    case "H1":
                        if (line[relationIndex+1].length() > 0) {
                            typeRelationHNum++;
                        }
                        if (Heu.H1EqualOf(line[expansionIndex], line[relationIndex+1])) {
                            rightNum++;
                          expan.add(line);  
                        }
                        break;
                    case "H2":
                        if (line[relationIndex+2].length() > 0) {
                            typeRelationHNum++;
                        }
                        if (Heu.H2H3EqualOf(line[expansionIndex], line[relationIndex+2])) {
                            rightNum++;
                            expan.add(line);
                        }
                        break;
                    case "H3":
                        if (line[relationIndex+3].length() > 0) {
                            typeRelationHNum++;
                        }
                        if (Heu.H2H3EqualOf(line[expansionIndex], line[relationIndex+3])) {
                            rightNum++;
                            expan.add(line);
                        }
                        break;
                }
            }
        }
        double p = 1.0*rightNum/typeRelationHNum;
        double m = p * Util.sigmoid(rightNum);
//        System.out.print(typeNum + ",");
//        System.out.print(typeRelationNum + ",");
//        System.out.print(typeRelationHNum + ",");
//        System.out.print(rightNum + ",");
        if (Double.isNaN(p)) {
//            System.out.print("0,");
        	return 0;
        } else {
//            System.out.print(p + ",");
        
        }
        
        if (Double.isNaN(m)) {
//            System.out.println("0");
            return 0;
        } else {
//            System.out.println(m);
            return m;
        }
    }
}
