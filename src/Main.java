import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.thread.mhp.DfsForBackEdge;
import soot.jimple.toolkits.thread.mhp.LoopBodyFinder;
import soot.jimple.toolkits.thread.mhp.stmt.JPegStmt;
import soot.options.Options;
import soot.tagkit.Tag;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.ExceptionalGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.pdg.MHGDominatorTree;
import soot.util.Chain;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.jimple.Stmt;


import soot.jimple.toolkits.thread.mhp.stmt.JPegStmt;
import soot.tagkit.*;
import soot.util.*;


public class Main {
	
	public static void main(String[] args) {
		
		//Static Analysis (Retrieve Flow Graph)
		staticAnalysis();

		//Dynamic Analysis (Instrumentation) 
		dynamicAnalysis();
 
		soot.Main.main(args);

	}
	


	private static void staticAnalysis(){
		configure("/home/aditya/Downloads/CS201Profiling/Analysis"); //Change this path to your Analysis folder path in your project directory
		SootClass sootClass = Scene.v().loadClassAndSupport("Test3");
	    sootClass.setApplicationClass();
	    ArrayList<SootMethod> methods = (ArrayList)sootClass.getMethods();
	    for(SootMethod m : methods){
	    	System.out.println("Method:" + m);
	    	Body methodBody = m.retrieveActiveBody();
	    	ExceptionalBlockGraph blockGraph = new ExceptionalBlockGraph(methodBody);
	    	MHGDominatorTree dominatorTree = new MHGDominatorTree(new MHGDominatorsFinder(blockGraph));
	    	
	    	//Loop Calcuations
	    	Collection loops = computeLoops(methodBody);
	    	System.out.println("loops here "+loops);
	    	
	    	//Block Calculations
	    	
	    	List<Block> blocks = blockGraph.getBlocks();
	    	for(Block b : blocks){
	    		System.out.println("Basic Block: " + b.getIndexInMethod());
	    		Iterator<Unit> units = b.iterator();
	    		while(units.hasNext()){
	    			System.out.println(units.next());
	    		}
	    		List<Block> preds = b.getPreds();
	    		List<Block> succs = b.getSuccs();
	    		System.out.print("Preds: ");
	    		for(Block p : preds){
	    				System.out.print(p.getIndexInMethod()+" ");
	    		}
	    		System.out.print("\nSuccs: ");
	    		for(Block s : succs){
	    				System.out.print(s.getIndexInMethod()+" ");
	    		}
	    		System.out.println("\n");
	    	}
	    	System.out.println("Domintaor Sets: ");
	    	SimpleDominatorsFinder finder = new SimpleDominatorsFinder(blockGraph);
	    	
	    	for(Block b : blocks){
	    		System.out.print("Block " + b.getIndexInMethod() + " --> ");
	    		List<Block> d = finder.getDominators(b);
	    		for (Block dominator : d){
	    			System.out.print("Block " + dominator.getIndexInMethod() + " ");
	    		}
	    		System.out.println();
	    	}
	    	System.out.println();
	    	
	    }
	    
	    //Static Analysis code
	}
	
	//For finding loops
	private static Collection<Loop> computeLoops(Body b){
			LoopFinder loopFinder = new LoopFinder();
			loopFinder.transform(b);
			Collection<Loop> loops = loopFinder.loops();
			return loops;
			
	}
	
	// Show dominator sets
	private static void dominatorSet(MHGDominatorTree dom, DominatorNode root){
		ArrayList<DominatorNode> queue = new ArrayList<DominatorNode>();
		queue.add(root);
		while(!queue.isEmpty()){
			DominatorNode node = queue.get(0);
			queue.remove(0);
			for(Object n : node.getChildren()){
				queue.add((DominatorNode)n);
			}
			
			System.out.println();
			printDominatorSet(node);
		}
	}
	
	private static void printDominatorSet(DominatorNode node){
		DominatorNode parent = node.getParent();
		while(parent != null){
			
		}
		
	}
	
	private static void dynamicAnalysis(){
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

		@Override
		protected void internalTransform(Body arg0, String arg1, Map arg2) {
			//Dynamic Analysis (Instrumentation) code				
		}			
	   }));
	}
	
	public static void configure(String classpath) {		
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_java);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_soot_classpath(classpath);
        Options.v().set_prepend_classpath(true);
        Options.v().setPhaseOption("cg.spark", "on");        
    }
}
