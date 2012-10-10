package miloTester;

public class MaxProd {
    int dim;
    int itr;
    float sumDivisor;
    boolean isFeasibleSolution;

    float[][]costMatrix;
    float[][] costFunctionMatrix;
    float [][] ProductToVarMsgArray0;
    float [][] ProductToVarMsgArray1;
    float [][] CustomerToVarMsgArray0;
    float [][] CustomerToVarMsgArray1;

    float[][] varToCustMsgArray0_current;
    float[][] varToCustMsgArray1_current;
    float[][] varToProdMsgArray0_current;
    float[][] varToProdMsgArray1_current;

    float[][] varToCustMsgArray0_old;
    float[][] varToCustMsgArray1_old;
    float[][] varToProdMsgArray0_old;
    float[][] varToProdMsgArray1_old;

    variableNode[] variableArray;
    factorNode[] prodFactorArray;
    factorNode[] custFactorArray;
    int[][] permutationMatrix;
    int[][] previousPermutMatrix;
    int[] assignedCol;
    boolean[] sameAsPreviousPermutation=new boolean[10];

//constructor
MaxProd(float[][] SSMatrix, InputNode []custarray, InputNode[] prodarray){
    costMatrix=SSMatrix;
    
    dim=costMatrix.length;
    
    permutationMatrix= new int[dim][dim];
    previousPermutMatrix=new int[dim][dim];
    
    custFactorArray=new factorNode[dim];
    prodFactorArray=new factorNode[dim];

    costFunctionMatrix=new float[dim][dim];
    ProductToVarMsgArray0=new float[dim][dim];
    ProductToVarMsgArray1=new float[dim][dim];
    CustomerToVarMsgArray0=new float[dim][dim];
    CustomerToVarMsgArray1=new float[dim][dim];

    varToCustMsgArray0_current=new float[dim][dim];
    varToCustMsgArray1_current=new float[dim][dim];
    varToProdMsgArray0_current=new float[dim][dim];
    varToProdMsgArray1_current=new float[dim][dim];

    varToCustMsgArray0_old=new float[dim][dim];
    varToCustMsgArray1_old=new float[dim][dim];
    varToProdMsgArray0_old=new float[dim][dim];
    varToProdMsgArray1_old=new float[dim][dim];

    
    for(int i=0;i<dim;i++){
         custFactorArray[i]=new factorNode(custarray[i].name,i);
         prodFactorArray[i]=new factorNode(prodarray[i].name,i);
    }
    
    int dimSquared= (int) dim*dim;
    variableArray=new variableNode[dimSquared];

    int j=0;
    
    for(factorNode custNode:custFactorArray){
        for(factorNode prodNode: prodFactorArray){
            variableArray[j]=new variableNode(custNode, prodNode);
            j++;
        }
    }
    
varToCustMsgArray0_old=varToCustMsgArray0_current;
varToCustMsgArray1_old=varToCustMsgArray1_current;
varToProdMsgArray0_old=varToProdMsgArray0_current;
varToProdMsgArray1_old=varToProdMsgArray1_current;
    
   
}


//Main method
void calculateAssignments(){
    //normalizes the matrix of scores
    normalize();
   
    //calculate from cost factor nodes
    calculateCostFctn();
    
    initializeFactortoVarMsg();
    
    fill(varToCustMsgArray0_old,1);
    fill(varToCustMsgArray1_old,1);
    fill(varToProdMsgArray0_old,1);
    fill(varToProdMsgArray1_old,1); 
    
    //initialize previousPermuMatrix
    initializePrevPermutation();
    
    //ITERATE UNTIL WE GET THE SAME PERMUTATION MATRIX 10 TIMES IN A ROW--OR WE 
    //HAVE ITERATED MORE THAN 3000 TIMES WITHOUT CONVERGENCE
  
    initializePrevPermutation();
    
    while(!permutationMatrixConverged() && itr<3000){
        updatePreviousPermutation();

        //for(int k=0;k<9;k++){
        for(variableNode varNode: variableArray){
            calculateVar2CustMsg(varNode);
            calculateVar2ProdMsg(varNode);           
        }
    
        for(factorNode custNode:custFactorArray){
            for(variableNode varNode: variableArray){
            calculateCusttoVarMsg(custNode, varNode);
            }
        }
    
        for (factorNode prodNode:prodFactorArray){
            for(variableNode varNode: variableArray){
                calculateProdtoVarMsg(prodNode, varNode);            
            }
        }
        for(variableNode varNode: variableArray){
            calculateBelief(varNode);
        }
        compareResults();
        itr++;
    }   
    unNormalize(); //rescales cost matrix back to original
    returnAssignments();
    
    //determine if the assignment is a feasible solution:
    checkFeasibleSolution();
}
    
/**Initialize all factor to variable messages to be 1*/
void initializeFactortoVarMsg(){
fill(ProductToVarMsgArray0,1);
fill(ProductToVarMsgArray1, 1);
fill(CustomerToVarMsgArray0,1);
fill(CustomerToVarMsgArray1,1);
}

void fill(float [][] array, int x){
int length=array.length;

    for(int i=0;i<length;i++){
        for(int j=0;j<length;j++){
            array[i][j]=x;
        }
    }
}

void calculateCostFctn(){    
    for(int i=0;i<dim;i++){
        for(int j=0;j<dim;j++)
            costFunctionMatrix[i][j]=(float) Math.exp(costMatrix[i][j]);    
    }
}


void calculateVar2CustMsg(variableNode currentVar){
    int custIndex=currentVar.customer.index;
    int prodIndex=currentVar.product.index;
    
    //look up value of variable in the indicator 

    float msg0 = (float) ProductToVarMsgArray0[prodIndex][custIndex]; 
    float msg1 = (float) (costFunctionMatrix[custIndex][prodIndex]*ProductToVarMsgArray1[prodIndex][custIndex]); 
    
    varToCustMsgArray0_current[custIndex][prodIndex]=msg0/(msg0+msg1);
    varToCustMsgArray1_current[custIndex][prodIndex]=msg1/(msg0+msg1);
}

void calculateVar2ProdMsg(variableNode currentVar){
    int custIndex=currentVar.customer.index;
    int prodIndex=currentVar.product.index;
    int x_ij;
    float cost=costFunctionMatrix[custIndex][prodIndex];
    
    //x_ij=permutationMatrix[custIndex][prodIndex].getIntValue();

    float msg0 = (float) CustomerToVarMsgArray0[custIndex][prodIndex];
    float msg1 = (float) (costFunctionMatrix[custIndex][prodIndex]*CustomerToVarMsgArray1[custIndex][prodIndex]);

    varToProdMsgArray0_current[custIndex][prodIndex]=msg0/(msg0+msg1);
    varToProdMsgArray1_current[custIndex][prodIndex]=msg1/(msg0+msg1);
}


void calculateCusttoVarMsg(factorNode currentCust, variableNode currentVar){
float temp=0;
int custIndex=currentCust.index;
int prodIndex=currentVar.product.index;
float xij_0;
float xij_1=1;
int col = 0;        
float max=0;

//Case 1: x_ij=0 and max=:
for(int i=0;i<dim;i++){
    if(i!=prodIndex){
        if(max<varToCustMsgArray1_old[custIndex][i]){
            max=varToCustMsgArray1_old[custIndex][i];
            col=i;
        }
    }
}

xij_0=max;
for(int i=0;i<dim;i++){
    if(i!=prodIndex){
        if(i!=col){
            xij_0=xij_0*varToCustMsgArray0_old[custIndex][i];
        }
    }
}

//Case 2: x_ij=1 and xl,j (l!=i) =0
for(int j=0;j<dim;j++){
     if(j!=prodIndex){
         xij_1=xij_1*varToCustMsgArray0_old[custIndex][j];
     }
}
   //update this message in the message array       
    CustomerToVarMsgArray0[custIndex][prodIndex]=xij_0/(xij_0+xij_1);
    //update this message in the message array       
    CustomerToVarMsgArray1[custIndex][prodIndex]=xij_1/(xij_0+xij_1);
}



void calculateProdtoVarMsg(factorNode currentProd, variableNode currentVar){
float temp=0;
int prodIndex=currentProd.index;
int custIndex=currentVar.customer.index;
float xij_0;
float xij_1=1;
int row = 0;        
float max=0;

//Case 1: x_ij=0 and max=:
for(int i=0;i<dim;i++){
    if(i!=custIndex){
        if(max<varToProdMsgArray1_old[i][prodIndex]){
            max=varToProdMsgArray1_old[i][prodIndex];
            row=i;
        }
    }
}
xij_0=max;

for(int i=0;i<dim;i++){    
    if(i!=custIndex){
        if(i!=row){
            xij_0=xij_0*varToProdMsgArray0_old[i][prodIndex];
        }
    }
}

//Case 2: x_ij=1 and xl,j (l!=i) =0
for(int j=0;j<dim;j++){
     if(j!=custIndex){
         xij_1=xij_1*varToProdMsgArray0_old[j][prodIndex];
     }
}
   //update this message in the message array       
    ProductToVarMsgArray0[prodIndex][custIndex]=xij_0/(xij_0+xij_1);      
    ProductToVarMsgArray1[prodIndex][custIndex]=xij_1/(xij_0+xij_1);
}



    


void calculateBelief(variableNode variable){
    int prodIndex=variable.product.index;
    int custIndex=variable.customer.index;
    float belief_1;
    float belief_0;
    float cost;
    
    cost=costFunctionMatrix[custIndex][prodIndex];
    
    belief_1=(float) (cost)*ProductToVarMsgArray1[prodIndex][custIndex]*CustomerToVarMsgArray1[custIndex][prodIndex];
    
    belief_0=(float) ProductToVarMsgArray0[prodIndex][custIndex]*CustomerToVarMsgArray0[custIndex][prodIndex];

    //normalize beliefs
    float totalbelief=belief_1+belief_0;
    float b0=belief_0/totalbelief;
    float b1=belief_1/totalbelief;
    
    if (b1>b0){
        permutationMatrix[custIndex][prodIndex]=1;
    }
    else{
        permutationMatrix[custIndex][prodIndex]=0;
    } 
}
        
void normalize(){
    sumDivisor=0;
    
    for(int i=0;i<dim;i++){
        for(int j=0;j<dim;j++){
            sumDivisor+=costMatrix[i][j];
        }   
    }
    
    for(int i=0;i<dim;i++){
        for(int j=0;j<dim;j++){
            costMatrix[i][j]=costMatrix[i][j]/sumDivisor;
        }   
    }    
}


void unNormalize(){
        for(int i=0;i<dim;i++){
        for(int j=0;j<dim;j++){
            costMatrix[i][j]=costMatrix[i][j]*sumDivisor;
        }   
    }    
}

boolean permutationMatrixConverged(){
boolean converged = true;    

    for(int i=0; i<10; i++){
        if(sameAsPreviousPermutation[i]==false){
            converged=false;
            break;
        }
    }
    return converged;    
}


    private void initializePrevPermutation() {
        for (int y = 0; y < dim; y++){
	    for (int x = 0; x < dim; x++){
	        previousPermutMatrix[y][x] = 2;
            }
        }
    }
    
     private void updatePreviousPermutation() {
        for (int y = 0; y < dim; y++){
	    for (int x = 0; x < dim; x++){
	        previousPermutMatrix[y][x] = permutationMatrix[y][x];
            }
        }
    }

    //stores booleans in circular array
    private void compareResults() {
        int index =itr%10;
        
        if(equalMatrices()){
            sameAsPreviousPermutation[index] = true;
        }
        else{
            sameAsPreviousPermutation[index] = false;
        }
    }
    
boolean equalMatrices(){
    boolean isEqual=true;
    Outer:
    for(int i=0;i<dim;i++){
        for(int j=0;j<dim;j++){
            if(permutationMatrix[i][j]!=previousPermutMatrix[i][j]){
                isEqual=false;
                break Outer;
            }
        }
    }
    return isEqual;
}


    private void returnAssignments() {
        assignedCol=new int[dim];
        
        int row=0;
        int col=0;
        
        for(int i =0; i<dim; i++){
            for(int j =0; j<dim; j++){
                if(permutationMatrix[i][j]==1){
                    row=i;
                    col=j;
                    break;}
            }
            assignedCol[row]=col;}
    }

    private void checkFeasibleSolution() {
    int assignment=0;
    isFeasibleSolution=true;
    
    for(int i=0;i<dim;i++){
        assignment=0;
        for(int j=0;j<dim;j++){
            if(permutationMatrix[i][j]==1)
                assignment++;
        }
    }
    for(int i=0;i<dim;i++){
            if(assignment>1){
                isFeasibleSolution=false;
            }
    }
    }

private class factorNode{
    int index;
    String name;
    
    //array of assignment variables associated with the factor node
    variableNode[] assignmentVarArray; 
    
    //constructor
    factorNode(String factorname, int ind){
        name=factorname;
        index=ind;
    }
}

private class variableNode{
    factorNode customer;
    factorNode product;
    String name;
        
    variableNode(factorNode cust, factorNode prod){
        customer=cust;
        product=prod;
    }
}
}
    
    