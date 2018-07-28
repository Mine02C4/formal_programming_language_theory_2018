// The name of a package is "coins.ssa"
package coins.ssa;

import coins.backend.LocalTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coins.backend.Data;
import coins.backend.Function;
import coins.backend.cfg.BasicBlk;
import coins.backend.util.BiLink;
import coins.backend.lir.LirIconst;
import coins.backend.lir.LirNode;
import coins.backend.util.ImList;
import coins.backend.cfg.FlowGraph;

// Import coins.backend.Op, if you would like to refer kinds of operators.
import coins.backend.Op;

// Implement LocalTransformer
public class ConstantFolding implements LocalTransformer {

	private SsaEnvironment env;
	private SsaSymTab sstab;

	public ConstantFolding(SsaEnvironment e, SsaSymTab tab) {
		env = e;
		sstab = tab;
	}

	public String name() {
		return "ConstantFolding";
	}

	public String subject() {
		return "Simple optimizer using peephole approach";
	}

	public boolean doIt(Data data, ImList args) {
		return true;
	}

	public boolean doIt(Function function, ImList args) {
		// making a control graph.
		FlowGraph flow = function.flowGraph();

		for (BiLink bbl = flow.basicBlkList.first(); !bbl.atEnd(); bbl = bbl.next()) {
			BasicBlk bb = (BasicBlk) bbl.elem();

			// Two continuous statements, "prevNode" and "node", are considered as a
			// peephole,
			// where prevNode records an immediately previous node of the node
			BiLink prevNodel = null;
			for (BiLink nodel = bb.instrList().first(); !nodel.atEnd(); prevNodel = nodel, nodel = nodel.next()) {
				if (prevNodel != null) {
					LirNode node = (LirNode) nodel.elem();
					LirNode prevNode = (LirNode) prevNodel.elem();

					@SuppressWarnings("serial")
					List<Integer> targetOps = new ArrayList<Integer>() {
						{
							add(Op.ADD);
							add(Op.SUB);
							add(Op.MUL);
						}
					};
					// X <- A * B
					// If A and B are constant
					if (node.opCode == Op.SET && targetOps.contains(node.kid(1).opCode)
							&& (node.kid(1).kid(0).opCode == Op.INTCONST)
							&& (node.kid(1).kid(1).opCode == Op.INTCONST)) {
						LirIconst lconst = (LirIconst) node.kid(1).kid(0);
						LirIconst rconst = (LirIconst) node.kid(1).kid(1);
						long val;
						switch (node.kid(1).opCode) {
						case Op.ADD:
							val = lconst.value + rconst.value;
							break;
						case Op.SUB:
							val = lconst.value - rconst.value;
							break;
						case Op.MUL:
							val = lconst.value * rconst.value;
							break;
						default:
							return false;
						}
						LirNode vconst = env.lir.iconst(lconst.type, val);
						System.out.println(node.toString() + " is ");
						node.setKid(1, vconst);
						System.out.println("\treplaced with " + node.toString());
					}
				}
			}
		}

		// If you have modified a control flow graph, you have to touch it.
		flow.touch();

		// The last of "doIt" returns true.
		return (true);

	}
}
