// The name of a package is "coins.ssa"
package coins.ssa;

import coins.backend.LocalTransformer;
import coins.backend.Data;
import coins.backend.Function;
import coins.backend.cfg.BasicBlk;
import coins.backend.util.BiLink;
import coins.backend.lir.LirNode;
import coins.backend.util.ImList;
import coins.backend.cfg.FlowGraph;

// Import coins.backend.Op, if you would like to refer kinds of operators.
import coins.backend.Op;

// Implement LocalTransformer
public class PeepHoleCSE implements LocalTransformer {

	private SsaEnvironment env;
	private SsaSymTab sstab;

	public PeepHoleCSE(SsaEnvironment e, SsaSymTab tab) {
		env = e;
		sstab = tab;
	}

	public String name() {
		return "PeepHole2";
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
				if (prevNodel != null) {
					LirNode node = (LirNode) nodel.elem();
					LirNode prevNode = (LirNode) prevNodel.elem();

					// X <- A * B
					if (node.opCode == Op.SET && prevNode.opCode == Op.SET) {
						if (node.kid(1).equals(prevNode.kid(1))) {
							boolean can = true;
							for (int i = 0; i < prevNode.kid(1).nKids(); i++) {
								if (prevNode.kid(1).kid(i).equals(prevNode.kid(0))) {
									can = false;
									break;
								}
							}
							if (can) {
								System.out.println(node.toString() + " is ");
								node.setKid(1, prevNode.kid(0).makeCopy(env.lir));
								System.out.println("\treplaced with " + node.toString());
							}
						}
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
