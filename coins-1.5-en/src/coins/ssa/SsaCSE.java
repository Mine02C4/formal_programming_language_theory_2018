// The alogrithm takes advantage of the following SSA form properties:
// 1. The expressions with the same lexically form, which has the same operator and operands, generate
//    the same value.
// 2. If a statement y=e2 is dominated by another statement x=e1  and e1 and e2 have the same lexically form, 
//    e2 is redundant for e1.  
//    Notice that e2 is always executed whenever e1 is executed for their dominance relation.
// 3. Because x=e1 dominates y=e2, e2 can be replaced with x. 
//
// The algorithm propagates to the current node the tables with expressions at its dominators,
// where each table handles expressions at a single node.
// The propagation can be achieved in preorder of depth first search on a dominator tree, and therefore, 
// the tables can be operated in a stack.
// Notice that the table at a node for which the operation are completed has to be poped from the stack
// when returning to its parent node.


package coins.ssa;

import coins.backend.LocalTransformer;
import coins.backend.Data;
import coins.backend.Function;
import coins.backend.cfg.*;
import coins.backend.ana.Dominators;
import coins.backend.lir.*;
import coins.backend.Op;

import coins.backend.util.*;


import java.util.Hashtable;
import java.util.Stack;


public class SsaCSE implements LocalTransformer {

    private SsaEnvironment env;
    private SsaSymTab sstab;

    // Map including expressions
    private Stack<Hashtable<LirNode,LirNode>> maps;
    private Dominators dom;

    public String name() {
	return "SsaCSE";
    }

    public String subject() {
	return "Simple CSE based on SSA form.";
    }

    public boolean doIt(Data data, ImList args) {
	return true;
    }

    public SsaCSE(SsaEnvironment e, SsaSymTab tab) {
	env = e;
	sstab = tab;
    }


    private void visit(BasicBlk bb) {
	// Generate a table for the current node, pusing it on the stack.
	maps.push(new Hashtable<LirNode,LirNode>());

	for (BiLink nodel = bb.instrList().first(); !nodel.atEnd(); nodel = nodel.next()) {
	    LirNode node = (LirNode) nodel.elem();
	    if (node.opCode == Op.SET && node.kid(0).opCode == Op.REG) {
		LirNode dst = node.kid(0);
		LirNode expr = node.kid(1);
		if (expr.opCode != Op.INTCONST
		    && expr.opCode != Op.FLOATCONST
		    && expr.opCode != Op.REG
		    && expr.opCode != Op.CALL
		    && expr.opCode != Op.USE
		    && expr.opCode != Op.CLOBBER
                    // The references to the same memory location are regarded as the same expression
                    // in SSA form, even if they give different values. 
		    && expr.opCode != Op.MEM) {
			
		    LirNode var = null;
		    // Check all the tables in the stack.
		    for(Hashtable<LirNode,LirNode> map: maps) {
			var = map.get(expr);
			if (var != null) break;
		    }
		    if (var != null) 
			node.setKid(1, var.makeCopy(env.lir));
		    else
			maps.peek().put(expr, dst);
		}
	    }
	}

	//Visit all the children of the current node
	for(BiLink p=dom.kids[bb.id].first();!p.atEnd();p=p.next()){
	    BasicBlk child = (BasicBlk)p.elem();
	    visit(child);
	}
	//Pop the table for the current node. 
	maps.pop();
    }

    public boolean doIt(Function function,ImList args) {
	dom=(Dominators)function.require(Dominators.analyzer);
	maps = new Stack<Hashtable<LirNode,LirNode>>();

	// Apply visit to the root of a dominator tree. 
	visit(function.flowGraph().entryBlk());

	function.flowGraph().touch();
	return(true);
    }
}
