package elod.harvest.bugets.thessaloniki;
/*
 java -jar /home/ilias/git/elod-salonikabudget/ThessBudget.jar
/home/ilias/skaros/Dropbox/ThessHarvestMsng /mnt/4C1EAB314334DA2B/Cloud/Dropbox/SettingFiles/Authantication/EmailAuthentication.txt /home/ilias/skaros/MessengerDB.txt /home/ilias/Downloads/lib
 */

/*
"<table style=\"width:100%\"><tr><td>Jill</td><td>Smith</td><td>50</td></tr><tr><td>Eve</td><td>Jackson</td><td>94</td></tr></table> "
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;
import com.ui4j.api.dom.Document;
import com.ui4j.api.dom.Element;
import com.ui4j.api.dom.Select;

import elod.tool.msg.Notifications;
import org.hyperic.sigar.SigarException;


public class ScrapingThessalonikiBudget {
	static File newDir;
	//the sleep time in case of exception
	static int timeout=10000;
	static String rootDir ="/home/ilias/skaros/Dropbox/";
	//the dir where log is saved
	static String logDir=rootDir+"logs/ThessCity/";
	
	//the dir where the harvested files are saved
	static String harvestDir=rootDir+"ThessBudget/";
	//the log file
	static final String logFile="thess.log";
	static String SPLIT_CHAR=";";
	
	static final long minExpSize=150*1024;
	static final long minIncSize=30*1024;
static final String Budget_Url="http://www.thessaloniki.gr/egov/budget.html";
//"https://gaiacrmkea.c-gaia.gr/city_thessaloniki/index.php";

	static final int downloadTTL=5;
	
	//static PrintWriter messenger = null;
	  static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	  
	  
	  
	/*
	 * EMAIL SETTINGS
	 */
	 private static String USER_NAME = "harvest.notifier";  // GMail user name (just the part before "@gmail.com")
	    private static String PASSWORD = "HarvestNotifier"; // GMail password
	    private static String[] RECIPIENT ;//= {"skaros.ilias@gmail.com","skarosi@hotmail.com"};

	    static Notifications notif;
	    
	    
	/**
	 * The function will create a new Document based on the page provided.
	 * with that page (the thessaloniki Budget) will change the combobox to match the supplied parameters
	 * and save the outcome to a file with the filename provided.
	 * 
	 * @param typeIndex the type of data we want to get, 0:income, 1:expenses
	 * @param fileName the string representing the name to be used as filename
	 * @param page the webpage of the city budget council 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	
	public static void getData(int typeIndex, String fileName,Page page) {
	     // get the DOM
        Document document = page.getDocument().query("frame").getDocument();//.getContentDocument();
         // find the year combobox
System.out.println(document.getBody());
        Select year = document
                        .query("#fyear")
                        .getSelect();
        System.out.println("1b");
try{
	System.out.println("YEAR"+year.getLength());
}catch(Exception e){System.out.println("Exception1");
}System.out.println("1c");
//try catch NullPointerException
        // select last year on list
        year.setSelectedIndex(year.getLength()-1);
        // trigger the change event
        year.change();

        // Small delay before the page load
        try {
			Thread.sleep(timeout);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        //select type tipos
        Select type= document
        		.query("#esex")
        		.getSelect();
   try{     System.out.println("type"+type.getLength());
	}catch(Exception e){System.out.println("Exception1");
	}
       type.setSelectedIndex(typeIndex);
       type.change();
       // Small delay before the page load
       try {
		Thread.sleep(timeout);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
      //select diefthinsh all
      Select dep =document
        	.query("#cdief")
        	.getSelect();
      dep.setSelectedIndex(0);
      dep.change();
      // Small delay before the page load
      try {
		Thread.sleep(timeout);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

      // find the data table
      Element table=null;
      try{ 
      table = document
             .query("#example")
             .query("tbody");
      }
      catch(Exception e){
    	  System.out.println("Exception:"+e.getLocalizedMessage()+"\n"+e.toString());
    	  e.printStackTrace();
      }


      // iterate all rows
      List<Element> rows=null;
      try{
      rows= table.queryAll("tr");
      }
      catch(Exception e){
    	  System.out.println("Exception:"+e.getLocalizedMessage()+"\n"+e.toString());
    	  e.printStackTrace();
      }
      PrintWriter writer = null;
	try {
		writer = new PrintWriter(fileName, "UTF-16");
	} catch (FileNotFoundException e){
		notif.addMessage("Can not find "+fileName, "5", false);
	}catch( UnsupportedEncodingException e) {
		notif.addMessage("Unsupported Encoding on UTF-16", "5",false);
	}
//    write the column titles first
      if(typeIndex==1){
//    	  this is the header for the expenses table
    	  writer.println("Υπηρεσία;Κ.Α.;Περιγραφή;Προϋπολογισθέντα;Διαμορφωθέντα;Δεσμευθέντα;Ενταλθέντα;Πληρωθέντα");
      }else
//    	  this is the header for the income table
    	  writer.println("Υπηρεσία;Κ.Α.;Περιγραφή;Προϋπολογισθέντα;Διαμορφωθέντα;Βεβαιωθέντα;Εισπραχθέντα");
        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            // iterate all columns
            List<Element> cells = row.queryAll("td");
          
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < cells.size(); j++) {
                Element cell = cells.get(j);
                String text = cell.getText();
//                if(!text.equals("")){
                	//if it is the first column, no comma will be placed
                	builder.append(text+SPLIT_CHAR);
                	

//                }
            }    
//            remove the last ";"
            builder.substring(0,builder.length()-1); 

//            then the harvested data
            writer.println(builder.toString());
        } 
        writer.close();
        page.close();
	}//getData
	
	/**
	 * Connects to the Thessaloniki Budget site and gets the web page form for the current year and pass it to the getData() method
	 * @throws Exception
	 */
	public static void readCityBudget() throws Exception{
		String subject = "Thessaloniki Budget Error";
        StringBuilder errorMessage = new StringBuilder();

        boolean sentMail=false;

        
		BrowserEngine webkit = BrowserFactory.getWebKit();

        // load the page
        Page page = webkit.navigate(Budget_Url);
        
        String fileName = new SimpleDateFormat("yyyy_MM_dd_HH.mm").format(new Date());
        String workingDir = System.getProperty("user.dir");
//        where the downloaded files will be saved
        File expesnsesDir = new File(harvestDir+"expenses/"+new SimpleDateFormat("yyyy").format(new Date())+"/"+new SimpleDateFormat("MM").format(new Date())+new SimpleDateFormat("MMMM").format(new Date()));
        File incomeDir = new File(harvestDir+"income/"+new SimpleDateFormat("yyyy").format(new Date())+"/"+new SimpleDateFormat("MM").format(new Date())+new SimpleDateFormat("MMMM").format(new Date()));

        if(!expesnsesDir.exists()){
        	//dir doesnt exist and should be created
        	if(!expesnsesDir.mkdirs()){
            	//There is a problem with creating the directory
            	//Default dir will be used.
        		expesnsesDir=new File(workingDir);
            	System.out.println("Error while Creating Expenses Directory. Default Directory will be used: "+expesnsesDir.toString());
//            	notif.addError("Error while Creating Directory. Default Directory will be used: "+expesnsesDir.toString());
            	notif.addMessage("Error while Creating Expenses Directory. Default Directory will be used: "+expesnsesDir.toString(), "3", false);
            	FileWriter writer = new FileWriter(logDir+logFile,true); //the true will append the new data
  				Date now=new Date();
  				errorMessage.append(now+" "+"Error while Creating Directory. Default Directory will be used: "+expesnsesDir.toString()+"\n");
  				writer.append(now+" "+"Error while Creating Directory. Default Directory will be used: "+expesnsesDir.toString()+"\n");
//  				messenger.write("Error,"+dateFormat.format(new Date())+",Error while Creating Directory. Default Directory will be used: "+expesnsesDir.toString()+"\n");
//  				messenger.flush();
  				
  				//set the sentMail flag to true, now an email will be sent
  				sentMail=true;
  				writer.flush();
  				writer.close();
            }
        }

        //and the same for the income files
        if(!incomeDir.exists()){
        	//dir doesnt exist and should be created
        	if(!incomeDir.mkdirs()){
        		//There is a problem with creating the directory
        		//Default dir will be used.
        		incomeDir=new File(workingDir);
        		System.out.println("Error while Creating Income Directory. Default Directory will be used: "+incomeDir.toString());
        		notif.addMessage("Error while Creating Income Directory. Default Directory will be used: "+incomeDir.toString(), "3", false);
        		FileWriter writer = new FileWriter(logDir+logFile,true); //the true will append the new data
        		Date now=new Date();
        		errorMessage.append(now+" "+"Error while Creating Directory. Default Directory will be used: "+incomeDir.toString()+"\n");
        		writer.append(now+" "+"Error while Creating Directory. Default Directory will be used: "+incomeDir.toString()+"\n");
        		//set the sentMail flag to true, now an email will be sent
  				sentMail=true;
        		writer.flush();
        		writer.close();
        	}
        }   	    	 
        boolean incPass=false,expPass=false;

        //Get the data for the expenses for the latest year  
        try{
        	int count=0;
        	Thread.sleep(5000);
        	do{
        		getData(1,expesnsesDir+"/"+fileName+".csv",page);
        	
	        	if(count>=downloadTTL){
	        		//stop retry
	        		System.out.println("STOP RETRY EXPENSES:"+expesnsesDir+"/"+fileName+".csv");
	        		expPass=false;
	        		break;
	        	}else
	        	{System.out.println("RETRY EXPENSES:"+expesnsesDir+"/"+fileName+".csv");
	        		//retry
	        		count++;
	        	}
        	}while(!validateFile(expesnsesDir+"/"+fileName+".csv",minExpSize));
        	
        	if(validateFile(expesnsesDir+"/"+fileName+".csv",minExpSize)){
        		expPass=true;
        	}else{
        		//file verification failed
        		//retry
        		
        	}
        	System.out.println("File with expenses saved at"+expesnsesDir+"/"+fileName+".csv");
        	notif.addMessage("File with expenses saved at"+expesnsesDir+"/"+fileName+".csv","1",false);
        	notif.runInsert();
        }catch(Exception e){
        	FileWriter writer = new FileWriter(logDir+logFile,true); //the true will append the new data
        	Date now=new Date();
        	errorMessage.append(now+" "+"Error while Creating Directory. Default Directory will be used: "+incomeDir.toString()+"\n");
			writer.append(now+" "+"Error while creating the expenses file. "+e.getMessage()+"\n");
			notif.addMessage("Error while creating the expenses file. "+e.getMessage(),"3",false);
			notif.runInsert();
			//set the sentMail flag to true, now an email will be sent
				sentMail=true;
			writer.flush();
			writer.close();
        }
        notif.runInsert();
        //Get the data for the income for the latest year
        try{
        	Thread.sleep(5000);
        	int count=0;
        	do{
        		getData(0,incomeDir+"/"+fileName+".csv",page);
	        	if(count>=downloadTTL){
	        		//stop retry
	        		System.out.println("STOP RETRY INCOME:"+incomeDir+"/"+fileName+".csv");
	        		incPass=false;
	        		break;
	        	}else
	        	{System.out.println("RETRY INCOME:"+incomeDir+"/"+fileName+".csv");
	        		//retry
	        		count++;
	        	}
        	}while(!validateFile(incomeDir+"/"+fileName+".csv",minIncSize));
        	
        	
        	
        	if(validateFile(incomeDir+"/"+fileName+".csv",minIncSize)){
        		incPass=true;
        	}
        	System.out.println("File with income saved at"+incomeDir+"/"+fileName+".csv");
//        	messenger.write("Success,"+dateFormat.format(new Date())+",File with income saved at"+incomeDir+"/"+fileName+".csv\n");
//        	messenger.flush();
//        	notif.addSuccess("File with income saved at"+incomeDir+"/"+fileName+".csv");
        	notif.addMessage("File with income saved at"+incomeDir+"/"+fileName+".csv","1",false);
        	notif.runInsert();
        }catch(Exception e){
			FileWriter writer = new FileWriter(logDir+logFile,true); //the true will append the new data
			Date now=new Date();
			errorMessage.append(now+" "+"Error while creating the income file. "+e.getMessage()+"\n"+e.getLocalizedMessage()+"\n"+e.getStackTrace());
			writer.append(now+" "+"Error while creating the income file. "+e.getMessage()+"\n");
			notif.addMessage("Error while creating the income file. "+e.getMessage(),"5",false);
			notif.runInsert();
//			messenger.write("Error,"+dateFormat.format(new Date())+",Error while creating the income file. "+e.getMessage()+"\n");
//			messenger.flush();
			//set the sentMail flag to true, now an email will be sent
				sentMail=true;
			writer.flush();
			writer.close();
		}
       notif.finishExecution();
       
        if (incPass&&expPass){
        	//both files OK        	
        	notif.addMessage("SUCCESS, ThessBudget files downloaded succesfull", "1", false);
        	System.out.println("\nSUCCESS, ThessBudget files downloaded succesfull\n");
        }
        else{
        	//one of the files not ok        	
        	if(incPass){
        		//the income file is OK
        		if(!expPass){
        			//expenses failed
        		    notif.addMessage("ERROR, ThessBudget Harvest, income file downloaded properly, BUT expenses FAILED", "5", false);
        		    System.out.println("\nERROR, ThessBudget Harvest, income file downloaded properly, BUT expenses FAILED\n");
        		    }      	
        	}else{
        		//income failed
        		if(!expPass){
        			//both failed
        			notif.addMessage("ERROR, ThessBudget Harvest, income and expenses files FAILURE", "5", false);
        			 System.out.println("\nERROR, ThessBudget Harvest, income and expenses files FAILURE\n");
        		}
        	}	
        }
      
             
        
       try{ 
    	   if(sentMail){
        
            sendFromGMail(USER_NAME, PASSWORD, RECIPIENT, subject, errorMessage.toString());
    	   }
        }catch(Exception e){
        	FileWriter writer = new FileWriter(logDir+logFile,true); //the true will append the new data
			Date now=new Date();
        	writer.append(now+" "+"Error while sending email. "+e.getMessage()+"\n");
//        	messenger.write("Error,"+dateFormat.format(new Date())+",Error while sending email. "+e.getMessage()+"\n");
        	
        	writer.flush();
			writer.close();
        }
	}
 
	public static boolean validateFile(String file,long min){
		System.out.println("SIZE"+new File(file).length()+"\nmin"+min);
		if(new File(file).length()<min){
			return false;
		}else{
			return true;
		}
	}
	
	public static void main(String[] args) throws Exception {   
		//create a new messaging object
		 notif=new Notifications("13","12", "Skaros","Thessaloniki Budget Harvester","SkarosIlias");
	 
		if(args.length<3){
			System.out.println("Usage java -jar ThessBudget.jar [root/dir/to/save/files] [email setting file path] [messenger DB settings] [Sigar lib folder]");
			System.exit(1);
		}
		//set up the Sigar library directory
		System.setProperty("org.hyperic.sigar.path",args[3]);
		
		if(!new File(args[2]).exists()){
			System.out.println("messenger settings file doesnt exist");
			
		}else{
			//set up the messaging settings
			notif.DbCredentials(new File(args[2]));
			
		}
		
		
		rootDir=args[0];
//		if the directory provided has no / add it to the end
		if (rootDir.charAt(rootDir.length()-1)!='/'){
			rootDir+="/";
		}
		logDir=rootDir+"logs/ThessCity/";
		harvestDir=rootDir+"ThessBudget/";
		
		
		//read the email credential file
		File EmailCreadentialFile=new File(args[1]);
		
		//the default recipient
		RECIPIENT=new String[]{"skaros.ilias@gmail.com"};
		
		if(EmailCreadentialFile.isFile()){
			BufferedReader fr;
			fr = new BufferedReader(new FileReader(EmailCreadentialFile));
			String input;
			String[] credentials;
			while ((input=fr.readLine())!=null){
				System.out.println(input);
				credentials=input.split(":");
				if (credentials[0].contains("USER_NAME")){				  
					USER_NAME=credentials[1].trim();
				}else
					if (credentials[0].contains("PASSWORD")){				  
						PASSWORD=credentials[1].trim();
					}
					else if(credentials[0].contains("RECIPIENT")){
						//there is a list of recipients
						if(credentials[1].length()>5){
							//this looks like a valid email address
								RECIPIENT=credentials[1].trim().split(" ");
						}
							
					} 
			}
			fr.close();
		}
		else{
			//email settings file doesn't exist. Terminating
			//if logdir was supplied and is writable
			if (new File(logDir).canWrite()){
				FileWriter writer = new FileWriter(logDir+logFile+new SimpleDateFormat("yyyy_MM").format(new Date())+".log",true); //the true will append the new data
  				Date now=new Date();
  				writer.append(now+" "+"Error while reading email settings. No file was found. Terminating.");		
			}
			System.exit(1);
		}
		
		
	
		System.setProperty("ui4j.headless", "true");
		try{
			File logDirectory = new File (logDir);
			 //create log directory
			if (!logDirectory.exists()){
				logDirectory.mkdirs();
			}
			
			logDirectory = new File (harvestDir);
			//create harvest directory
			if (!logDirectory.exists()){
			logDirectory.mkdirs();
			
			}
			//get the system resources
			notif.addMachineStat("83.212.86.161","Harvester");
			readCityBudget();
			notif.addMachineStat("83.212.86.161","Harvester");
			//push all messages to the database
			notif.runInsert();
			notif.finishExecution();
		}
		catch(Exception e){
			System.out.println("Exception "+e.getMessage());
		}
	notif.runInsert();
	notif.finishExecution();
//		messenger.close();
		System.exit(0);
	
	}

	
	
	
	private static void sendFromGMail(String from, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        Session session =Session.getInstance(props); //Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
            	
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
            }
           
//            for( int i = 0; i < toAddress.length; i++) {
//            	System.out.println("METHOD...!!!"+i);
//                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
//            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            System.out.println("error"+ae.getLocalizedMessage());
        	ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }

}