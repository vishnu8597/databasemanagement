package Main.UI;

import Main.Dumps.*;
import Main.QueryFunctions.*;
import Main.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainUI {
    public String path="schemas";
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private String userID = null;
    private Boolean isQueryExecuted = false;
    boolean tranCheck;

    public static void main(String[] args) throws IOException, InterruptedException {

        MainUI uiObject=new MainUI();
        uiObject.mainUImethod();
    }

    public void mainUImethod() throws IOException, InterruptedException {

        UserLogin login=new UserLogin();
        boolean isLoggedIn=false;
        userID=login.enterLoginCredentials();

        if (!userID.equals(null)){
            isLoggedIn=true;
        }

        if (isLoggedIn)  {
            displayQueryOption();
        }
    }

    private String selectschema() throws IOException {
        System.out.println("Select Schema");
        String sc = reader.readLine();
        path=path+"/"+sc;
        File theDir = new File(path);
        if (!theDir.exists()){
            File meta = new File("schemas"+"/"+sc+"/"+"META");
            meta.mkdirs();
            File dumps = new File("schemas"+"/"+sc+"/"+"DUMPS");
            dumps.mkdirs();
            System.out.println("Schema created");
        }
        else{
            System.out.println("Schema Selected");
        }
        return sc;
    }

    private void displayQueryOption() throws IOException, InterruptedException {
        List<String> tranQuery;
        boolean commitCheck=false;
        boolean checkTrans=true;
        Scanner sc=new Scanner(System.in);
        String[] keyword=null;
        System.out.println("\nPress 1 for Simple Query\nPress 2 for Transaction");
        int response=sc.nextInt();
        if(response==1){
            System.out.println("\n=============================\n\tQUERY TYPE\n=============================\n");
            String query = reader.readLine();
            keyword = query.split(" ");
            selectQueryFunction(keyword[0].toLowerCase(),query);
        }
        else if(response==2 ){
            TransactionClass transaction=new TransactionClass();
            ParseQuery parse=new ParseQuery();
            tranQuery=transaction.callTransaction();
            int querySize=tranQuery.size();
            if((tranQuery.get(0).contains("begin"))
                    &&(tranQuery.get(querySize-1).contains("end"))){
                Map<String, String> queryMap= new HashMap<>();
                List<String> tableName=new ArrayList<>() ;
                for(String query:tranQuery){
                    if((query.contains("commit"))||(query.contains("COMMIT"))
                            ||(query.contains("Commit"))){
                        if(checkTrans) {
                            for (String name : tableName) {
                                transaction.dropBackup("schemas/test/" + name + "backup.txt");
                            }
                            commitCheck = true;
                        }
                    }
                    else if (query.equalsIgnoreCase("end")) {
                        if(commitCheck){
                            System.out.println("Transaction completed.");
                        }
                        else{
                            for (String name : tableName) {
                                transaction.copyFile(  "schemas/test/" + name + "backup.txt",
                                        "schemas/test/" + name +  ".txt");
                                transaction.dropBackup("schemas/test/" + name + "backup.txt");
                            }
                            System.out.println("Transaction Failed.");
                        }
                        break;
                    }
                    else if((query.contains("insert".toLowerCase( )))
                            ||(query.contains("update".toLowerCase( )))
                            ||(query.contains("delete".toLowerCase( )))){
                        String table="";
                        queryMap.clear();
                        queryMap  = parse.processQuery(query);
                        table=parse.getInsertTableName(queryMap);
                        if ( checkTrans) {
                            if (!tableName.contains(table.toLowerCase())) {
                                tableName.add(table.toLowerCase());
                                String name=transaction.createBackup(table);
                                transaction.copyFile("schemas/test/" + table +  ".txt" ,name);
                            }
                            keyword = query.split(" ");
                            if (!selectQueryFunction(keyword[0].toLowerCase(), query)) {
                                for (String name : tableName) {
                                    transaction.copyFile(  "schemas/test/" + name + "backup.txt",
                                            "schemas/test/" + name +  ".txt");
                                    transaction.dropBackup("schemas/test/" + name + "backup.txt");
                                }
                                break;
                            }

                        }
                        else if(( !tableName.isEmpty())&&(!checkTrans)) {
                            break;
                        }

                    }
                    else if (!query.equalsIgnoreCase("begin")) {
                        keyword = query.split(" ");
                        if(!selectQueryFunction(keyword[0].toLowerCase(),query)) {
                            for (String name : tableName) {
                                transaction.copyFile(  "schemas/test/" + name + "backup.txt",
                                        "schemas/test/" + name +  ".txt");
                                transaction.dropBackup("schemas/test/" + name + "backup.txt");
                            }
                            break;
                        }

                    }
                }
            }
            else{
                System.out.println("Error in query.") ;
            }

        }

    }

    private boolean selectQueryFunction(String keyword,String query) throws InterruptedException, IOException {

        String  newQuery ;

        switch (keyword) {
            case "select":
                //Select Function;
                SelectQuery selectQuery = new SelectQuery();
                String selectSchemaName = selectschema();
                isQueryExecuted = selectQuery.selectFunction(query, selectSchemaName);

                //Make SQL Dump
                if (isQueryExecuted){
                    MakeDumps makeDumps = new MakeDumps(query, selectSchemaName);
                    makeDumps.dumping();
                }

                break;

            case "insert":
                //Insert Function;
                InsertClass insertQuery=new InsertClass();
                String insertSchemaName=selectschema();
                newQuery=query.replace(";","");
                isQueryExecuted=insertQuery.insertTable(newQuery,insertSchemaName);

                //Make SQL Dump
                if(isQueryExecuted) {
                    MakeDumps makeDumps1 = new MakeDumps(query, insertSchemaName);
                    makeDumps1.dumping();
                }
                else{
                    System.out.println("insertion failed");
                }
                break;

            case "update":
                UpdateQuery updateQuery = new UpdateQuery(selectschema());
                newQuery=query.replace(";","");
                isQueryExecuted =updateQuery.update(newQuery);
                break;

            case "create":
                //Create Function;
                CreateTable createTable = new CreateTable(selectschema());
                isQueryExecuted=createTable.createTable(query);
                break;

            case "erd":
                ERD erd = new ERD();
                erd.display();
                isQueryExecuted = true;
                break;

            case "drop":
                tranCheck=false;
                DropClass dropQuery=new DropClass();
                String dropSchemaName=selectschema();
                isQueryExecuted = dropQuery.DropTable(query,dropSchemaName);

                //Make SQL Dump
                if(isQueryExecuted) {
                    MakeDumps makeDumps4 = new MakeDumps(query, dropSchemaName);
                    makeDumps4.dumping();
                }
                break;

            case "delete":
                //Delete Function;
                tranCheck=false;
                DeleteQuery deleteQuery = new DeleteQuery();
                String deleteSchemaName=selectschema();
                isQueryExecuted = deleteQuery.deleteFunction(query,deleteSchemaName);

                //Make SQL Dump
                if(isQueryExecuted) {
                    MakeDumps makeDumps2 = new MakeDumps(query, deleteSchemaName);
                    makeDumps2.dumping();
                }
                break;

            case "exit":
                System.out.println("");
                System.exit(1);
                break;

            default:
                System.err.println(" ***** Wrong Input *****");
                TimeUnit.SECONDS.sleep(2);
                displayQueryOption();
                break;
        }

        //update log
        if (!(keyword.contains("meta") || keyword.equals("dumps"))) {
            CreateLogFile createLogFile = new CreateLogFile(keyword,query, userID, isQueryExecuted);
            createLogFile.updateLogFile();
        }

        return isQueryExecuted;
    }

}
