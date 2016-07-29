package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

/**
 * Proposal to apply a substitution to a focus node and to propagate it down and up.
 *
 * Assumption: the substitution must be directly applicable to the focus node
 * (i.e. the focus node should not reject it).
 *
 *
 */
public interface SubstitutionPropagationProposal<T extends QueryNode> extends SimpleNodeCentricOptimizationProposal<T> {

    ImmutableSubstitution<? extends ImmutableTerm> getSubstitution();

}
