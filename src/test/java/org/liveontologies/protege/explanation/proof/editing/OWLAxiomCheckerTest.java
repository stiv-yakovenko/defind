package org.liveontologies.protege.explanation.proof.editing;

/*-
 * #%L
 * Protege Proof-Based Explanation
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Live Ontologies Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.StringWriter;

import javax.inject.Provider;

import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

public class OWLAxiomCheckerTest {

	@Test
	@Ignore // too many axioms cannot be parsed
	public void testManchesterParser() throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(
				getInputOntology("owl2primer.owl"));
		ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserPatched(
				new Provider<OWLOntologyLoaderConfiguration>() {
					@Override
					public OWLOntologyLoaderConfiguration get() {
						return new OWLOntologyLoaderConfiguration();
					}
				}, manager.getOWLDataFactory());
		parser.setOWLEntityChecker(new OntologyEntityChecker(ontology));
		for (OWLAxiom axiom : ontology.getAxioms()) {
			String rendered = render(axiom);
			if (axiom.isOfType(AxiomType.DECLARATION)) {
				continue;
			}
			try {
				parser.setStringToParse(rendered);
				OWLAxiom parsed = parser.parseAxiom();
				assertEquals(axiom, parsed);
			} catch (OWLParserException e) {
				throw new RuntimeException("Cannot parse: " + rendered, e);
			}
		}

	}

	InputStream getInputOntology(String fileName) {
		return getClass().getClassLoader().getResourceAsStream(fileName);
	}

	String render(OWLObject obj) {
		StringWriter writer = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(
				writer, new SimpleShortFormProvider());
		obj.accept(renderer);
		return writer.toString();
	}

}
