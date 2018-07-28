// The name of a package is "coins.ssa"
package coins.ssa;

import coins.backend.LocalTransformer;

import java.util.ArrayList;
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
			BiLink prevNodel = null;
			for (BiLink nodel = bb.instrList().first(); !nodel.atEnd(); prevNodel = nodel, nodel = nodel.next()) {
				LirNode node = (LirNode) nodel.elem();
				if (prevNodel != null) {
					LirNode prevNode = (LirNode) prevNodel.elem();
					if (node.opCode == Op.SET && prevNode.opCode == Op.SET && prevNode.kid(1).opCode == Op.INTCONST) {
						for (int i = 0; i < node.kid(1).nKids(); i++) {
							if (node.kid(1).kid(i).equals(prevNode.kid(0))) {
								System.out.println(node.toString() + " is ");
								node.kid(1).setKid(i, prevNode.kid(1));
								System.out.println("\treplaced with " + node.toString());
							}
							for (int j = 0; j < node.kid(1).kid(i).nKids(); j++) {
								if (node.kid(1).kid(i).kid(j).equals(prevNode.kid(0))) {
									System.out.println(node.toString() + " is ");
									node.kid(1).kid(i).setKid(j, prevNode.kid(1));
									System.out.println("\treplaced with " + node.toString());
								}
							}
						}
					}
				}
				if (node.opCode == Op.SET) {
					node.setKid(1, env.lir.evalTree(node.kid(1)));
				}
			}
		}

		// If you have modified a control flow graph, you have to touch it.
		flow.touch();

		// The last of "doIt" returns true.
		return (true);

	}
}
