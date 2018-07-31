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
public class PeepHoleDCE implements LocalTransformer {

	private SsaEnvironment env;
	private SsaSymTab sstab;

	public PeepHoleDCE(SsaEnvironment e, SsaSymTab tab) {
		env = e;
		sstab = tab;
	}

	public String name() {
		return "PeepHoleDCE";
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
					// Y <- C * D
					// if X == Y and X != C and X != D then delete X
					if (node.opCode == Op.SET && prevNode.opCode == Op.SET) {
						if (node.kid(0).equals(prevNode.kid(0))) {
							boolean can = true;
							for (int i = 0; i < node.kid(1).nKids(); i++) {
								if (prevNode.kid(0).equals(node.kid(1).kid(i))) {
									can = false;
									break;
								}
							}
							if (can) {
								System.out.println("Delete " + prevNode.toString());
								prevNodel.unlink();
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
