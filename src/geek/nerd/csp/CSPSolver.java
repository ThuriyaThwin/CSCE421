package geek.nerd.csp;

import java.io.IOException;



/*import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 */
import abscon.instance.tools.InstanceParser;

public class CSPSolver {

	public CSPSolver() {
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public static void main(String[] args) throws IOException{//, InvalidFormatException,  {
		InstanceParser parser = new InstanceParser();
		if (args.length == 0) {
			System.out.println("To Use The Solver, Please At Least Give A Instance File!");
			System.exit(1);
		}else if (args.length == 4) {
			parser.loadInstance(args[args.length-1]);
			parser.parse(true);
		}else {
			System.out.println("Please check your arguments.");
			System.exit(0);
		}

		System.out.println("===========================================");
		System.out.println("Problem Name: "+parser.getProblemName());
		System.out.println("File Name: " + args[args.length-1] );
		if (args[0].equalsIgnoreCase("-abt")){
			System.out.println("Running Vanilla BackTracking : ");
			BackTracking bt = new BackTracking(parser.getVariables(), parser.getConstraints());
			bt.sortVariables(args[1]);
			bt.startBackTracking();
		}else if (args[0].equalsIgnoreCase("-acbj")){
			System.out.println("Running CBJ : ");
			CBJ cbj = new CBJ(parser.getVariables(), parser.getConstraints());
			cbj.sortVariables(args[1]);
			cbj.startCBJ();
		}else if (args[0].equalsIgnoreCase("-aac1") || args[0].equalsIgnoreCase("-aac3")){
			ArcConsistency ac = new ArcConsistency(parser.getVariables(), parser.getConstraints(), args[args.length-1]);
			ac.startAC(args[0]);
		}else if (args[0].equalsIgnoreCase("-afc")) {
			if(args[1].equalsIgnoreCase("-udld") || args[1].equalsIgnoreCase("-uddeg") || args[1].equalsIgnoreCase("-uddd") || args[1].equalsIgnoreCase("-udlx")){
				System.out.println("Running Dynamic Forward Checking : ");
				DynamicFC dfc = new DynamicFC(parser.getVariables(), parser.getConstraints(), args[1], args[args.length-1]);
				dfc.startDynamicForwardChecking();
			}else{
				System.out.println("Running Forward Checking : ");
				ForwardChecking fc = new ForwardChecking(parser.getVariables(), parser.getConstraints());
				fc.sortVariables(args[1]);
				fc.startForwardChecking();
			}
		}else if (args[0].equalsIgnoreCase("-afccbj")) {
			System.out.println("Running FC-CBJ : ");
			if(args[1].equalsIgnoreCase("-udld") || args[1].equalsIgnoreCase("-uddeg") || args[1].equalsIgnoreCase("-uddd") || args[1].equalsIgnoreCase("-udlx")){
				DynamicFCCBJ dfccbj = new DynamicFCCBJ(parser.getVariables(), parser.getConstraints(), args[1], args[args.length-1]);
				dfccbj.startDynamicFCCBJ();
			}else{
				FCCBJ fccbj = new FCCBJ(parser.getVariables(), parser.getConstraints(), args[1]);
				fccbj.sortVariables(args[1]);
				fccbj.startFCCBJ();
			}
		}else if (args[0].equalsIgnoreCase("-aac2001")) {
			System.out.println("Running AC2001 : ");
			AC2001 ac2001 = new AC2001(parser.getVariables(), parser.getConstraints(), args[args.length-1]);
			ac2001.startAC2001();
		}else if (args[0].equalsIgnoreCase("-amac")) {
			System.out.println("Running MAC : ");
			if(args[1].equalsIgnoreCase("-udld") || args[1].equalsIgnoreCase("-uddeg") || args[1].equalsIgnoreCase("-uddd") || args[1].equalsIgnoreCase("-udlx")){
				FCMAC2001 fcmac2001 = new FCMAC2001(parser.getVariables(), parser.getConstraints(), args[1], args[args.length-1]);
				fcmac2001.startFCMAC2001();
			}else{
				System.out.println("Please check arguments!");
			}
		}else if (args[0].equalsIgnoreCase("-amaccbj")) {
			System.out.println("Running MAC-CBJ : ");
			if(args[1].equalsIgnoreCase("-udld") || args[1].equalsIgnoreCase("-uddeg") || args[1].equalsIgnoreCase("-uddd") || args[1].equalsIgnoreCase("-udlx")){
				FCCBJMAC2001 fcmac2001 = new FCCBJMAC2001(parser.getVariables(), parser.getConstraints(), args[1], args[args.length-1]);
				fcmac2001.startFCCBJMAC2001();
			}else{
				System.out.println("Please check arguments!");
			}
		}
		else {
			System.out.println("Please check your arguments.");
			System.exit(0);
		}
		System.out.println("Terminated.");
		System.out.println("===========================================");

	}

}
