package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.AscendingSubstitutionNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

/**
 * TODO: find a better name
 */
public class AscendingSubstitutionNormalizerImpl implements AscendingSubstitutionNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private AscendingSubstitutionNormalizerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }

    /**
     * Prevents creating construction nodes out of ascending substitutions
     *
     * Here, variable nullability is not considered due to the complexity induced by the descending substitution
     *
     */
    @Override
    public AscendingSubstitutionNormalization normalizeAscendingSubstitution(
            ImmutableSubstitution<ImmutableTerm> ascendingSubstitution, ImmutableSet<Variable> projectedVariables) {

        Var2VarSubstitution downRenamingSubstitution = substitutionFactory.getVar2VarSubstitution(
                ascendingSubstitution.getImmutableMap().entrySet().stream()
                        .filter(e -> e.getValue() instanceof Variable)
                        .map(e -> Maps.immutableEntry(e.getKey(), (Variable) e.getValue()))
                        .filter(e -> !projectedVariables.contains(e.getValue()))
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (v1, v2) -> v1)));

        ImmutableSubstitution<ImmutableTerm> newAscendingSubstitution = downRenamingSubstitution
                .composeWith(ascendingSubstitution)
                .reduceDomainToIntersectionWith(projectedVariables)
                .normalizeValues(true);

        return new AscendingSubstitutionNormalizationImpl(newAscendingSubstitution, downRenamingSubstitution,
                projectedVariables);
    }


    public class AscendingSubstitutionNormalizationImpl implements AscendingSubstitutionNormalization {

        private final ImmutableSubstitution<ImmutableTerm> ascendingSubstitution;
        private final Var2VarSubstitution downRenamingSubstitution;
        private final ImmutableSet<Variable> projectedVariables;

        private AscendingSubstitutionNormalizationImpl(ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
                                                   Var2VarSubstitution downRenamingSubstitution,
                                                   ImmutableSet<Variable> projectedVariables) {
            this.ascendingSubstitution = ascendingSubstitution;
            this.downRenamingSubstitution = downRenamingSubstitution;
            this.projectedVariables = projectedVariables;
        }

        @Override
        public Optional<ConstructionNode> generateTopConstructionNode() {
            return Optional.of(ascendingSubstitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> iqFactory.createConstructionNode(projectedVariables, s));

        }

        @Override
        public IQTree updateChild(IQTree child) {
            return downRenamingSubstitution.isEmpty()
                    ? child
                    : child.applyDescendingSubstitution(downRenamingSubstitution, Optional.empty());
        }

        @Override
        public ImmutableExpression updateExpression(ImmutableExpression expression) {
            return downRenamingSubstitution.isEmpty()
                    ? expression
                    : downRenamingSubstitution.applyToBooleanExpression(expression);
        }

        @Override
        public ImmutableSubstitution<ImmutableTerm> getAscendingSubstitution() {
            return ascendingSubstitution;
        }
    }
}