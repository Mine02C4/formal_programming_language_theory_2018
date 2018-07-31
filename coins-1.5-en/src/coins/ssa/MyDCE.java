// Worklist based solver for dataflow equations, which is using bitvector structure for recording dataflow information, but
// is not a bitvector method. 

package coins.ssa;

import coins.backend.LocalTransformer;
import coins.backend.Data;
import coins.backend.Function;
import coins.backend.cfg.*;
import coins.backend.lir.*;

import coins.backend.Op;

import coins.backend.util.*;

import java.util.Stack;
import java.util.Vector;

public class MyDCE implements LocalTransformer {

	private SsaEnvironment env;
	private SsaSymTab sstab;

	Stack<BiLink> works;

	public String name() {
		return "My Dead Code Elimination";
	}

	public String subject() {
		return "Optimizatin with DCE.";
	}

	public boolean doIt(Data data, ImList args) {
		return true;
	}

	public MyDCE(SsaEnvironment e, SsaSymTab tab) {
		env = e;
		sstab = tab;
	}

	private Function f;

	class PickUpUsedNode implements PickUpVariable {
		private Vector<LirNode> nlist;

		PickUpUsedNode() {
			nlist = new Vector<>();
		}

		public void meetVar(LirNode node) {
			if (!nlist.contains(node))
				nlist.addElement(node);
		}

		boolean isEmpty() {
			return nlist.isEmpty();
		}

		boolean contains(LirNode node) {
			return nlist.contains(node);
		}
	}

	boolean isUsed(LirNode target_node) {
		if (target_node.opCode == Op.SET) {
			LirNode tnode = target_node.kid(0);
			for (BiLink bb = f.flowGraph().basicBlkList.first(); !bb.atEnd(); bb = bb.next()) {
				BasicBlk v = (BasicBlk) bb.elem();
				for (BiLink nodel = v.instrList().first(); !nodel.atEnd(); nodel = nodel.next()) {
					LirNode node = (LirNode) nodel.elem();
					PickUpUsedNode un = new PickUpUsedNode();
					node.pickUpUses(un);
					if (!un.isEmpty() && un.contains(tnode)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	boolean isMemoryInst(LirNode node) {
		if (node.opCode == Op.SET) {
			if (node.kid(0).opCode == Op.MEM || node.kid(1).opCode == Op.MEM) {
				return true;
			}
		}
		return false;
	}

	public boolean doIt(Function function, ImList args) {
		works = new Stack<>();
		f = function;

		for (BiLink bb = f.flowGraph().basicBlkList.first(); !bb.atEnd(); bb = bb.next()) {
			BasicBlk v = (BasicBlk) bb.elem();
			for (BiLink nodel = v.instrList().first(); !nodel.atEnd(); nodel = nodel.next()) {
				works.push(nodel);
			}
		}
		while (!works.empty()) {
			BiLink nodel = works.pop();
			LirNode node = (LirNode) nodel.elem();
			// load, store, call, return, or branch
			if (isUsed(node) || node.isBranch() || node.opCode == Op.CALL || node.opCode == Op.EPILOGUE
					|| node.opCode == Op.PROLOGUE || isMemoryInst(node)) {
				continue;
			}
			nodel.unlink();
		}

		f.flowGraph().touch();
		return (true);
	}
}
