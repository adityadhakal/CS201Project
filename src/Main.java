import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import soot.ArrayType;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.options.Options;
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


public class Main {
	
	public static void main(String[] args) {
		
		//Static Analysis (Retrieve Flow Graph)
		staticAnalysis();
		
		//Dynamic Analysis (Instrumentation) 
		dynamicAnalysis();
 
		soot.Main.main(args);

	}

	private static void staticAnalysis(){
		//Static Analysis code
		
		configure("/home/hypothesis/workspace/CS201Project/Analysis"); //Change this path to your Analysis folder path in your project directory
		SootClass sootClass = Scene.v().loadClassAndSupport("Test1");
	    sootClass.setApplicationClass();
	    ArrayList<SootMethod> methods = (ArrayList<SootMethod>)sootClass.getMethods();
	    
	    
	    //Pred Succ 
	    for(SootMethod m : methods){
	    	System.out.println("Method:" + m);
	    	Body methodBody = m.retrieveActiveBody();
	    	ExceptionalBlockGraph blockGraph = new ExceptionalBlockGraph(methodBody);
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
	    	
	    	//Dominator Sets
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
	    	
	    	//Loops
	    	System.out.println("\nLoops:");
	    	
	    	for(Block b : blocks){
	    		List<Block> succs = b.getSuccs();
	    		List<Block> dom = finder.getDominators(b);
	    		for(Block succ: succs){
	    			if(succ.getIndexInMethod() <= b.getIndexInMethod())
	    				printLoop(b,succ);
	    		}
	    	}
	    	
	    } 
	}
	
	
	private static void printLoop(Block n, Block d){
		ArrayList<Block> stack = new ArrayList<>();
		ArrayList<Integer> loop = new ArrayList<>();
		loop.add(d.getIndexInMethod());
		loop.add(n.getIndexInMethod());
		stack.add(n);
		while(stack.size() > 0){
			Block m = stack.get(stack.size() - 1 );
			stack.remove(stack.size() - 1);
			for (Block p : m.getPreds()){
				if(!loop.contains(p.getIndexInMethod())){
					loop.add(p.getIndexInMethod());
					stack.add(m);
				}
			}
		}
		sort(loop);
		String loopResult = "[";
		for (int i:loop){
			loopResult += " "+i;
		}
		loopResult += " ]";
		System.out.println(loopResult);
	}
	
	private static void sort(ArrayList<Integer> list){
		
		int size = list.size();
		int[] array = new int[size] ;
		
		for(int i = 0 ; i < size ; i++){
			array[i] = list.get(i);
		}
		
		for (int i = 1 ; i < size ; i++){
			int key = array[i];
			int j = i - 1 ;
			while (j >= 0 && key < array[j]){
				array[j+1] = array[j];
				j--;
			}
			array[j+1] = key;
		}
		list.clear();
		for(int i = 0 ; i < size ; i++){
			list.add(array[i]);
		}
	}
	
	private static PatchingChain<Unit> returnUnit; 
	private static void dynamicAnalysis(){
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {
			 
			boolean addedFieldToMainClassAndLoadedPrintStream = false;
			boolean flag = true;
			private SootClass javaIoPrintStream;
			
		@Override
		protected void internalTransform(Body arg0, String arg1, Map arg2) {
			//Dynamic Analysis (Instrumentation) code	
	      //Making blocks!!!
			if(arg0.getMethod().getName().equals("main"))
				returnUnit = arg0.getUnits();
			
			ExceptionalBlockGraph blockGraph = new ExceptionalBlockGraph(arg0);
	    	List<Block> blocks = blockGraph.getBlocks();
	    	System.out.println("Method: "+blockGraph.getBody().getMethod().getName()+" :");
	    	System.out.println("---V---");
	    	
	    	//System.out.println("Block Size = "+blocks.size());
	    	//Making fields for counting and printing
	    	SootField[] gotoCounter = new SootField[blocks.size()];
		    SootMethod toCall = null;
		    SootMethod tolong = null;
		        
	     //   boolean addedLocals = false;
	     //   Local tmpRef = null, tmpLong = null;
	     // Add code at the end of the main method to print out the 
	        // gotoCounter (this only works in simple cases, because you may have multiple returns or System.exit()'s )
	        synchronized(this)
	        {
	            if (!Scene.v().getMainClass().
	                    declaresMethod("void main(java.lang.String[])"))
	                throw new RuntimeException("couldn't find main() in mainClass");

	                for(int i =0; i<blocks.size(); i++){
	                gotoCounter[i] = new SootField("_"+String.valueOf(blockGraph.getBody().getMethod().getNumber())+"_"+String.valueOf(i), LongType.v(),Modifier.STATIC);
	                Scene.v().getMainClass().addField(gotoCounter[i]);
	                }
	                Scene.v().loadClassAndSupport("java.io.PrintStream");
	                Scene.v().loadClassAndSupport("java.lang.System");
	                javaIoPrintStream = Scene.v().getSootClass("java.io.PrintStream");
	                addedFieldToMainClassAndLoadedPrintStream = true;    
	        }
	         
	        //Putting a new variable tmpLocal
    		Local tmpLocal = Jimple.v().newLocal("tmp", LongType.v());
            arg0.getLocals().add(tmpLocal);
            
            // Create a local to hold the PrintStream System.out
    		Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
    		arg0.getLocals().add(tmpRef);
    		
    		//Create another local to hold String.valueOf
    		Local tmpStr = Jimple.v().newLocal("tmpStr", RefType.v("java.lang.String"));
    		arg0.getLocals().add(tmpStr);
    		
    		
    		
	    	//Iterating through blocks
	    	//for(Block b : blocks)
	    	for(int j = 0; j<blocks.size();j++)	
	    	{
	    		Unit bTail = blocks.get(j).getTail();// This gives us tail unit.
	    		
	    		AssignStmt toAdd1 = Jimple.v().newAssignStmt(tmpLocal, 
                       Jimple.v().newStaticFieldRef(gotoCounter[j].makeRef() ));
	    		AssignStmt toAdd2 = Jimple.v().newAssignStmt(tmpLocal,
	    				Jimple.v().newAddExpr(tmpLocal, LongConstant.v(1L)));
	    		AssignStmt toAdd3 = Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef
	    				(gotoCounter[j].makeRef()),tmpLocal);
	    		
	    	
	    	
	    		// insert "tmpLocal = gotoCounter;"
	    		blocks.get(j).insertBefore(toAdd1, bTail);
           
	    		// insert "tmpLocal = tmpLocal + 1L;" 
	    		blocks.get(j).insertBefore(toAdd2, bTail);

	    		//insert "gotoCounter = tmpLocal;" 
	    		blocks.get(j).insertBefore(toAdd3, bTail);
	    		
	    		//System.out.println("."+blockGraph.getBody().getMethod().getName()+".");
	    		

	    		//This assigns the print object
	    		AssignStmt whatever = Jimple.v().newAssignStmt(tmpRef,Jimple.v().newStaticFieldRef(Scene.v().getField
	    				("<java.lang.System: java.io.PrintStream out>").makeRef()));
	    		
	    		//We need to print everything on Main Statement
	    		//Now the printing Statement In the end of the method
	    		//Adding print object
	    		blocks.get(j).insertBefore(whatever, bTail);
	    	}
		    		

		    		//This actually prints "tmpLocal" --- We need to print gotoCounter...
		    		toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");	
		    		tolong = Scene.v().getMethod("<java.io.PrintStream: void println(long)>");
		    		
		    		
		    	
		    		Chain units = arg0.getUnits();
		    		
		    		for(int k = 0; k<blocks.size();k++){
		    			
		    			// First get the tmpRef added. Let's add a lot of them
		    			//Putting a new local variable tmpPrintLong to Print everything at the end
		        		Local tmpPrintLong = Jimple.v().newLocal("tmpPrintLong"+k, LongType.v());
		        		arg0.getLocals().add(tmpPrintLong);
		    			
//		    		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(base, method)))	
		    			
		    		units.add(Jimple.v().newAssignStmt(tmpPrintLong, 
		                       Jimple.v().newStaticFieldRef(gotoCounter[k].makeRef() )));
//		    		//Now loop through all the variables so we can print them
		    		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr
		    				(tmpRef, tolong.makeRef(),tmpPrintLong)));
//		    	
//		    		InvokeStmt print_method_name = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr
//		    				(tmpRef, toCall.makeRef(),StringConstant.v(blocks.get(j).toString())));
//		    		
//		    		//Putting the assignment Operation
		    		//blocks.get(j).insertBefore(toAdd4, bTail);
//		    		//Printing the Method name
//		    		blocks.get(j).insertBefore(print_method_name,bTail);
//		    		
//		    		//Adding the print statement
		    		//blocks.get(j).insertBefore(print_long, bTail);
//		    		
		    		
		    		}
		    		
		    	
	    	
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
