package miloTester;


public class InputNode{
	int letterlength;
	String name;
	int Multplier;
	int vowels;
	int constants;
        boolean isDummy;

        //Empty constructor
	InputNode(){
            
        }
        
	InputNode(String aname, boolean isdummy){		
			name=aname;
			letterlength=numofLetters(name);
			vowels=countVowels(name);
			constants=letterlength-vowels;
                        isDummy=isdummy;
		}
                
		public boolean isEven(){
			return (letterlength % 2 == 0);
		}
		
		 
		 public int countVowels(String word){
                 //Counts number of vowels in a word
			  int count = 0; 
			  word = word.toLowerCase();
			  
			    for (int i = 0; i < word.length(); i++) {
			        char c = word.charAt(i);
			        if (c=='a' || c=='e' || c=='i' || c=='o' || c=='u' || c=='y') {
			            count++;
			        }
			    }
			    return count;
			}
                  
                
                 public int numofLetters(String str){
                  //returns the number of letters (ie not numbers, symblos, or whitespace
                     int count=0;
                     for(int i=0;i<str.length();i++){
                         if (Character.isLetter(str.charAt(i))){
                             count++;
                         }
                     }//ends for statement
                     return count;
 }
		  
		
			  
		  


                  
    
}
