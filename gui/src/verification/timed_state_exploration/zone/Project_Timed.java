package verification.timed_state_exploration.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lpn.parser.LhpnFile;
import verification.platu.logicAnalysis.Analysis;
import verification.platu.main.Options;
import verification.platu.project.Project;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

public class Project_Timed extends Project{

	/* The list for timing analysis */
	protected List<StateGraph_timed> designUnitTimedSet;
	private boolean _useGraph;
	
	public Project_Timed() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Project_Timed(ArrayList<LhpnFile> lpns) {
		super(lpns);
		// TODO Auto-generated constructor stub
	}

	/**
	 * If the OptionsFlag is false, then this constructor is identical to
	 * Poject(LhpnFile lpn). If the OptionsFlag is true, this constructor uses
	 * StateGraph_timed objects.
	 * 
	 * @author Andrew N. Fisher
	 * 
	 * @param lpn
	 * 			The lpn under consideration.
	 * @param OptionsFlag
	 * 			True for timing analysis and false otherwise. The option should match
	 * 			Options.getTimingAnalysisFlag().
	 */
	public Project_Timed(LhpnFile lpn, boolean OptionsFlag, boolean usegraph)
	{
		_useGraph = usegraph;
		
		if(Options.getTimingAnalysisFlag())
		{
			this.label = "";
			this.designUnitSet = new ArrayList<StateGraph>(0);
			this.designUnitTimedSet = new ArrayList<StateGraph_timed>(1);
			StateGraph_timed stategraph = new StateGraph_timed(lpn);
			designUnitTimedSet.add(stategraph);
		}
		else
		{
			this.label = "";
			this.designUnitSet = new ArrayList<StateGraph>(1);
			StateGraph stateGraph = new StateGraph(lpn);
			designUnitSet.add(stateGraph);
		}
	}

	public Project_Timed(LhpnFile lpn) {
		super(lpn);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void search() {	
		validateInputs();
		
//		if(Options.getSearchType().equals("compositional")){
//    		this.analysis = new CompositionalAnalysis();
//			
//			if(Options.getParallelFlag()){
//				this.analysis.parallelCompositionalFindSG(this.designUnitSet);
//			}
//			else{
//				this.analysis.findReducedSG(this.designUnitSet);
//			}
//			
//			return;
//		}
	    
		long start = System.currentTimeMillis(); 
		int lpnCnt = designUnitTimedSet.size();

		/* Prepare search by placing LPNs in an array in the order of their indices.*/
        StateGraph_timed[] sgArray = new StateGraph_timed[lpnCnt];
        int idx = 0;
//		for (StateGraph du : designUnitSet) {
//			LhpnFile lpn = du.getLpn();
//			lpn.setIndex(idx++);
//			sgArray[lpn.getIndex()] = du;
//		}
		
		// If timing, then create the sgArray with StateGraph_timed objects.
		if(Options.getTimingAnalysisFlag())
		{
			for(StateGraph_timed du : this.designUnitTimedSet)
			{
				LhpnFile lpn = du.getLpn();
				lpn.setLpnIndex(idx++);
				sgArray[lpn.getLpnIndex()] = du;
			}
		}

		// Initialize the project state
		HashMap<String, Integer> varValMap = new HashMap<String, Integer>();
		//TimedState[] initStateArray = new TimedState[lpnCnt];
		State[] initStateArray = new State[lpnCnt];
		
		for (int index = 0; index < lpnCnt; index++) {
			LhpnFile curLpn = sgArray[index].getLpn();
			StateGraph_timed curSg = sgArray[index];
			
			// TODO: Does this method need to be different than getInitState()
			initStateArray[index] = curSg.getInitStateTimed(_useGraph); //curLpn.getInitState();
			int[] curStateVector = initStateArray[index].getVariableVector();
			varValMap = curLpn.getAllVarsWithValuesAsInt(curStateVector);
//			HashMap<String, String> vars = curLpn.getAllOutputs();//curLpn.getAllOutputs();
//			DualHashMap<String, Integer> VarIndexMap = curLpn.getVarIndexMap();
//			for(String var : vars.keySet()) {
//				varValMap.put(var, curStateVector[VarIndexMap.getValue(var)]);
//			}
			
		}

		// TODO: (future) Need to adjust the transition vector as well?
		// Adjust the value of the input variables in LPN in the initial state.
		// Add the initial states into their respective LPN.
		for (int index = 0; index < lpnCnt; index++) {
			StateGraph_timed curSg = sgArray[index];
			initStateArray[index].update(curSg, varValMap, curSg.getLpn().getVarIndexMap());
			//initStateArray[index] = (TimedState)curSg.addState(initStateArray[index]);
			curSg.addState(((TimedState) initStateArray[index]).getState());
		}
		
		// Initialize the zones for the initStateArray, if timing is enabled.
		if(Options.getTimingAnalysisFlag())
		{
			for(int index =0; index < lpnCnt; index++)
			{
				if(sgArray[index] instanceof StateGraph_timed)
				{
					
				}
			}
		}
		
//		if (Options.getTimingAnalysisFlag()) {
//			new TimingAnalysis(sgArray); 
//			return;
//		}
//		else if(!Options.getTimingAnalysisFlag()) {
//			Analysis tmp = new Analysis(sgArray, initStateArray, lpnTranRelation, Options.getSearchType());
//			// Analysis tmp = new Analysis(lpnList, curStateArray,
//			// lpnTranRelation, "dfs_por");
//			//Analysis tmp = new Analysis(modArray, initStateArray, lpnTranRelation, "dfs");
//			//Analysis tmp = new Analysis(modArray, initStateArray, lpnTranRelation, "dfs_noDisabling");
//		}
//		else {
//			System.out.println("---> Error: wrong value for option 'timingAnalysis'");
//			return;
//		}
		
		/* Entry point for the timed analysis. */
		if(Options.getTimingAnalysisFlag())
		{
			Analysis_Timed dfsTimedStateExploration = new Analysis_Timed(sgArray);
			long startTimed = System.currentTimeMillis(); 
			// TODO: states not used
			//StateGraph_timed[] states = dfsTimedStateExploration.search_dfs_timed(sgArray, initStateArray);
			dfsTimedStateExploration.search_dfs_timed(sgArray, initStateArray);
			long elapsedTimeMillisForTimed = System.currentTimeMillis() - startTimed;
			System.out.println("---> totoal runtime: " + (double)elapsedTimeMillisForTimed/1000 + " sec\n");
			//return new StateGraph_timed[0];
			
		}
		
		Analysis dfsStateExploration = new Analysis(sgArray);
		//StateGraph[] stateGraphArray = dfsStateExploration.search_dfs(sgArray, initStateArray);
		dfsStateExploration.search_dfs(sgArray, initStateArray);
		
		long elapsedTimeMillis = System.currentTimeMillis() - start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		System.out.println("---> total runtime: " + elapsedTimeSec + " sec\n");
		
		//return stateGraphArray;
		
		//return new StateGraph_timed[0];
	}

}
