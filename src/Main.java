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
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.JastAddJ.Signatures;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
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
		Scene.v().addBasicClass(EdgeProfiling.class.getName());
		soot.Main.main(args);

	}

	private static void staticAnalysis(){
		//Static Analysis code
		
		configure("/home/aditya/Downloads/CS201Profiling/Analysis"); //Change this path to your Analysis folder path in your project directory
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
	
	private static Stmt returnUnit; 
	private static void dynamicAnalysis(){
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {
			 
			boolean addedFieldToMainClassAndLoadedPrintStream = false;
			boolean flag = true;
			private SootClass javaIoPrintStream;
			private SootClass edgeProfiling = Scene.v().loadClassAndSupport("EdgeProfiling");
			private SootMethod increase = edgeProfiling.getMethodByName("increase");
			
			
		@Override
		protected void internalTransform(Body arg0, String arg1, Map arg2) {
			//Dynamic Analysis (Instrumentation) code
			
			//tmpRef is object for printing
			Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
			Scene.v().getMainMethod().getActiveBody().getLocals().add(tmpRef);
			
			//tmpPrintLong is object for holding long value before printing
			Local tmpPrintLong = Jimple.v().newLocal("tmpPrintLong", LongType.v());
    		Scene.v().getMainMethod().getActiveBody().getLocals().add(tmpPrintLong);
    		
	      //Executing Main
			if(arg0.getMethod().getName().equals("main")){
				SootField currentBlock = new SootField("currentBlock", LongType.v());
				SootField prev = new SootField("prevBlock", LongType.v());
				Iterator<Unit> stmtIt = arg0.getUnits().iterator();
				while (stmtIt.hasNext())
				 	{
					Stmt s = (Stmt) stmtIt.next();
					if(s instanceof ReturnStmt || s instanceof ReturnVoidStmt){
						returnUnit = s;
					}
					}
				
			}
			
			ExceptionalBlockGraph blockGraph = new ExceptionalBlockGraph(arg0);
	    	List<Block> blocks = blockGraph.getBlocks();
	    	//System.out.println("Method: "+blockGraph.getBody().getMethod().getName()+" :");
	    	//System.out.println("---V---");
	    	
	    	//Making fields for counting and printing
	    	SootField[] gotoCounter = new SootField[blocks.size()];
		    SootMethod toCall = null;
		    SootMethod tolong = null;
		    SootMethod toPrint = null;
		    
		    toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");	
    		tolong = Scene.v().getMethod("<java.io.PrintStream: void println(long)>");
    		toPrint= Scene.v().getMethod("<java.io.PrintStream: void print(java.lang.String)>");
		        
    		  //Putting a new variable tmpLocal
    		Local tmpLocal = Jimple.v().newLocal("tmp", LongType.v());
            arg0.getLocals().add(tmpLocal);

    		//Create another local to hold String.valueOf
    		Local tmpStr = Jimple.v().newLocal("tmpStr", RefType.v("java.lang.String"));
    		arg0.getLocals().add(tmpStr);
    		
	     // Add code at the end of the main method to print out the 
	        // gotoCounter (this only works in simple cases, because you may have multiple returns or System.exit()'s )
	        synchronized(this)
	        {
	            if (!Scene.v().getMainClass().
	                    declaresMethod("void main(java.lang.String[])"))
	                throw new RuntimeException("couldn't find main() in mainClass");

	                for(int i =0; i<blocks.size(); i++){
	                	
	                	//gotoCounter is shootfield for the static variable
	                gotoCounter[i] = new SootField("_"+String.valueOf(blockGraph.getBody().getMethod().getNumber())+"_"+String.valueOf(i), LongType.v(),Modifier.STATIC);
	                Scene.v().getMainClass().addField(gotoCounter[i]);

	                //aditya
	                InvokeExpr increaseExp = Jimple.v().newStaticInvokeExpr(increase.makeRef(), StringConstant.v(arg0.getMethod().getSignature()), IntConstant.v(i));
	                Stmt increaseStmt = Jimple.v().newInvokeStmt(increaseExp);
	                //aditya
	                
	                if(arg0.getMethod().getName().equals("main")&& i == 0){
	                	
	                	//tmpLocal = _1_0
	                	Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt(tmpLocal, 
	                        Jimple.v().newStaticFieldRef(gotoCounter[0].makeRef())),returnUnit);
	                	//tmpLocal = tmpLocal+1
	                	Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt(tmpLocal,
	 	    				Jimple.v().newAddExpr(tmpLocal, LongConstant.v(1L))),returnUnit);
	                	//_1_0 = tmpLocal
	                	Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef
	 	    				(gotoCounter[0].makeRef()),tmpLocal),returnUnit);
	                }
	                //This assigns tempref to print

	                Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt
	                		(tmpRef, Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),returnUnit);
	              //Print the Name of the method on Start of each block
	                if(i==0)
	                	Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr
			    				(tmpRef, toCall.makeRef(),StringConstant.v("Method:"+arg0.getMethod().getName()))),returnUnit);
	                //One time execution for the Main
	                if(arg0.getMethod().getName().equals("main")&& i == 0){
	                	
	                	//Print the method Name first:
	                	
	                	
	                	//tmpLocal = _1_0
	                	Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt(tmpLocal, 
	                        Jimple.v().newStaticFieldRef(gotoCounter[0].makeRef())),returnUnit);
	                	//tmpLocal = tmpLocal+1
	                	Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt(tmpLocal,
	 	    				Jimple.v().newAddExpr(tmpLocal, LongConstant.v(1L))),returnUnit);
	                	//_1_0 = tmpLocal
	                	Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef
	 	    				(gotoCounter[0].makeRef()),tmpLocal),returnUnit);
	                }
	                
	                //This assigns long variables to static longs
	                Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newAssignStmt
	                		(tmpPrintLong, Jimple.v().newStaticFieldRef(gotoCounter[i].makeRef())),returnUnit);

	              //Now loop through all the variables so we can print them

	              
	               //This prints the block number
	                Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr
		    				(tmpRef, toPrint.makeRef(),StringConstant.v("b"+i+":"))),returnUnit);
	                
	                //This part prints the Long Values:
		    		Scene.v().getMainMethod().getActiveBody().getUnits().insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr
		    				(tmpRef, tolong.makeRef(),tmpPrintLong)),returnUnit);
	                }
	                Scene.v().loadClassAndSupport("java.io.PrintStream");
	                Scene.v().loadClassAndSupport("java.lang.System");
	                Scene.v().loadClassAndSupport("EdgeProfiling");
	                javaIoPrintStream = Scene.v().getSootClass("java.io.PrintStream");
	                addedFieldToMainClassAndLoadedPrintStream = true;    
	        }
    		
	    	//Iterating through blocks
	    	//for(Block b : blocks)
	    	for(int j = 0; j<blocks.size();j++)	
	    	{
	    		Unit bTail = blocks.get(j).getTail();// This gives us tail unit.
	    		Unit bhead = blocks.get(j).getHead();//this gives us head unit
	    		
	    		 InvokeExpr increaseExp = Jimple.v().newStaticInvokeExpr(increase.makeRef(), 
	    				 StringConstant.v(arg0.getMethod().getSignature()), IntConstant.v(j));
	                Stmt increaseStmt = Jimple.v().newInvokeStmt(increaseExp);
	    		
	    		AssignStmt toAdd1 = Jimple.v().newAssignStmt(tmpLocal, 
                       Jimple.v().newStaticFieldRef(gotoCounter[j].makeRef() ));
	    		AssignStmt toAdd2 = Jimple.v().newAssignStmt(tmpLocal,
	    				Jimple.v().newAddExpr(tmpLocal, LongConstant.v(1L)));
	    		AssignStmt toAdd3 = Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef
	    				(gotoCounter[j].makeRef()),tmpLocal);
	    		
	    		//inserting increase statement in each block's head
	    		blocks.get(j).insertAfter(increaseStmt, bhead);
	    	
	    	
	    		// insert "tmpLocal = gotoCounter;"
	    		blocks.get(j).insertBefore(toAdd1, bTail);
           
	    		// insert "tmpLocal = tmpLocal + 1L;" 
	    		blocks.get(j).insertBefore(toAdd2, bTail);

	    		//insert "gotoCounter = tmpLocal;" 
	    		blocks.get(j).insertBefore(toAdd3, bTail);	
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
