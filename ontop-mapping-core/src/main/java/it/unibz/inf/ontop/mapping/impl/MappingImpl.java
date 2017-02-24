package it.unibz.inf.ontop.mapping.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.InjectiveVar2VarSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryRenamer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;


public class MappingImpl implements Mapping {

    private final MappingMetadata metadata;
    private final ImmutableMap<AtomPredicate, IntermediateQuery> definitions;

    @AssistedInject
    private MappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted Stream<IntermediateQuery> queryStream) {
        AtomicInteger i = new AtomicInteger(0);
        this.metadata = metadata;
        this.definitions = queryStream
                .map(m -> appendSuffixToVariableNames(m, i.incrementAndGet()))
                .collect(ImmutableCollectors.toMap(
                        q -> q.getProjectionAtom().getPredicate(),
                        q -> q
                ));
    }

    @AssistedInject
    private MappingImpl(@Assisted MappingMetadata metadata,
                        @Assisted ImmutableMap<AtomPredicate, IntermediateQuery> mappingMap) {
        this(metadata, mappingMap.values().stream());
    }

    @Override
    public MappingMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Optional<IntermediateQuery> getDefinition(AtomPredicate predicate) {
        return Optional.ofNullable(definitions.get(predicate));
    }

    @Override
    public ImmutableSet<AtomPredicate> getPredicates() {
        return definitions.keySet();
    }

    private static IntermediateQuery appendSuffixToVariableNames(IntermediateQuery query, int suffix) {
        Map<Variable, Variable> substitutionMap =
                query.getKnownVariables().stream()
                        .collect(Collectors.toMap(v -> v, v -> DATA_FACTORY.getVariable(v.getName()+"m"+suffix)));
        QueryRenamer queryRenamer = new QueryRenamer(new InjectiveVar2VarSubstitutionImpl(substitutionMap));
        return queryRenamer.transform(query);
    }
}
