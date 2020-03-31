import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import main.Step2_ComputePriorityAddNewRules;
import util.Dic;
import util.GlobleVariable;
import util.Heu;
import util.Util;

public class Expansion_AddSemantic {
	
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
	
	
	public static ArrayList<String> all_rules = new ArrayList<>(); //保存采样结果
	
	public static ArrayList<String[]> total_results = new ArrayList<>(); //保存所有数据结果
		
	public static ArrayList<String> total_expaed = new ArrayList<>(); //已经检测过的
	
	public static ArrayList<String> used_trans_rule = new ArrayList<>();
	
    private static String LinsenAbbrDic(String part) {
        if (Dic.abbrDicHashMap.containsKey(part)) {
            return Dic.abbrDicHashMap.get(part);
        }
        return "";
    }
    private static String ComputerAbbrDic(String part) {
        if (Dic.computerAbbrDicHashMap.containsKey(part)) {
            return Dic.computerAbbrDicHashMap.get(part);
        }
        return "";
    }
	
	public static ArrayList<String[]> read_csv(String file_path){
		
		ArrayList<String[]> lines = new ArrayList<>();
		

        ArrayList<String> file = Util.readFile(file_path);

        for (int i = 1; i < file.size(); i++) {
            String[] strs = file.get(i).split(",", -1);
            lines.add(strs);
        }
        
        return lines;

	}
	
	public static Map<String,String> read_rules(String file_path){
		//key是规则，vaule是分
		Map<String,String> rules = new HashMap();
		File file = new File(file_path);  
        BufferedReader reader = null;  
        try {  
            System.out.println("以行为单位读取文件内容，一次读一整行：");  
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;  
            int line = 1;  
            // 一次读入一行，直到读入null为文件结束  
            while ((tempString = reader.readLine()) != null) {  
                // 显示行号  
            	rules.put(tempString.split("!")[1],tempString.split(" ")[0]);
            	all_rules.add(tempString.split("!")[1]);
                System.out.println(tempString);  
                line++;  
            }  
            reader.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
		return rules;
		
	}
	
	public static void write_result(ArrayList<String[]> lines,String csv_path){
		
		try {
			System.setOut(new PrintStream(new File(csv_path)));
			
			for(int i = 0;i<lines.size();i++){
				for(int j = 0;j<lines.get(i).length;j++)
					System.out.print(lines.get(i)[j]+",");
				System.out.println();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void result_to_datas(String result,String[] lines){
		if(result.length()>0){
			total_expaed.add(lines.toString()); //记录扩展数据
			//加到结果中
			String[] temp = new String[lines.length+1];
			for(int j=0;j<lines.length;j++){
				temp[j]=lines[j];
				temp[lines.length]=result;
				total_results.add(temp);							
			}
		}
	}
		
	
	public static void kg_expa(String rule,String[] line){
		  //kg规则的扩展
		
		String[] temp = rule.split(",",-1);
		//System.out.println("&&&&&&&"+ temp.length);
		if(!temp[2].equals(""))
			temp[1] = temp[1]+"_"+temp[2];
	//	System.out.println("&&&&^^^^^&&"+ temp[1]);
        String expansion = "";
        String semanticRelation = "";
        if (temp[0].equalsIgnoreCase(line[typeOfIdentifierIndex])) {
            switch (temp[1]) {
                case "subclass_H1":
                expansion = Heu.handleExpansionForH(line[abbrIndex], line[subclassIndex], "H1");
                semanticRelation="subclass_H1";
                break;
                case "subsubclass_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[subsubclassIndex], "H1");
                    semanticRelation="subsubclass_H1";
                    break;
                case "parents_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parentsIndex], "H1");
                    semanticRelation="parents_H1";
                    break;
                case "ancestor_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[ancestorIndex], "H1");
                    semanticRelation="ancestor_H1";
                    break;
                case "methods_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[methodsIndex], "H1");
                    semanticRelation="methods_H1";
                    break;
                case "fields_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[fieldsIndex], "H1");
                    semanticRelation="fields_H1";
                    break;
                case "comment_H1":
                    expansion = Heu.handleCommentForH(line[abbrIndex], line[commentIndex], "H1");
                    semanticRelation="comment_H1";
                    break;
                case "type_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[typeIndex], "H1");
                    semanticRelation="type_H1";
                    break;
                case "enclosingClass_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[enclosingClassIndex], "H1");
                    semanticRelation="enclosingClass_H1";
                    break;
                case "assignment_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[assignmentIndex], "H1");
                    semanticRelation= "assignment_H1";
                    break;
                case "methodInvocated_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[methodInvocatedIndex], "H1");
                    semanticRelation="methodInvocated_H1";
                    break;
                case "parameterArgument_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parameterArgumentIndex], "H1");
                    semanticRelation="parameterArgument_H1";
                    break;
                case "parameter_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parameterIndex], "H1");
                    semanticRelation="parameter_H1";
                    break;
                case "enclosingMethod_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[enclosingMethodIndex], "H1");
                    semanticRelation="enclosingMethod_H1";
                    break;
                case "argument_H1":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[argumentIndex], "H1");
                    semanticRelation="argument_H1";
                    break;
                case "subclass_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[subclassIndex], "H2");
                    semanticRelation="subclass_H2";
                    break;
                case "subsubclass_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[subsubclassIndex], "H2");
                    semanticRelation="subsubclass_H2";
                    break;
                case "parents_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parentsIndex], "H2");
                    semanticRelation="parents_H2";
                    break;
                case "ancestor_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[ancestorIndex], "H2");
                    semanticRelation="ancestor_H2";
                    break;
                case "methods_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[methodsIndex], "H2");
                    semanticRelation="methods_H2";
                    break;
                case "fields_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[fieldsIndex], "H2");
                    semanticRelation="fields_H2";
                    break;
                case "comment_H2":
                    expansion = Heu.handleCommentForH(line[abbrIndex], line[commentIndex], "H2");
                    semanticRelation="comment_H2";
                    break;
                case "type_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[typeIndex], "H2");
                    semanticRelation="type_H2";
                    break;
                case "enclosingClass_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[enclosingClassIndex], "H2");
                    semanticRelation="enclosingClass_H2";
                    break;
                case "assignment_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[assignmentIndex], "H2");
                    semanticRelation="assignment_H2";
                    break;
                case "methodInvocated_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[methodInvocatedIndex], "H2");
                    semanticRelation="methodInvocated_H2";
                    break;
                case "parameterArgument_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parameterArgumentIndex], "H2");
                    semanticRelation="parameterArgument_H2";
                    break;
                case "parameter_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parameterIndex], "H2");
                    semanticRelation="parameter_H2";
                    break;
                case "enclosingMethod_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[enclosingMethodIndex], "H2");
                    semanticRelation="enclosingMethod_H2";
                    break;
                case "argument_H2":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[argumentIndex], "H2");
                    semanticRelation="argument_H2";
                    break;
                case "subclass_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[subclassIndex], "H3");
                    semanticRelation="subclass_H3";
                    break;
                case "subsubclass_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[subsubclassIndex], "H3");
                    semanticRelation="subsubclass_H3";
                    break;
                case "parents_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parentsIndex], "H3");
                    semanticRelation="parents_H3";
                    break;
                case "ancestor_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[ancestorIndex], "H3");
                    semanticRelation="ancestor_H3";
                    break;
                case "methods_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[methodsIndex], "H3");
                    semanticRelation="methods_H3";
                    break;
                case "fields_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[fieldsIndex], "H3");
                    semanticRelation="fields_H3";
                    break;
                case "comment_H3":
                    expansion = Heu.handleCommentForH(line[abbrIndex], line[commentIndex], "H3");
                    semanticRelation="comment_H3";
                    break;
                case "type_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[typeIndex], "H3");
                    semanticRelation="type_H3";
                    break;
                case "enclosingClass_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[enclosingClassIndex], "H3");
                    semanticRelation="enclosingClass_H3";
                    break;
                case "assignment_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[assignmentIndex], "H3");
                    semanticRelation="assignment_H3";
                    break;
                case "methodInvocated_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[methodInvocatedIndex], "H3");
                    semanticRelation="methodInvocated_H3";
                    break;
                case "parameterArgument_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parameterArgumentIndex], "H3");
                    semanticRelation="parameterArgument_H3";
                    break;
                case "parameter_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[parameterIndex], "H3");
                    semanticRelation="parameter_H3";
                    break;
                case "enclosingMethod_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[enclosingMethodIndex], "H3");
                    semanticRelation="enclosingMethod_H3";
                    break;
                case "argument_H3":
                    expansion = Heu.handleExpansionForH(line[abbrIndex], line[argumentIndex], "H3");
                    semanticRelation="argument_H3";
                    break;
                case "LinsenAbbr":
                    expansion = LinsenAbbrDic(line[abbrIndex]);
                    semanticRelation="LinsenAbbr";
                    break;
                case "ComputerAbbr":
                    expansion = ComputerAbbrDic(line[abbrIndex]);
                    semanticRelation="ComputerAbbr";
                    break;
            }
        }

        if (expansion.length() > 0) {
            HashMap<String, Integer> hashMap = new HashMap<>();
            String[] strs = expansion.split(";");
            for (int j = 0; j < strs.length; j++) {
                String[] strss = strs[j].split("#");
                for (int k = 0; k < strss.length; k++) {
                    if (strss[k].length() > 0) {
                        if (hashMap.containsKey(strss[k])) {
                            int curr = hashMap.get(strss[k]);
                            hashMap.put(strss[k], curr + 1);
                        } else {
                            hashMap.put(strss[k], 1);
                        }
                    }
                }
            }
            int max = 0;
            ArrayList<String> arrayList = new ArrayList<>();
            for (String key :
                    hashMap.keySet()) {
                if (hashMap.get(key) > max) {
                    max = hashMap.get(key);
                    arrayList = new ArrayList<>();
                    arrayList.add(key);
                } else if (hashMap.get(key) == max) {
                    arrayList.add(key);
                }
            }
            String result = "";
            for (int j = 0; j < arrayList.size(); j++) {
                if (arrayList.get(j).length() > result.length()) {
                    result = arrayList.get(j);
                }
            }
            result = result +","+ semanticRelation;
            result_to_datas(result,line);
        }
        
        
//        return "";//
    }
   
	public static void kg_expa_total_datas(String rule,ArrayList<String[]> total_datas){
		//用kg处理所有数据
		for(int i=0;i<total_datas.size();i++){
			String[] lines = total_datas.get(i);
			String result="";
			
			if(!total_expaed.contains(lines.toString())){
				kg_expa(rule, lines);
			}

		}//end for
	}
	
	//增加语义关系迁移
	public static void semantic_expan(String[] line){
		String current_abbr = line[abbrIndex];
		String location = line[idIndex];
		Map <String,String> result_num_semantic= new HashMap();//记录结果，
		
		for(int i =0;i<total_results.size();i++){
			
			String target_abbr = total_results.get(i)[abbrIndex];//缩写词
			String target_location = total_results.get(i)[idIndex];
			String target_expan = total_results.get(i)[expansionIndex].split(",")[0];//扩展词
			
			
			if(current_abbr.equalsIgnoreCase(target_abbr) && Step2_ComputePriorityAddNewRules.InSameKG(current_abbr, target_abbr,location,target_location) ){ //满足路径规则
				//记录并统计
				if(!result_num_semantic.containsKey(target_expan))
					result_num_semantic.put(target_expan, ""+0);
				
				result_num_semantic.put(target_expan, ""+String.valueOf(1+Integer.parseInt(result_num_semantic.get(target_expan))));
			}// end for 路径
		}//end for eData
		
		if(result_num_semantic.size()<1)
			return ;
		
		int num_flag = 0;
		String result_expa = "";
		for(String expa:result_num_semantic.keySet()){
			if(Integer.parseInt(result_num_semantic.get(expa))>num_flag){
				num_flag = Integer.parseInt(result_num_semantic.get(expa));
				result_expa = expa+","+"InSameKG";
			}
			
		}
		
		result_to_datas(result_expa,line);	
		
		
		
	}
    
	public static void semantic_expan_total_datas(ArrayList<String[]> total_datas){
//		used_trans_rule.add("InSameKG");//如果是迁移规则，先加进去
		for(int i=0;i<total_datas.size();i++){
			String[] lines = total_datas.get(i);
			String result="";
			
			if(!total_expaed.contains(lines.toString())){
				semantic_expan(lines);
			}

		}//end for
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
    	String pathPackA = pathFileA.substring(0,PackIndexA+1);
    	
    	int PackIndexB = pathFileB.lastIndexOf("/");
    	String pathPackB = pathFileB.substring(0,PackIndexB+1);
    	
    	if((pathPackA.equals(pathPackB)))
    	{
    		return 1;
    	}
    	
    	return 2;
    }
    
	public static void trans_expa(String rule,String[] line){
		//迁移规则的扩展
		int abbrLength = Integer.parseInt(rule.split(",",-1)[0]);
		int abbrLoca = Integer.parseInt(rule.split(",",-1)[1]);
		

		String currentAbbr =line[abbrIndex]; //当前缩写词
		String currentPath = line[filesIndex]; //当前文件路径
		int currentAbbrlength = currentAbbr.length();
		
		Map <String,String> result_num = new HashMap();//记录结果，
		
		if(currentAbbrlength > 2){
			currentAbbrlength = 3;
		}
		if(currentAbbrlength==abbrLength){//满足长度规则
			
			for(int j =0;j<total_results.size();j++){
				//遍历有结果的部分
				
				String targetAbbr = total_results.get(j)[abbrIndex];//缩写词
				String targetExpa = total_results.get(j)[expansionIndex].split(",")[0];//扩展词
				String targetPath = total_results.get(j)[filesIndex];//路径
				
				if(currentAbbr.equalsIgnoreCase(targetAbbr) && getLoca(currentPath,targetPath)==abbrLoca ){ //满足路径规则
					//记录并统计
					if(!result_num.containsKey(targetExpa))
						result_num.put(targetExpa, ""+0);
					
					result_num.put(targetExpa, ""+String.valueOf(1+Integer.parseInt(result_num.get(targetExpa))));
				}// end for 路径
			}//end for eData
		}//end for 长度
    	
		if(result_num.size()<1)
			return ;
		
		int num_flag = 0;
		String result_expa = "";
		for(String expa:result_num.keySet()){
			if(Integer.parseInt(result_num.get(expa))>num_flag){
				num_flag = Integer.parseInt(result_num.get(expa));
				result_expa = expa+","+rule;
			}
			
		}
		
		result_to_datas(result_expa,line);		
//		return result_expa;
	}
	
	
	public static void trans_expa_total_datas(String rule,ArrayList<String[]> total_datas){
//		used_trans_rule.add(rule);//如果是迁移规则，先加进去
		for(int i=0;i<total_datas.size();i++){
			String[] lines = total_datas.get(i);
			String result="";
			
			if(!total_expaed.contains(lines.toString())){
				trans_expa(rule, lines);
			}

		}//end for
	}
	
	
	
	public static void trans_expa_again(ArrayList<String[]> total_datas){
		for(int j = 0 ;j<used_trans_rule.size();j++){
			
			String used_rule = used_trans_rule.get(j);
			
			if(!used_rule.equals("InSameKG")){
				trans_expa_total_datas(used_rule,total_datas);
			}else{
				semantic_expan_total_datas(total_datas);
			}
		}//end for used rule
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//读取规则文件内容
		Map<String,String> rules = read_rules("C:/PHDONExia/ICSE2019/code/SemanticExpand/src/rule6.txt");
		
		//读取数据文件内容
		ArrayList<String[]> total_datas = read_csv("C:/PHDONExia/ICSE2019/ExpData/originalData/data6_abbrAndHResult.csv");
		
	   //遍历规则，对总数据进行扩展
	
		for(int rules_num=0;rules_num< all_rules.size();rules_num++){
			//遍历每一条规则
			
			String rule = all_rules.get(rules_num);
			System.out.println("------------"+rule);
			
			if(!Util.isNum(rule.split(",")[0].charAt(0))&& (!rule.split(",")[0].equals("InSameKG"))){
				//判断是不是kg.是的话，怎么处理
				
				//先用kg处理
				kg_expa_total_datas(rule,total_datas);
			
				trans_expa_again(total_datas);
				//循环again
			}else{
				//不是kg规则，怎么处理
				used_trans_rule.add(rule);
				
				if(rule.equals("InSameKG")){
					//如果是语义规则
					semantic_expan_total_datas(total_datas);

				}else{
					//如果是迁移，怎么处理
					trans_expa_total_datas(rule,total_datas);
					}
				
				
				
				//如果不是迁移，怎么处理
				
				
			}
		}
		
		
		//没有的补上
		for(int i = 0;i<total_datas.size();i++){
			if(!total_expaed.contains(total_datas.get(i).toString())){
				 String[] temp = new String[total_datas.get(i).length+1];
				 for(int j = 0;j <total_datas.get(i).length;j++)
					 temp[j] = total_datas.get(i)[j];
				 temp[total_datas.get(i).length] = "***";
				 total_results.add(temp); 
//				System.out.println(datas.get(i).toString());
			}
		}
		
		//修改顺序
		ArrayList<String[]> sorted_results = new ArrayList<>(); //保存采样结果
		
		for(int i = 0;i < total_datas.size();i++){
			for(int j = 0;j<total_results.size();j++){
				if((total_results.get(j)[idIndex].equals(total_datas.get(i)[idIndex]))&&(total_results.get(j)[abbrIndex].equals(total_datas.get(i)[abbrIndex]))){
					sorted_results.add(total_results.get(j));
					break;
				}
					
			}
			
		}
		
		//保存结果
		write_result(sorted_results,"C:/PHDONExia/ICSE2019/ExpData/NewResult/data6_rule6.csv");
		
		
	}

}
