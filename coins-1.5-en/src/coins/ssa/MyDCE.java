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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import coins.ssa.BitVector;

public class MyDCE implements LocalTransformer {

	private SsaEnvironment env;
	private SsaSymTab sstab;

	// A prefix for temoraries
	private String prefixOfTmp = "_av";
	
	HashSet<LirNode> available_node;

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

	// Util includes convenient methods
	private Util util;


	// For recording MEM nodes
	private Vector<Integer> memq;

	// BitVector represents a bit-vector.
	// In addition to methods getBit(i) extracting value of ith bit as a integer
	// value,
	// setBit(i) setting ith bit to 1, and resetBit(i) setting ith bit to 0,
	// you can use method vectorAnd for "and" operation, vectorOr for "or"
	// operation, vectorNot for "not" operation
	BitVector[] gen;
	BitVector[] kill;
	BitVector[] in;
	BitVector[] out;

	// For getting the temporary corresponding to a bit location of an expression
	Vector<Symbol> tmpPool;

	boolean IsTargetExpression(LirNode exp) {
		return exp.opCode != Op.INTCONST && exp.opCode != Op.FLOATCONST && exp.opCode != Op.REG && exp.opCode != Op.CALL
				&& exp.opCode != Op.USE && exp.opCode != Op.CLOBBER;
	}

	// Performing the followings while searching all the expressions:
	// 1. determining a bit location of each expression,
	// and recording an association between the expression and the bit location in
	// id2exp and exp2id respectively.
	// 2. splitting x = e into t = e; x = t introducing a temporary t, which is
	// recorded in tempPool.
	void collectExp() {
	}

	// Composing gen and kill of a basic block from gen and kill of each
	// statement
	// In addition, common sub-expressions are eliminated within the basic block.
	void initGenKill(BasicBlk bb, BitVector gen, BitVector kill) {
	}

	void init() {
		for (BiLink bb = f.flowGraph().basicBlkList.first(); !bb.atEnd(); bb = bb.next()) {
			BasicBlk v = (BasicBlk) bb.elem();
			initGenKill(v, gen[v.id], kill[v.id]);

			// Set all bits of "in" for each basic block except a start node.
			if (v != f.flowGraph().entryBlk())
				in[v.id].vectorNot(in[v.id]);

			// Calculate "out" from "in" for each basic block.
			in[v.id].vectorSub(kill[v.id], out[v.id]);
			gen[v.id].vectorOr(out[v.id], out[v.id]);
		}
	}

	void update() {
		// Transform a program

		for (BiLink bb = f.flowGraph().basicBlkList.first(); !bb.atEnd(); bb = bb.next()) {
			BasicBlk v = (BasicBlk) bb.elem();

		}
	}

	public boolean doIt(Function function, ImList args) {

		f = function;
		util = new Util(env, f);

		memq = new Vector<Integer>();
		tmpPool = new Vector<Symbol>();

		try {
			collectExp();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		int idBound = f.flowGraph().idBound();
		kill = new BitVector[idBound];
		gen = new BitVector[idBound];
		in = new BitVector[idBound];
		out = new BitVector[idBound];

		init();
		update();

		f.flowGraph().touch();
		return (true);
	}
}

