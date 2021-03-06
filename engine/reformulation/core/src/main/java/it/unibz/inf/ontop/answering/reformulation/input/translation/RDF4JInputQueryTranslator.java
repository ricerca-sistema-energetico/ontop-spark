package it.unibz.inf.ontop.answering.reformulation.input.translation;

import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.iq.IQ;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

public interface RDF4JInputQueryTranslator extends InputQueryTranslator {

    /*
     * TODO: support bindings
     */
    IQ translate(ParsedQuery inputParsedQuery)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException;

    IQ translateAskQuery(ParsedQuery parsedQuery)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException;
}
