package verification.platu.expression;

import java.util.HashMap;
import java.util.HashSet;

public class NotEquivNode implements ExpressionNode {
	ExpressionNode LeftOperand = null;
	ExpressionNode RightOperand = null;
	
	public NotEquivNode(ExpressionNode leftOperand, ExpressionNode rightOperand){
		this.LeftOperand = leftOperand;
		this.RightOperand = rightOperand;
	}
	
	@Override
	public int evaluate(int[] stateVector){
		if(LeftOperand.evaluate(stateVector) != RightOperand.evaluate(stateVector))
			return 1;
		
		return 0;
	}
	
	@Override
	public void getVariables(HashSet<VarNode> variables){
		LeftOperand.getVariables(variables);
		RightOperand.getVariables(variables);
	}
	
	@Override
	public String toString(){
		return LeftOperand.toString() + "!=" + RightOperand.toString();
	}
	
	@Override
	public ExpressionNode copy(HashMap<String, VarNode> variables){
		return new NotEquivNode(this.LeftOperand.copy(variables), this.RightOperand.copy(variables));
	}
}
