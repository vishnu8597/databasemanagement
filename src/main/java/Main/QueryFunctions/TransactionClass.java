package Main.QueryFunctions;

import Main.UI.MainUI;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class TransactionClass {

  String schema;

  public List<String> callTransaction( ){
    List<String> tranQuery=new ArrayList<>();
    String[] keyword;
    System.out.println("\n================================\n\tTYPE TRANSACTION HERE\n================================\n");
    Scanner scan=new Scanner(System.in);
    String line;
    while(true){
      line=scan.nextLine();
      if (line.equals("")){
        break;
      }
      else{
        tranQuery.add(line);
      }
    }
    return tranQuery;

  }

  public String createBackup(String table ){
    boolean status=false;
    String fileName="";
    try {
      fileName="schemas" + "/test/"+table.toLowerCase( ).trim()+"backup.txt";
      File myObj = new File(fileName);
      status=myObj.createNewFile();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return fileName;
  }

  public void copyFile(String source, String dest ){
    FileInputStream fileInput=null;
    FileOutputStream fileOutput=null;
    try{
      fileInput= new FileInputStream(source);
      fileOutput= new FileOutputStream(dest);
      int check;
      while((check=fileInput.read())!=-1){
        fileOutput.write(check);
      }
    }
    catch( IOException e){
      e.getMessage();
    }

  }

  public void dropBackup(String source ){
    File file = new File(source);
    file.delete();

  }


}
