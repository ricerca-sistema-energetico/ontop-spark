package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;


public class DefaultDBStrEndsWithFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    /**
     * TODO: type the input
     */
    protected DefaultDBStrEndsWithFunctionSymbol(DBTermType metaDBTermType, DBTermType dbBooleanTermType) {
        super("STR_ENDS_WITH", ImmutableList.of(metaDBTermType, metaDBTermType), dbBooleanTermType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException("DefaultDBStrEndsWithFunctionSymbol blocks negation");
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableTerm secondTerm = terms.get(1);

        // TODO: use a non-strict equality
        return termConverter.apply(
                termFactory.getStrictEquality(
                        termFactory.getDBRight(
                                terms.get(0),
                                termFactory.getDBCharLength(secondTerm)),
                        secondTerm));
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * TODO: allows it
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
