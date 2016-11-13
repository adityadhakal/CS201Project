public class Test2 {

	public static void main(String[] args) {		
		int a = 10;
		func1(a);		
	}
	
	public static void func1 (int x) {
	    int y = x;
	    int z = 1;
	    while(y > 0){
	        x=x-z;
	        y=x;
	    }
	}
	
}
