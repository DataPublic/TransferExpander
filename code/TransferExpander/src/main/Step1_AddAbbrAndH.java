package main;

import util.Dic;
import util.GlobleVariable;
import util.Heu;
import util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;


public class  Step1_AddAbbrAndH {
    public static void main(String[] args) throws FileNotFoundException {
        for (int o = 1; o <= 1; o++) {
            GlobleVariable.parseResultFile = "C:/PHDONExia/icseCode/data/batik.csv";
            GlobleVariable.addAbbrAndHResultFile = "C:/PHDONExia/icseCode/data/batik_abbrAndHResult.csv";
            HashMap<String, ArrayList<String>> idToInfos = readParseResult(GlobleVariable.parseResultFile);
            handleParseReult(idToInfos);
        }
    }

    private static void handleParseReult(HashMap<String, ArrayList<String>> idToInfos) throws FileNotFoundException {
        System.setOut(new PrintStream(new File(GlobleVariable.addAbbrAndHResultFile)));
        System.out.println("id,files,abbr,name,typeOfIdentifier,subclass,H1,H2,H3,subsubclass,H1,H2,H3,parents,H1,H2,H3,ancestor,H1,H2,H3,methods,H1,H2,H3,fields,H1,H2,H3,comment,H1,H2,H3,type,H1,H2,H3,enclosingClass,H1,H2,H3,assignment,H1,H2,H3,methodInvocated,H1,H2,H3,parameterArgument,H1,H2,H3,parameter,H1,H2,H3,enclosingMethod,H1,H2,H3,argument,H1,H2,H3");

        for (String id :
                idToInfos.keySet()) {
            if (id.equalsIgnoreCase("Lorg/apache/batik/anim/MotionAnimation;.(Lorg/apache/batik/anim/timing/TimedElement;Lorg/apache/batik/anim/dom/AnimatableElement;I[F[FZZ[Lorg/apache/batik/anim/values/AnimatableValue;Lorg/apache/batik/anim/values/AnimatableValue;Lorg/apache/batik/anim/values/AnimatableValue;Lorg/apache/batik/anim/values/AnimatableValue;Lorg/apache/batik/ext/awt/geom/ExtendedGeneralPath;[FZZFS)V#1#unPt")) {
                System.out.println();
            }
            ArrayList<String> value = idToInfos.get(id);
            String filesOfIdentifier = value.get(0);

            String nameOfIdentifier = value.get(1);
            String typeOfIdentifier = value.get(2);
            if (nameOfIdentifier.length() == 0) {
                continue;
            }

            ArrayList<String> parts = Util.split(nameOfIdentifier);
            // comment 9
            // 3 - 16
            for (String part :
                    parts) {
                if (!Dic.isInDict(part)) {
                    String record = id + "," + filesOfIdentifier + "," + part + "," + nameOfIdentifier + "," + typeOfIdentifier + ",";

                    for (int i = 3; i <= 17; i++) {
                        if (i == 9) {
                            String comment = value.get(i);
                            record += comment + ",";
                            record += handleComment(part, comment.toLowerCase()) + ",";
                        } else if (i == 17) {
                            record += value.get(i) + ",";
                            if (value.get(i).length() == 0) {
                                record += ",,";
                            } else {
                                record += handleExpansion(part, value.get(i));
                            }

                        } else {
                            record += value.get(i) + ",";
                            // not exist possible expansions for current relation
                            if (value.get(i).length() == 0) {
                                record += ",,,";
                            } else {
                                record += handleExpansion(part, value.get(i)) + ",";
                            }
                        }
                    }
                    
                    
                    
                    
                    String[] ss = record.split(",", -1);
                    if (ss.length != 65) {
                        System.err.println(ss.length);
                    }
                    System.out.println(record);
                }
            }
        }
    }

    private static String handleExpansion(String part, String expansion) {
        String result = "";
        result += Heu.handleExpansionForH(part, expansion, "H1");
        result += ",";
        result += Heu.handleExpansionForH(part, expansion, "H2");
        result += ",";
        result += Heu.handleExpansionForH(part, expansion, "H3");
        return result;
    }

    private static String handleComment(String part, String comment) {
        String result = "";
        result += Heu.handleCommentForH(part, comment, "H1");
        result += ",";
        result += Heu.handleCommentForH(part, comment, "H2");
        result += ",";
        result += Heu.handleCommentForH(part, comment, "H3");
        return result;
    }

    private static HashMap<String, ArrayList<String>> readParseResult(String fileName) {
        HashMap<String, ArrayList<String>> idToInfos = new HashMap<>();
        ArrayList<String> lines = Util.readFile(fileName);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] temp = line.split(",", -1);
            if (temp.length != 19) {
                System.err.println("not 19 " + temp.length);
                System.err.println(line);
                continue;
            }

            if (idToInfos.containsKey(temp[0])) {
                System.err.println(temp[0]);
                System.err.println("duplicate keys");
                continue;
            }
            // id name typeOfIdentifier Assign Extend MethodDeclaration MethodInvocation Type Comment
            ArrayList<String> value = new ArrayList<>();
            for (int j = 1; j < temp.length; j++) {
                value.add(temp[j]);
            }
            idToInfos.put(temp[0], value);
        }
        return idToInfos;
    }
}
