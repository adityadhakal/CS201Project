import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;


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
		SootClass sootClass = Scene.v().loadClassAndSupport("Test1");
	    sootClass.setApplicationClass();
	    //Static Analysis code
	    
	    
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
