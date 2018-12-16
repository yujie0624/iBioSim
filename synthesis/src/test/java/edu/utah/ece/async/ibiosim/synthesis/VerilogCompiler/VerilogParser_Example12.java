package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogModule;


/**
 * 
 * @author Tramy Nguyen
 *
 */
public class VerilogParser_Example12 extends AbstractVerilogParserTest{

	private static VerilogModule verilog_imp;
	
	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", reader.getFile("contAssign.v")};
		
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		Map<String, VerilogModule> moduleList = compiledVerilog.getVerilogModules();
		Assert.assertEquals(1, moduleList.size());
		
		verilog_imp = moduleList.get("contAssign");
		Assert.assertNotNull(verilog_imp);
	}
	
	@Test
	public void Test_contAssign() {
		VerilogAssignment actual_assign = verilog_imp.getContinuousAssignment(0);
		Assert.assertEquals("t", actual_assign.getVariable());
		Assert.assertEquals("not(parity0)", actual_assign.getExpression());
	}
	
	
}