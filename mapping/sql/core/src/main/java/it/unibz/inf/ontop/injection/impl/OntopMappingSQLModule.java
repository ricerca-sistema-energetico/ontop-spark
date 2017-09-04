package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.spec.dbschema.DBMetadataExtractor;
import it.unibz.inf.ontop.spec.dbschema.PreProcessedImplicitRelationalDBConstraintExtractor;
import it.unibz.inf.ontop.spec.dbschema.RDBMetadataExtractor;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;

public class OntopMappingSQLModule extends OntopAbstractModule {


    private final OntopMappingSQLSettings settings;

    protected OntopMappingSQLModule(OntopMappingSQLConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopMappingSQLSettings.class).toInstance(settings);

        bindFromSettings(SQLPPMappingFactory.class);
        bindFromSettings(SQLMappingParser.class);
        bindFromSettings(SQLPPMappingConverter.class);
        bindFromSettings(PreProcessedImplicitRelationalDBConstraintExtractor.class);
        bindFromSettings(MappingExtractor.class);
        bindFromSettings(RDBMetadataExtractor.class);

//        Module nativeQLFactoryModule = buildFactory(
//                ImmutableList.of(RDBMetadataExtractor.class),
//                NativeQueryLanguageComponentFactory.class);
//        install(nativeQLFactoryModule);
    }
}
