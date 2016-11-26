import java.util.ArrayList;

public class EdgeProfiling {
	
	private static ArrayList<Edge> edges;
	private static int lastVisited = 0;
	
	public static void init(){
		edges = new ArrayList<>();
	}
	
	public static void increase(String methodSign,int desBlc){
		for(int i = 0 ; i < edges.size() ; i++){
			Edge e = edges.get(i);
			if(e.methodSign == methodSign && e.srcBlc == lastVisited && e.dstBlc == desBlc){
				e.increase();
				return;
			}
		}
		Edge e = new Edge(lastVisited, desBlc, methodSign);
		edges.add(e);
		lastVisited = desBlc;
	}
	
	
	public static void print(){
		for(int i = 0 ; i < edges.size() ; i++){
			System.out.println("Method S: " + edges.get(i).methodSign);
			System.out.println("b"+edges.get(i).srcBlc + "-->b"+edges.get(i).dstBlc+":"+edges.get(i).numberOfTimes);
		}
	}
}
