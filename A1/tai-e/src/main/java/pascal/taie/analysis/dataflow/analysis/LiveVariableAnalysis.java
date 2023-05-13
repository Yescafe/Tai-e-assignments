/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package pascal.taie.analysis.dataflow.analysis;

import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;

/**
 * Implementation of classic live variable analysis.
 */
public class LiveVariableAnalysis extends
        AbstractDataflowAnalysis<Stmt, SetFact<Var>> {

    public static final String ID = "livevar";

    public LiveVariableAnalysis(AnalysisConfig config) {
        super(config);
    }

    /**
     * The flowing of Live variable analysis is backward
     * @return false
     */
    @Override
    public boolean isForward() {
        return false;
    }

    /**
     * IN[exit] = empty
     * @param cfg
     * @return
     */
    @Override
    public SetFact<Var> newBoundaryFact(CFG<Stmt> cfg) {
        return new SetFact<>();
    }

    /**
     * IN[B] = empty
     * @return
     */
    @Override
    public SetFact<Var> newInitialFact() {
        return new SetFact<>();
    }

    /**
     * out[B] |= IN[S] which S is each successor of B
     * @param fact
     * @param target
     */
    @Override
    public void meetInto(SetFact<Var> fact, SetFact<Var> target) {
        fact.union(target);
    }

    /**
     * IN[B] = use_B | (OUT[B] - def_B)
     * => IN[B] |= use_B; IN[B] |= OUT[B] - def_B
     * @param stmt
     * @param in
     * @param out
     * @return whether `in` is changed
     */
    @Override
    public boolean transferNode(Stmt stmt, SetFact<Var> in, SetFact<Var> out) {
        var originalIn = in.copy();

        var uses = new SetFact<Var>();   // use_B
        System.out.println(stmt.getUses());
        for (var use : stmt.getUses()) {
            if (use instanceof Var) {
                uses.add((Var) use);
            }
        }

        var myOut = out.copy();    // out[B]
        if (stmt.getDef().isPresent()) {
            var lValue = stmt.getDef().get();
            if (lValue instanceof Var) {
                myOut.remove((Var) lValue);    // out[B] - def_B
            }
        }

        in.union(uses);       // in[B] |= use_B
        in.union(myOut);      // in[B] |= out[B] - def_B

        return !in.equals(originalIn);
    }
}
