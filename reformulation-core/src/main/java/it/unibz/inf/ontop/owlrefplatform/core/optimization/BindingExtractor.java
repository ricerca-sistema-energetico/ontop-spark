package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNodeVisitor;

import java.util.Optional;

/**
 *  Retrieve bindings from the sub-tree,
 *  possible different implementations based on the search depth
 *
 *  TODO: explain
 */
public interface BindingExtractor {

    Optional<ImmutableSubstitution<ImmutableTerm>> extractInSubTree(IntermediateQuery query, QueryNode subTreeRootNode) ;

}