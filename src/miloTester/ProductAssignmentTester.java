package miloTester;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


        
public class ProductAssignmentTester {
        float[][] custProdMatrix;
	static LinkedList<InputNode> customer=new LinkedList<InputNode>();
	static LinkedList<InputNode> product=new LinkedList<InputNode>();
        static float SS;
        static InputNode[] customerArray;
        static InputNode[] prodArray;
        static int []assignmentArray; 
        static String userInputFilePath;
        static float [][] InputMatrix;
        static int dim;
        static int readerSelection;
        static boolean fromCSVFile;

	/**
	 * The system class asks the user to input the file location of the comma delimited file
	 * containing the list of customers and products. The user can choose to solve it using the
         * Hungarian Method or by finding the MAP on a graphical model
	 */
        
        public static void main(String[] args) throws IOException {
            initialpromptMenu();   
        }
        
        
        
        
        
        
        
        private static void initialpromptMenu() {	
	Scanner readOption = new Scanner(System.in);
        while(true){
            System.out.println("\n Directory Listing Menu" +
                            "\n 1. Read csv file" +
                            "\n 2. Read flat file of SS scores "+
                            "\n 3. Exit" +
                            "\n Enter selection: ");
                try{
                    readerSelection = Integer.parseInt(readOption.nextLine());
                    readUserConsoleInitialMenu();
                    break;}
                catch(Exception e){
                        System.out.println("Did not recognize input. Please re-enter.");
                }
            }
        }
        
        static void calculationPromptMenu() {	
	Scanner readOption = new Scanner(System.in);
        while(true){
            System.out.println("\n Directory Listing Menu" +
                            "\n 1. Solve with Hungarian Algorithm" +
                            "\n 2. Solve with MaxProduct Message Passing Algorithm"+
                            "\n 3. Return to main menu" +
                            "\n 4. Exit" +
                            "\n Enter selection: ");
                try{
                    readerSelection = Integer.parseInt(readOption.nextLine());
                    readUserConsole();
                    break;}
                catch(Exception e){
                        System.out.println("Did not recognize input. Please re-enter.");
                }
            }
        }

          
        static void readUserConsoleInitialMenu() throws IOException {
        if (readerSelection==1){
            readCSVFile();
            parseCSVFile();
            initialpromptMenu();
        }
            
        if (readerSelection==2){
            parseScoresfromFile();
            fromCSVFile=false;
            System.out.println("Select algorithm from menu: ");
            calculationPromptMenu();
            initialpromptMenu();
        }
        if (readerSelection==3){
            System.exit(0);
        }
        }
    
        static void readUserConsole() throws IOException{	
	if (readerSelection==1){
            Hungarian hungarianSolver=new Hungarian(InputMatrix);
            hungarianSolver.calculateAssignments();
            assignmentArray=hungarianSolver.assignedCol;
            printMaxSum();
            
            if(fromCSVFile)
                printCustomerProductCombo();
            return;
	}	
	if (readerSelection==2){
             MaxProd maxSumSolver=new MaxProd(InputMatrix, customerArray, prodArray);
             maxSumSolver.calculateAssignments();
             assignmentArray=maxSumSolver.assignedCol;
             InputMatrix=maxSumSolver.costMatrix; 
             if(maxSumSolver.permutationMatrixConverged() && maxSumSolver.isFeasibleSolution){
                 printMaxSum();
             
             if(fromCSVFile)
                 printCustomerProductCombo();
                 return;
             }
        
             else if(maxSumSolver.permutationMatrixConverged() && !maxSumSolver.isFeasibleSolution){
                 System.out.println("The message passing algorithm has converged to an infeasible solution. Most likely there is no unique solution. Please select Hungarian algorithm form the menu.");
                 calculationPromptMenu();
             }
             
             else if(!maxSumSolver.permutationMatrixConverged())
                 System.out.println("The message passing algorithm has not converged. Most likely there is no unique solution to the problem; it may also require more than 3,000 iterations to converge."); 
                 calculationPromptMenu();;
        }
        if (readerSelection==3){
            System.exit(0);
        }
        }
        
        
	/**Creates dummy variables for either customers or products if the number of customers
         is not equal to the number of products*/
	static void createDummy(){
            InputNode dummy;
                if (customer.size()>product.size()){
                    int numDummy=(customer.size()-product.size());
                    //create new product dummies
                    for(int i=0; i<numDummy;i++){
                        dummy=new InputNode("Dummy", true);
                        product.add(dummy);
                    }
                }
                else{
                    int numDummy=(product.size()-customer.size());
                    //create new customer dummies
                    for(int i=0; i<numDummy;i++){
                        dummy=new InputNode("Dummy", true);
                        customer.add(dummy);
                    }
                }
        }
                    
        static void parseScoresfromFile() throws FileNotFoundException, IOException{         
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input file name: ");
        // Reads a single line from the console and stores into name variable
        userInputFilePath= scanner.nextLine();
            
        //Calculates total number of lines in the document
        double lines=countLines();
        dim=(int) Math.sqrt(lines);
        BufferedReader in=null;
   
        try {
                File file = new File(userInputFilePath);
                FileReader inputFil = new FileReader(file);
                in = new BufferedReader(inputFil);

                InputMatrix=new float[dim][dim];
                String str;
                for(int i=0;i<dim;i++){
                    for(int j=0;j<dim;j++){
                            str =in.readLine();
                            InputMatrix[i][j] =  Float.parseFloat(str);
                    }
                }
                customer=new LinkedList();
                product=new LinkedList();
                for(int i=0;i<dim;i++){
                         InputNode cust=new InputNode ("placeholder", false);
                         customer.add(cust);
                    }
                for(int i=0;i<dim;i++){
                         InputNode prod=new InputNode ("placeholder", false);
                         product.add(prod);
                    }
                convertToArray();
                assignmentArray=new int[dim];
        } catch (FileNotFoundException ex) {
                System.out.println("File not found. Please re-enter.");
                initialpromptMenu();
        } finally {
            try {
                    in.close();
            } catch (IOException ex) {
                    System.out.println("File error. Please re-enter.");
                    Logger.getLogger(ProductAssignmentTester.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        }
        
        static void readCSVFile(){
            System.out.println("Please input file name: ");
            Scanner scanner = new Scanner(System.in);
            userInputFilePath= scanner.nextLine();
        }        
        
        /**Parses file; creates String array of customers and products*/
        static void parseCSVFile() throws IOException{
            FileInputStream fstream = null;
            try {
                fstream = new FileInputStream(userInputFilePath);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                int itr=1;
                
                //Read File Line By Line where each Line is a test case
                while ((strLine = br.readLine()) != null)   {
                    StringTokenizer st = new StringTokenizer(strLine, ";");
                    String customerStr = st.nextToken(";");
                    customer= new LinkedList();
                    product= new LinkedList();
                    customer=parseString(customerStr, ",");

                    for (Iterator it = customer.iterator (); it.hasNext();) {
                        InputNode custStr = (InputNode) it.next();
                    }
                    String productStr = st.nextToken(";");
                    product=parseString(productStr, ",");

                    for (Iterator it = product.iterator (); it.hasNext();) {
                        InputNode prodStr = (InputNode) it.next();
                        }
                    
                    System.out.println("For line "+ itr++ +" of file "+ userInputFilePath+ ", please choose method of calculation from the menu: ");
                    
                    if(customer.size()!=product.size())
                        createDummy();

                    createCostMatrix();
                    convertToArray(); //converts the linkedlist of customers and products to an array
                    fromCSVFile=true;
                    calculationPromptMenu();
                }
            } catch (FileNotFoundException ex) {
                System.out.println("File not found. Please re-enter.");
                initialpromptMenu();
            } finally {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    System.out.println("File error. Please re-enter.");
                    Logger.getLogger(ProductAssignmentTester.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        }
        

	/**Creates a square matrix of SS scores from the arrays of customer names and product names. A dummy is used to make the matrix 
	 * square if the number of customers is not equal to the number of products
	 * @parameter
	 * @returns*/
	static void createCostMatrix() {
		//iterate through customers to calculate the SS score of each product per customer
		int row=0;
                int col=0;
                
                dim=getMax(customer, product);
               
                //initialize cost matrix
                InputMatrix=new float[dim][dim];
                
                for (Iterator it = customer.iterator(); it.hasNext();) {
		    InputNode cust = (InputNode) it.next();
                    col=0;
		    //iterate throuhg products
		    for (Iterator prodItr = product.iterator(); prodItr.hasNext();){
		    	InputNode prod = (InputNode) prodItr.next();
                       
                        if(prod.isDummy){
                            SS=0;
                            break;}      
                        else if(prod.isEven()){
		    		SS = (float) 1.5*cust.vowels;}
		    	//else is odd
		    	else{
                            SS=cust.constants;}
                        if(hasCommonFactor(cust.letterlength,prod.letterlength)){
                            SS=(float) 1.5*SS;}
                        
                        InputMatrix [row][col]=SS;		    
                        col++;
                    } //ends inner for statement
		row++;
                }
	}


        static boolean hasCommonFactor(int a, int b){
            boolean commonFactorFound=false;
            int number1=a;
            int number2=b;
            
                //List factorsList = new ArrayList();
		//int[] factors = new int [n];
		for (int i = 2; i <= number1; i++) {
			while (number1 % i == 0) {
				if(number2 % i ==0){
                                    commonFactorFound=true;
                                    break;
                                }
				number1/= i;
			}
		}//ends for statement
                return commonFactorFound;
        }
          
        
             
        static int getMax(LinkedList a, LinkedList b){
        //retuns the max length of two lists
            LinkedList firstList =a;
            LinkedList secondList=b;
            int maxLength=firstList.size();
             
            if(firstList.size()<secondList.size()){
                maxLength=secondList.size();
            }
            //else either the first list is longer or the lengths are equal, in both cases we leave it
            //as initialized value
            return maxLength;
        }

        
        /**Converts the customer and product linkedlists to array*/
	static void convertToArray() {  
            customerArray=new InputNode[dim];
            prodArray=new InputNode[dim];
            
        int i=0;
        for (Iterator itCust = customer.iterator (); itCust.hasNext();) {
            customerArray[i] = (InputNode) itCust.next();
            i++;}
        
        i=0;
        for (Iterator itProd = product.iterator (); itProd.hasNext();) {
            prodArray[i] = (InputNode) itProd.next();
            i++;}
	}



	static LinkedList parseString(String list, String delim){
            LinkedList result = new LinkedList();
	    
            StringTokenizer tokenizer = new StringTokenizer(list, delim);
	    
            while (tokenizer.hasMoreTokens()) {
	        //result.add(tokenizer.nextToken());
	    	  InputNode node = new InputNode(tokenizer.nextToken(), false);
	    	  result.add(node);
	      }
	     return result;
	  }
        
        
              
	static void printMaxSum(){
            DecimalFormat df = new DecimalFormat( "#########0.00");
            float sum=0;
            
            for(int i=0;i<dim;i++){
                
                sum+=InputMatrix[i][assignmentArray[i]];
                }
            
            String formattedSum = df.format(sum);
            System.out.println("The maximum total SS score is "+formattedSum+". ");
        }
        

        static void printCustomerProductCombo(){
            for(int i=0;i<dim;i++){
                InputNode customerId = customerArray[i];
                if(customerId.isDummy)
                    customerId.name="No one";
                if(prodArray[assignmentArray[i]].isDummy)
                    prodArray[assignmentArray[i]].name="nothing";
         
            System.out.println(customerArray[i].name+" is assigned to "+prodArray[assignmentArray[i]].name);
            }
        }
        
        static double countLines() throws IOException {
            double count=0;
            InputStream is=null;
            try {
                is = new BufferedInputStream(new FileInputStream(userInputFilePath));
                byte[] c = new byte[1024];
                int readChars = 0;
                
                while ((readChars = is.read(c)) != -1) {
                    for (int i = 0; i < readChars; ++i) {
                        if (c[i] == '\n')
                            ++count;
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProductAssignmentTester.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                is.close();
                return count;
            }
        }
}
    
    
	
	

