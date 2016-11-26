
public class Edge {
	
	public int srcBlc ;
	public int dstBlc ;
	public String methodSign;
	public int numberOfTimes ;
	
	public Edge(int src, int dst, String mthd){
		numberOfTimes = 1;
		srcBlc = src ;
		dstBlc = dst ;
		methodSign = mthd ;
	}
	
	public void increase(){
		numberOfTimes++;
	}
	
}
