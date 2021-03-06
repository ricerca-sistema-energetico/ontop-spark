package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class BinaryNonCommutativeIQTreeImpl extends AbstractCompositeIQTree<BinaryNonCommutativeOperatorNode>
        implements BinaryNonCommutativeIQTree {

    private final IQTree leftChild;
    private final IQTree rightChild;

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild, @Assisted("right") IQTree rightChild,
                                           @Assisted IQProperties iqProperties, IQTreeTools iqTreeTools,
                                           IntermediateQueryFactory iqFactory, TermFactory termFactory, OntopModelSettings settings) {
        super(rootNode, ImmutableList.of(leftChild, rightChild), iqProperties, iqTreeTools, iqFactory, termFactory);
        this.leftChild = leftChild;
        this.rightChild = rightChild;

        if (settings.isTestModeEnabled())
            validate();
    }

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild, @Assisted("right") IQTree rightChild,
                                           @Assisted IQTreeCache treeCache, IQTreeTools iqTreeTools,
                                           IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                           OntopModelSettings settings) {
        super(rootNode, ImmutableList.of(leftChild, rightChild), treeCache, iqTreeTools, iqFactory, termFactory);
        this.leftChild = leftChild;
        this.rightChild = rightChild;

        if (settings.isTestModeEnabled())
            validate();
    }

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild,
                                           @Assisted("right") IQTree rightChild,
                                           IQTreeTools iqTreeTools,
                                           IntermediateQueryFactory iqFactory,
                                           TermFactory termFactory,
                                           OntopModelSettings settings) {
        this(rootNode, leftChild, rightChild, iqFactory.createIQProperties(), iqTreeTools, iqFactory, termFactory, settings);
    }

    @Override
    public IQTree getLeftChild() {
        return leftChild;
    }

    @Override
    public IQTree getRightChild() {
        return rightChild;
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return getRootNode().acceptTransformer(this, transformer, leftChild, rightChild);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(visitor, leftChild, rightChild);
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        IQProperties properties = getProperties();
        if (properties.isNormalizedForOptimization())
            return this;
        return getRootNode().normalizeForOptimization(leftChild, rightChild, variableGenerator, properties);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, leftChild, rightChild, variableGenerator);
    }

    @Override
    protected IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, boolean alreadyNormalized) {
        InjectiveVar2VarSubstitution selectedSubstitution = alreadyNormalized
                ? renamingSubstitution
                : renamingSubstitution.reduceDomainToIntersectionWith(getVariables());

        return selectedSubstitution.isEmpty()
                ? this
                : getRootNode().applyFreshRenaming(renamingSubstitution, leftChild, rightChild, getTreeCache());
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, leftChild, rightChild);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitutionWithoutOptimizing(s, leftChild, rightChild))
                    .orElse(this);
        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable)
                && getRootNode().isConstructed(variable, leftChild, rightChild);
    }

    @Override
    protected boolean computeIsDistinct() {
        return getRootNode().isDistinct(this, leftChild, rightChild);
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    protected VariableNullability computeVariableNullability() {
        return getRootNode().getVariableNullability(leftChild, rightChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        IQTree newTree = getRootNode().propagateDownConstraint(constraint, leftChild, rightChild);
        return newTree.equals(this) ? this : newTree;
    }

    @Override
    public IQTree removeDistincts() {
        IQProperties properties = getProperties();
        return properties.areDistinctAlreadyRemoved()
                ? this
                : getRootNode().removeDistincts(leftChild, rightChild, properties);
    }

    @Override
    public IQTree replaceSubTree(IQTree subTreeToReplace, IQTree newSubTree) {
        if (equals(subTreeToReplace))
            return newSubTree;

        return iqFactory.createBinaryNonCommutativeIQTree(getRootNode(),
                leftChild.replaceSubTree(subTreeToReplace, newSubTree),
                rightChild.replaceSubTree(subTreeToReplace, newSubTree));
    }

    @Override
    protected ImmutableSet<ImmutableSubstitution<NonVariableTerm>> computePossibleVariableDefinitions() {
        return getRootNode().getPossibleVariableDefinitions(leftChild, rightChild);
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(leftChild, rightChild);
    }

    @Override
    protected ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return getRootNode().inferUniqueConstraints(leftChild, rightChild);
    }

    @Override
    protected ImmutableSet<Variable> computeNotInternallyRequiredVariables() {
        return getRootNode().computeNotInternallyRequiredVariables(leftChild, rightChild);
    }
}
