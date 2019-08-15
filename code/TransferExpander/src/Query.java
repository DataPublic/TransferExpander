import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Query {
	
	public static String[] keywords= {"JSON","YAML","CLI","AST","REGEXP","URI","EOL","PKG","PDF","CTOR","CSV","SQL","CSS","UI","REGEX","IP","EOF","API","JAXB"};
	public static String[] targets= {"abbreviation","shorted to","acronym for","know as"," or <b>"};
	public static ArrayList<String> results=new ArrayList<String>();
	public static ArrayList<String> fr=new ArrayList<String>();
	public static HashSet<String> ts=new HashSet<String>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for(String keyword : keywords) {
			//download(keyword.toLowerCase());
			System.out.println("-------------"+keyword);
			results.clear();
			fr.clear();
			ts.clear();
			extract(keyword);

			if(results.size()>1) {
				//System.out.println("=====");
				choose(keyword);
				if(fr.size()==0)
					//for(int i=0;i<results.size();i++) System.out.println(results.get(i));
					System.out.println(results.get(0));
				else
					//for(int i=0;i<fr.size();i++) System.out.println(fr.get(i));
					System.out.println(fr.get(0));
			}
			else
				System.out.println(results.get(0));
		}

	}
	
	public static void download(String keyword) {
		try {
			
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)); 

			Class.forName("com.mysql.jdbc.Driver");
	        Connection conn = DriverManager.getConnection(
	                "jdbc:mysql://127.0.0.1:3306/tianchi?serverTimezone=GMT&characterEncoding=utf8",
	                "root","root");
        	
	        PreparedStatement ps=conn.prepareStatement("insert into wiki_online values(?,?)");
			
            URL url = new URL("https://en.wikipedia.org/wiki/"+keyword);
            URLConnection URLconnection = url.openConnection(proxy);
            
            //URLconnection.setRequestProperty("Proxy-Authorization", "Basic " + "jjhss19930713");
            HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {  
                InputStream in = httpConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader bufr = new BufferedReader(isr);
                String str,text="";
                while ((str = bufr.readLine()) != null) {
                    //System.out.println(str);
                	text+=str;
                }
                ps.setString(1, keyword);
                ps.setString(2,	text);
                ps.execute();
                bufr.close();
                System.err.println(keyword+"  Done!!!"); 
            } else {
                //System.err.println(keyword+"  Fail!!!!!!!!!!!!!!!!!");
            }
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void extract(String keyword) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        Connection conn = DriverManager.getConnection(
	                "jdbc:mysql://127.0.0.1:3306/tianchi?serverTimezone=GMT&characterEncoding=utf8",
	                "root","123456");
        	
	        PreparedStatement ps=conn.prepareStatement("select * from wiki_online where keyword=?");
	        ps.setString(1, keyword);
	        
	        ResultSet rs=ps.executeQuery();
	        while(rs.next()) {
	        	String html=rs.getString(2);
	        	parse(keyword,html);
	        }
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void parse(String keyword,String html) {
		//System.out.println(html);
		
		//rule1
		int pos=html.indexOf("id=\"Computing\"");
		if(pos>=0) {
			html=html.substring(pos, html.length());
			pos=html.indexOf("</ul>");
			html=html.substring(0, pos);
			while((pos=html.indexOf("<li>"))>=0) {
				html=html.substring(pos, html.length());
				String t="";
				pos=html.indexOf("</li>");
				for(int i=4;i<pos;i++) t+=html.charAt(i);
				//System.out.println(removeLink(t));
				html=html.substring(pos+4, html.length());
				if(ts.contains(removeLink(t))) continue ;
				results.add(removeLink(t));
				ts.add(removeLink(t));
			}
			return ;
		}
		
		//rule2
		pos=html.indexOf("<p>");
		String subhtml=html.substring(pos,html.length());
		pos=html.indexOf("</p>");
		subhtml=subhtml.substring(0,pos);
		//System.out.println(subhtml);
		for(String target : targets) {
			pos=subhtml.indexOf(target);
			if(pos==-1) continue;
			String tsubhtml=subhtml.substring(0, pos);
			pos=tsubhtml.indexOf("<b>");
			if(pos>=0) {
				String t="";
				for(int i=pos+3;i<tsubhtml.length();i++) {
					char c=tsubhtml.charAt(i);
					if(c=='<') break;
					t+=c;
				}
				if(normal(t).equals(normal(keyword))) continue;
				//System.out.println(t);
				if(ts.contains(t)) return ;
				results.add(t);
				ts.add(t);
				return ;
			}
		}
		
		//rule 3
		pos=subhtml.indexOf("(");
		if(pos>=0) {
			String ahtml=subhtml.substring(0, pos);
			String bhtml=subhtml.substring(pos, subhtml.length());
			String a=getFirstB(ahtml);
			String b=getFirstB(bhtml);
			String res="";
			if(a.equals("")) res=b;
			else if(b.equals("")) res=a;
			else if(normal(a).equals(normal(keyword))) res=b;
			else res=a;
			//System.out.println(a+"  "+b+"  "+res);
			if(!res.equals("")&&!normal(res).equals(normal(keyword))) {
				//System.out.println(res);
				if(ts.contains(res)) return ;
				results.add(res);
				ts.add(res);
				return ;
			}
		}
		
		//rule 4
		pos=subhtml.indexOf("</b> (\"");
		if(pos>=0) {
			String t="";
			for(int i=pos+7;i<subhtml.length();i++) {
				char c=subhtml.charAt(i);
				if(c=='\"') break;
				t+=c;
			}
			//System.out.println(t);
			if(ts.contains(t)) return ;
			results.add(t);
			ts.add(t);
			return ;
		}
		
		
		//rule 5
		pos=html.indexOf("<p>");
		subhtml=html.substring(pos,html.length());
		pos=subhtml.indexOf("</ul>");
		if(pos>=0) {
			subhtml=subhtml.substring(0, pos);
			while((pos=subhtml.indexOf("<li>"))>=0) {
				subhtml=subhtml.substring(pos, subhtml.length());
				String t="";
				pos=subhtml.indexOf("</li>");
				for(int i=4;i<pos;i++) t+=subhtml.charAt(i);
				//System.out.println(removeLink(t));
				subhtml=subhtml.substring(pos+4, subhtml.length());
				if(ts.contains(removeLink(t))) continue;
				results.add(removeLink(t));
				ts.add(removeLink(t));
			}
		}
		
		//System.out.println("Done");
	}
	
	public static String removeLink(String text) {
		String t="";
		boolean flag=true;
		for(int i=0;i<text.length();i++) {
			char c=text.charAt(i);
			if(c=='<') {
				flag=false;
				while((c=text.charAt(i))!='>') {
					if(c=='/') flag=true;
					i++;
				}
			}
			else if(flag) {
				if(c==','||c==';') break;
				t+=c;
			}
		}
		return t;
	}
	
	public static String normal(String text) {
		text=text.toLowerCase();
		String res="";
		for(int i=0;i<text.length();i++) if(Character.isLetter(text.charAt(i))) {
			res+=text.charAt(i);
		}
		return res;
	}

	public static String normal2(String text) {
		String t="";
		boolean flag=true;
		for(int i=0;i<text.length();i++) {
			char c=text.charAt(i);
			if(c=='<') {
				flag=false;
				while((c=text.charAt(i))!='>') {
					if(c=='/') flag=true;
					i++;
				}
			}
			else if(flag)
				t+=c;
		}
		return t.toLowerCase();
	} 
	
	public static String getFirstB(String text) {
		String res="";
		int pos=text.indexOf("<b>");
		if(pos==-1) return "";
		for(int i=pos+3;i<text.length();i++) {
			char c=text.charAt(i);
			if(c=='<') break;
			res+=c;
		}
		return res;
	}

	public static String choose(String keyword) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        Connection conn = DriverManager.getConnection(
	                "jdbc:mysql://127.0.0.1:3306/tianchi?serverTimezone=GMT&characterEncoding=utf8",
	                "root","123456");
	        PreparedStatement ps=conn.prepareStatement("select * from wiki_online where keyword=?");
	        for(int i=0;i<results.size();i++) {
	        	String html="";
	        	ps.setString(1, results.get(i));
	        	ResultSet rs=ps.executeQuery();
	        	boolean flag=false;
	        	while(rs.next()) {
	        		html=rs.getString(2);
	        		flag=true;
	        	}
	        	if(!flag) {
	        		download(results.get(i));
		        	rs=ps.executeQuery();
		        	while(rs.next()) {
		        		html=rs.getString(2);
		        	}
	        	}
	        	//if(!results.get(i).equals("Empirical orthogonal functions")) continue; 
	        	if(isCom(html,keyword)) fr.add(results.get(i));//System.out.println(results.get(i));
	        	//System.out.println(results.get(i));
	        	
	        }
	        ps.close();
	        conn.close();

		}catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static boolean isCom(String html,String keyword) {
		
		int pos=html.indexOf("<p>");
		if(pos==-1) return false;
		String subhtml=html.substring(pos,html.length());
		pos=subhtml.indexOf("</p>");
		subhtml=subhtml.substring(0,pos).toLowerCase();
		subhtml=normal2(subhtml);
		if(subhtml.indexOf("computing")>=0||subhtml.indexOf("computer")>=0) {
			//System.out.println(subhtml);
			pos=html.indexOf("<title>");
			if(Character.toLowerCase(html.charAt(pos+7))==Character.toLowerCase(keyword.charAt(0)))
				return true;
			return false;
		}
		return false;
	}
}
