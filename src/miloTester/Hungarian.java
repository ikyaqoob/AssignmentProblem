package miloTester;


import java.util.Arrays;
import java.util.Random;

public class Hungarian {   
    float[][]origCostMatrix;
    float[][]reducedCostMatrix;
    boolean[] assignedRow;
    boolean[] checkedRow;
    boolean []checkedCol; 
    int[] assignedCol;
    int dim;
    boolean doneChecking=true;
    boolean madeNewRowAssignment;
    boolean madeNewColAssignment;
    costMatrixElmnt[][] matrix; 




public Hungarian(float [][] SSMatrix){
    origCostMatrix=SSMatrix;
    reducedCostMatrix=origCostMatrix;
    dim=origCostMatrix.length;
    assignedRow=new boolean [dim];
    checkedRow=new boolean [dim];
    checkedCol=new boolean [dim];
    madeNewRowAssignment=true;
    madeNewColAssignment=true;
        
    //initialize checkedRows (a row is checked if it is unassigned) to be true
    Arrays.fill(checkedRow,true);
    assignedCol=new int [dim];
    Arrays.fill(assignedCol, -1);
}

public void calculateAssignments() {
    //turn into a minimization problem by each element of cost matrix by the largest          
    matrix=new costMatrixElmnt[dim][dim];

    //convert to minimization problem; calculate reduced rows, reduced columns
    createInitialCostMat();
                
    //convert reducedMatrix to a 2D array of Classes which encapsulate boolean (isAssigned), and cost
    makeAssignments();
    int itr=0;             
    
    while(!allRowAssigned()){
        createNewZero();
        clearAssigments();
        makeAssignments(); 
    }
}


   
    /**
     * the first step of the hungarian algorithm
     * is to find the smallest element in each row
     * and subtract it's values from all elements
     * in that row
     * 
     *
     * @return the next step to perform
     */
    private void createInitialCostMat() {
        float max = findMax();
        subtractfromMax(max); 
        subtractMinfrmRow();
        subtractMinfrmCol();
    }

    private void subtractMinfrmRow(){
       // float[][] tempMatrix = reducedCostMatrix;
        float minRow;
        
        for(int i =0;i<dim;i++){
            minRow=findMininRow(i);
            
            for (int j = 0; j < dim; j++) {
                float newValue= reducedCostMatrix[i][j]-minRow;
                costMatrixElmnt costAmt= new costMatrixElmnt(newValue);
                matrix[i][j]=costAmt;
                }
        }
    }

    private void subtractMinfrmCol(){
        float minCol;
        float newVal;
            for (int i = 0; i < dim; i++) {
                minCol=findMinCol(i);
                for (int j = 0; j < dim; j++) {
                    newVal=matrix[j][i].cost-minCol;
                    matrix[j][i]=new costMatrixElmnt(newVal);
                }
            }
    }

    private float findMininRow(int i){
        float minValInRow=10000;

                // find the min value in the row
                for (int j = 0; j < dim; j++) {          
                    if (minValInRow > reducedCostMatrix[i][j]) {
                        minValInRow = reducedCostMatrix[i][j];
                    }
                }
            return minValInRow;
    }


    private float findMinCol(int i){
        float minValInCol = 10000;
                for (int j = 0; j < dim; j++) { 
                    if (minValInCol > matrix[j][i].cost) {
                        minValInCol = matrix[j][i].cost;
                    }
                }
            return minValInCol;
    }


    private void subtractfromMax(float max){
        for( int r = 0; r < dim; ++r ) {
            for( int c = 0; c < dim; ++c ) {
                reducedCostMatrix[r][c]=max-origCostMatrix[r][c];
            }
        }
    }



    private float findMax(){
    float max=0;
     reducedCostMatrix=new float[dim][dim];
    // Iterate through each element in the array
    for( int r = 0; r < dim; ++r ) {
        for( int c = 0; c < dim; ++c ) {
            if( origCostMatrix[r][c] > max ) {
                max = origCostMatrix[r][c];
            }
        }
    }    
    return max;
    }

    /** Makes assignments between the products and customers:
     * First iterates through the rows of the cost matrix: if there is a unique 0, make
     * an assignment and make the corresponding column of the 0 as being assigned
     * 
     * IMPORTANT: All costs of value 0 must be assigned
    *  2 special cases: If 1) no assignments are made during an iteration or 2) there exist
     * 0's which are not assigned, arbitrarily make assignments
     * */

    private void makeAssignments() {
        lookforAssignments:
            while(madeNewRowAssignment || madeNewColAssignment){
                assignRows();
                assignColumns();
                
                if(allRowAssigned())
                    break lookforAssignments;
            }
        
        //there exist 0's which are not either assigned or crossed, assign arbitrarily
        while(!allZeroOccupied()){
             makeArbAssignments();
        }
    }
            
       
    private void assignRows() {
        madeNewRowAssignment=false;
        int count;;
        int col=0;
        int row=0;
            
        for(int i=0; i<dim;i++){
            count=0;
            if(assignedRow[i]==false){
                for(int j=0; j<dim;j++){
                    if(!matrix[i][j].isOccupied){
                        if(matrix[i][j].cost==0){
                            count++;col=j; row=i;}
                    }
                }
                
                if (count==1){
                    matrix[row][col].isAssigned=true;
                    assignedCol[row]=col;
                    assignedRow[row]=true;
                    checkedRow[row]=false;
                    madeNewRowAssignment=true;
                    for(int k=0; k<dim;k++){
                        matrix[k][col].isOccupied=true;}
                }
            }
          }
    }//closes method
    




   /**If after one iteration, not all zeros have been assigned, make random assignments*/
    private void makeArbAssignments(){
        int count=0;
        int modCol;
        
        Random rn = new Random();
        int randCol=rn.nextInt(dim+1);;
       
        makeAssignmentLoop:
        for(int row=0; row<dim;row++){
            if(assignedRow[row]==false){ //if no assignment at this row, make arbitrary assignment
                for(int j=randCol;j<randCol+dim;j++){
                    modCol=j%dim;
                    if(matrix[row][modCol].cost==0){
                        if(!matrix[row][modCol].isAssigned){
                            if(!matrix[row][modCol].isOccupied){
                                  matrix[row][modCol].isAssigned=true;
                                    assignedCol[row]=modCol;
                                    assignedRow[row]=true; 
                                    checkedRow[row]=false;
                                    madeNewRowAssignment=true;

                                    for(int k=0; k<dim;k++){
                                        matrix[k][modCol].isOccupied=true;
                                    }


                                    for(int k=0; k<dim;k++){
                                        matrix[row][k].isOccupied=true;
                                    }
                                    break makeAssignmentLoop;
                            }
                        }
                    }
                }
            }
            }
        //after breaking out of loop
        makeAssignments();
    }
    
    
    
    /**for the unassigned rows, check the corresponding columns
       Delete min of reduced matrix from all elements, which creates at least a new 0*/
    private void createNewZero(){
        checkedCol=new boolean[dim];
        
        //for all unassigned rows, check columns where there is a 0
        do{
            checkColumn();
            checkRow();
        }
                while(!doneChecking);
          
        //find minimum element from unchecked rows and checked columns
        float min= findnewTheta();
        subtractTheta(min);
        addTheta(min);
    }

    
    

    
    
    /**Checks the columns of 0's of unassigned rows*/
    private void checkColumn(){
    doneChecking=true;
    
        for(int i=0;i<dim;i++){
            if(checkedRow[i]==true){ //a row is checked if it is unassigned 
                for(int j=0;j<dim;j++){
                    if(matrix[i][j].cost==0){
                        if(checkedCol[j]!=true){
                            checkedCol[j]=true; //made a new check
                            doneChecking=false;
                        }
                    }            
                }
            }
        }
    }
    
    
    private void checkRow(){
            doneChecking=true;
            
            for(int i=0;i<dim;i++){
            if(checkedCol[i]==true){
                for(int j=0;j<dim;j++){
                    if(matrix[j][i].cost==0){
                        if(matrix[j][i].isAssigned==true)
                        if(checkedRow[j]!=true){
                            checkedRow[j]=true; //made a new check
                            doneChecking=false;
                            break;
                        }
                    }
                }
            }
            }
    }


   

    /** draw lines thru unticked rows and ticked columns 
    theta=smallest number with no line passing through it*/
    private float findnewTheta(){
        float min=10000;
        
        for(int i=0;i<dim;i++){
            if(checkedRow[i]==true){
            for(int j=0;j<dim;j++){
                if(checkedCol[j]==false){
                    if(matrix[i][j].cost<min)
                        min=matrix[i][j].cost;
                }
            }
            }
        }
        if(min==10000){ //either the cost is greater than 10000 (unlikely), or we are not able to find a new theta to subtract
            min=0;
        }
        return min;
    }
    
    /**Subtracts theta from all elements that are not in a crossed row or crossed column*/
    private void subtractTheta(float min){
    float theta=min;
    
    for(int i=0;i<dim;i++){
        if(checkedRow[i]==true){
            for(int j=0;j<dim;j++){
                if(checkedCol[j]==false){
                    matrix[i][j].cost-=theta;
                }
            }
        }
    }
    }
                
    
    /**Add theta to all elements that are in the intersection of checked row and checked column*/
    private void addTheta(float theta){
        for(int i=0;i<dim;i++){
            if(checkedRow[i]==false)
                    for(int j=0;j<dim;j++){
                        if(checkedCol[j]==true){
                            matrix[i][j].cost+=theta;
                        }
                    }
        }
    }
    
    
    
    
    //"True" indicates the customer that corresponds to that row has been assigned a product
    private boolean allRowAssigned(){
        boolean AllTrue=true;
  
        
        for(int loop=0; loop<dim;loop++)
        {
            if(assignedRow[loop]==false)
            { 
                AllTrue=false;
                break;
            }
        }
        return AllTrue;
    }
    
    
    //check that all costs=0 have been assigned--ie, assigned 
    private boolean allZeroOccupied(){
        boolean all0Occupied=true;
        search:
        for(int i=0; i<dim;i++){
            for(int j=0; j<dim;j++){
            if(matrix[i][j].cost==0){
                if(!matrix[i][j].isOccupied){
                    all0Occupied=false;
                    break search;  
                }
            }
            }
        }
        return all0Occupied;
    }

    /**Refresh assignments from previous iteration*/
    private void clearAssigments() {
        assignedRow=new boolean [dim];
        checkedRow=new boolean [dim];
        madeNewRowAssignment=true;
        madeNewColAssignment=true;
        
        //initialize checkedRows (a row is checked if it is unassigned) to be true
        Arrays.fill(checkedRow,true);
        assignedCol=new int [dim];
        Arrays.fill(assignedCol, -1);
        
            for(int i=0; i<dim;i++){
            for(int j=0; j<dim;j++){
                matrix[i][j].isAssigned=false;
                matrix[i][j].isOccupied=false;
            }
        }
    }

    private void assignColumns() {
        int col=0;
        int row=0;
        int count; 
        madeNewColAssignment=false;
    
         for(int i=0; i<dim;i++){
            count=0;
            if(assignedCol[i]==-1){
            for(int j=0; j<dim;j++){
                if(!matrix[j][i].isOccupied){
                        if(matrix[j][i].cost==0){
                        count++; col=i; row=j;
                        }
                }
            }
        
            if (count==1){
                matrix[row][col].isAssigned=true;
                madeNewColAssignment=true;
                        
                for(int k=0; k<dim;k++){
                    matrix[row][k].isOccupied=true;
                }
                assignedCol[row]=col;
                assignedRow[row]=true;
                checkedRow[row]=false;
            }
         }
         }
    }

   

    
    private static class costMatrixElmnt {
        float cost;
        boolean isAssigned=false;
        boolean isOccupied=false;
       
        //empty constructor
        public costMatrixElmnt() {
        }
        
        public costMatrixElmnt(float costAmt){
            cost=costAmt;
        }
    }
}




