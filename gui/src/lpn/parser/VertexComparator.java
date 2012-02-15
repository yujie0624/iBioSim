package lpn.parser;

import java.util.Comparator;

public class VertexComparator implements Comparator<Vertex>{
	private int maxNumVarsInOneComp;
	
	public VertexComparator(Integer maxNumVarsInOneComp) {
		this.maxNumVarsInOneComp = maxNumVarsInOneComp;
	}

	public int compare(Vertex v1, Vertex v2) {
		if (v1.calculateBestNetGain(maxNumVarsInOneComp) >= v2.calculateBestNetGain(maxNumVarsInOneComp)) 
			return -1;
		else //if (v1.calculateBestNetGain(maxNumVarsInOneComp) > v2.calculateBestNetGain(maxNumVarsInOneComp)) 
			return 1;
//		else
//			return 0;
	}
}
