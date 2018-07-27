// Worklist based solver for dataflow equations, which is using bitvector structure for recording dataflow information, but
// is not a bitvector method. 


package coins.ssa;

import coins.backend.LocalTransformer;
import coins.backend.Data;
import coins.backend.Function;
import coins.backend.cfg.*;
import coins.backend.lir.*;

import coins.backend.Op;
import coins.backend.Type;
import coins.backend.sym.Symbol;

import coins.backend.util.*;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import coins.ssa.BitVector;


public class AV implements LocalTransformer {

    private SsaEnvironment env;
    private SsaSymTab sstab;

    // A prefix for temoraries
    private String prefixOfTmp = "_av";

    public String name() {
	return "AV";
    }

    public String subject() {
	return "Optimizatin with AV.";
    }

    public boolean doIt(Data data, ImList args) {
	return true;
    }

    public AV(SsaEnvironment e, SsaSymTab tab) {
	env = e;
	sstab = tab;
    }

    private Function f;

    // Util includes convenient methods
    private Util util;

    // The variable is used for determining a bit location of each expression on
    // For getting the bit location corresponding to a LirNode
    Hashtable id2exp;
    // For getting the LirNode corresponding to a bit location
    private Vector exp2id;
    // For recording MEM nodes
    private Vector memq;

    // BitVector represents a bit-vector.
    // In addition to methods getBit(i) extracting value of ith bit as a integer value,
    // setBit(i) setting ith bit to 1, and resetBit(i) setting ith bit to 0,
    // you can use method vectorAnd for "and" operation, vectorOr for "or"
    // operation, vectorNot for "not" operation
    BitVector[] gen;
    BitVector[] kill;
    BitVector[] in;
    BitVector[] out;

    // For getting the temporary corresponding to a bit location of an expression
    Vector tmpPool;

    // Used as a worklist
    private Stack stack;

    // Performing the followings while searching all the expressions:
    // 1. determining a bit location of each expression,
    // and recording an association between the expression and the bit location in
    // id2exp and exp2id respectively.
    // 2. splitting x = e into t = e; x = t introducing a temporary t, which is
    // recorded in tempPool.
    void collectExp() {
	for (BiLink bbl = f.flowGraph().basicBlkList.first(); !bbl.atEnd(); bbl = bbl.next()) {
	    BasicBlk bb = (BasicBlk) bbl.elem();

	    for (BiLink nodel = bb.instrList().first(); !nodel.atEnd(); nodel = nodel.next()) {
		LirNode node = (LirNode) nodel.elem();

		// Our target expression if the right hand side of each assignment statement such as 
		// (SET (...) e)
		if (node.opCode == Op.SET) {
		    if (node.kid(0).opCode == Op.REG
			&& node.kid(1).opCode != Op.INTCONST
			&& node.kid(1).opCode != Op.FLOATCONST
			&& node.kid(1).opCode != Op.REG
			&& node.kid(1).opCode != Op.CALL
			&& node.kid(1).opCode != Op.USE
			&& node.kid(1).opCode != Op.CLOBBER) {
			// The right-hand side expression of node is used as a key of some tables.
			LirNode exp = node.kid(1);

			// Getting a bit location from an expression. If it has not been set yet, set it.
			Integer id = (Integer) id2exp.get(exp);
			if (id == null) {
			    id = new Integer(exp2id.size());
			    id2exp.put(exp, id);
			    exp2id.add(exp);

			    // Generateing the temporary symbol for expression exp through the newSsaSymbol of SsaSymTab, and
			    // record it in tmpPool. Symbol is a kind of string, but it is much faster to compare the Symbols.
			    Symbol tmpSym = sstab.newSsaSymbol(prefixOfTmp, exp.type);
			    tmpPool.add(tmpSym);
			}

			// If the expression is MEM, its id is also recorded in memq.
			// all MEM expressions in memq have to be killed
			//    as soon as store operation (SET (MEM ...) (...)) appears.
			if (exp.opCode == Op.MEM)
			    memq.add(id);

			// Splitting x = e into t = e; x = t
			// Generating REG LirNode from Symbol corresponding to the temporary
			Symbol sym = (Symbol) tmpPool.elementAt(id.intValue());
			LirNode reg = env.lir.symRef(Op.REG, exp.type, sym, ImList.Empty);

			LirNode clone = node.makeCopy(env.lir);
			clone.setKid(0, reg);
			node.setKid(1, reg.makeCopy(env.lir));
			nodel.addBefore(clone);
		    }
		}
	    }
	}
    }

    // Composing gen and kill of a basic block from gen and kill of each
    // statement
    // In addition, common sub-expressions are eliminated within the basic block.
    void initGenKill(BasicBlk bb, BitVector gen, BitVector kill) {

	for (BiLink nodel = bb.instrList().first(); !nodel.atEnd(); nodel = nodel.next()) {
	    LirNode node = (LirNode) nodel.elem();

	    switch (node.opCode) {
	    case Op.SET:
		// (SET (REG ...) (...)):
		if (node.kid(0).opCode == Op.REG) {
		    boolean isRemoved = false;
		    if (node.kid(1).opCode != Op.INTCONST
			&& node.kid(1).opCode != Op.FLOATCONST
			&& node.kid(1).opCode != Op.REG
			&& node.kid(1).opCode != Op.CALL
			&& node.kid(1).opCode != Op.USE
			&& node.kid(1).opCode != Op.CLOBBER) {
			LirNode exp = node.kid(1);
			Integer id = (Integer) id2exp.get(exp);
			int index = id.intValue();

			// If gen has been set already,
			// eliminate a node because the node is a common
			// sub-expression; Otherwise, set gen.
			if (gen.getBit(index) == 1) {
			    isRemoved = true;
			    nodel.unlink();
			} else {
			    gen.setBit(index);
			}
		    }
		    // Search expressions including a variable assigned by the node,
		    // and then, set kills of the expressions, and reset gens of them
		    for (int i = 0; !isRemoved && i < exp2id.size(); i++) {
			LirNode exp = (LirNode) exp2id.elementAt(i);

			BiList operands = util.findTargetLir(exp, Op.REG,
								 new BiList());
			for (BiLink q = operands.first(); !q.atEnd(); q = q.next()) {
			    LirNode reg = (LirNode) q.elem();
			    Symbol regSym = ((LirSymRef) reg).symbol;
			    if (regSym == ((LirSymRef) node.kid(0)).symbol) {
				kill.setBit(i);
				gen.resetBit(i);
			    }
			}
		    }
		}
		// (SET (MEM ...) (...)):
		// Set kill and reset gen for all MEM expressions.
		if (node.kid(0).opCode == Op.MEM) {
		    for (int i = 0; i < memq.size(); i++) {
			Integer memId = (Integer) memq.elementAt(i);
			kill.setBit(memId.intValue());
			gen.resetBit(memId.intValue());
		    }
		}
		break;
	    case Op.CALL:
		// (CALL ...):
		// Set kills of all the MEM expressions, and reset gens of them
		for (int i = 0; i < memq.size(); i++) {
		    Integer index = (Integer) memq.elementAt(i);
		    kill.setBit(index.intValue());
		    gen.resetBit(index.intValue());
		}

		// If the CALL expressions has a return value, and assigns it to a variable,
		// set kills of all expressions including the variable, and
		// reset gens of them.
		if (node.kid(2).nKids() > 0
		    && node.kid(2).kid(0).opCode == Op.REG) {
		    Symbol returnS = ((LirSymRef) node.kid(2).kid(0)).symbol;

		    for (int i = 0; i < exp2id.size(); i++) {
			LirNode exp = (LirNode) exp2id.elementAt(i);
			BiList operands = util.findTargetLir(exp, Op.REG, new BiList());
			for (BiLink q = operands.first(); !q.atEnd(); q = q.next()) {
			    LirNode reg = (LirNode) q.elem();
			    Symbol regSym = ((LirSymRef) reg).symbol;
			    if (regSym == returnS) {
				kill.setBit(i);
				gen.resetBit(i);
			    }
			}
		    }
		}
	    }
	}
    }

    void init () {
	for(BiLink bb=f.flowGraph().basicBlkList.first();!bb.atEnd();bb=bb.next()){

	    BasicBlk v=(BasicBlk)bb.elem();

	    initGenKill(v, gen[v.id], kill[v.id]);

	    // Set all bits of "in" for each basic block except a start node.
	    if (v != f.flowGraph().entryBlk()) 
		in[v.id].vectorNot(in[v.id]);

	    // Calculate "out" from "in" for each basic block.
	    in[v.id].vectorSub(kill[v.id],out[v.id]);
	    gen[v.id].vectorOr(out[v.id], out[v.id]);

	    // If "out" includes bit value 0, push it on stack.
	    BitVector notOut = new BitVector(exp2id.size());
	    out[v.id].vectorNot(notOut);
	    if (!notOut.isEmpty()) {
		stack.push(v);
	    }
	}
    }
    

    void settle () {
	// pop a candidate to be processed from the stack, 
	while(!stack.empty()) {
	    BasicBlk v = (BasicBlk)stack.pop();
	    for(BiLink ss= v.succList().first();!ss.atEnd();ss=ss.next()){
		BasicBlk succ = (BasicBlk)ss.elem();
		// Propagating information to each successor
		for(BiLink pp= succ.predList().first();!pp.atEnd();pp=pp.next()){
			BasicBlk pred = (BasicBlk)pp.elem();	
			in[succ.id].vectorAnd(out[pred.id], in[succ.id]);	
		}			
		// To be precise, the For-statement is not necessary for the maximal solution of a dataflow equation.
		// Instead, it can be described as the folloing single statement:
		//
		// in[succ.id].vectorAnd(out[v.id], in[succ.id]);

		// Calculate "out" from "in" of the successor
		BitVector newOut = new BitVector(exp2id.size());
		in[succ.id].vectorSub(kill[succ.id], newOut);
		newOut.vectorOr(gen[succ.id], newOut);
		// If any changes exists in "out", 
		// the successor become a candidate to be processed.
		if (!out[succ.id].vectorEqual(newOut)) {
		    out[succ.id] = newOut;
		    stack.push(succ);
		}
	    }
	}
    }
	

    void update () {
	// Transform a program

	for(BiLink bb=f.flowGraph().basicBlkList.first();!bb.atEnd();bb=bb.next()){
	    BasicBlk v=(BasicBlk)bb.elem();

	    initGenKill(v, in[v.id], new BitVector(exp2id.size()));
	}
    }


    public boolean doIt(Function function,ImList args) {

	f = function;
	util=new Util(env,f);

	stack = new Stack();
	id2exp = new Hashtable();
	exp2id = new Vector();
	memq = new Vector();
	tmpPool = new Vector();
	
	int idBound = f.flowGraph().idBound();
	kill = new BitVector[idBound];
	gen = new BitVector[idBound];
	in = new BitVector[idBound];
	out = new BitVector[idBound];;

	collectExp();

	for (int i = 0; i < f.flowGraph().idBound(); i++) {
	    gen[i] = new BitVector(exp2id.size());
	    kill[i] = new BitVector(exp2id.size());
	    in[i] = new BitVector(exp2id.size());
	    out[i] = new BitVector(exp2id.size());
	}

	init();
	settle();
	update();

	f.flowGraph().touch();
	return(true);
    }
}

