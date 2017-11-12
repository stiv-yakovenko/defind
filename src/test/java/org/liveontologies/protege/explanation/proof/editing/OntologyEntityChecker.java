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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class OntologyEntityChecker implements OWLEntityChecker {

	private final Map<String, OWLClass> classes_ = new HashMap<String, OWLClass>();
	private final Map<String, OWLObjectProperty> objectProperties_ = new HashMap<String, OWLObjectProperty>();
	private final Map<String, OWLDataProperty> dataProperties_ = new HashMap<String, OWLDataProperty>();
	private final Map<String, OWLNamedIndividual> namedIndividuals_ = new HashMap<String, OWLNamedIndividual>();
	private final Map<String, OWLDatatype> datatypes_ = new HashMap<String, OWLDatatype>();
	private final Map<String, OWLAnnotationProperty> annotationProperties_ = new HashMap<String, OWLAnnotationProperty>();

	OntologyEntityChecker(OWLOntology ontology) {
		for (OWLClass obj : ontology.getClassesInSignature()) {
			classes_.put(render(obj), obj);
		}
		for (OWLObjectProperty obj : ontology
				.getObjectPropertiesInSignature()) {
			objectProperties_.put(render(obj), obj);
		}
		for (OWLDataProperty obj : ontology.getDataPropertiesInSignature()) {
			dataProperties_.put(render(obj), obj);
		}
		for (OWLNamedIndividual obj : ontology.getIndividualsInSignature()) {
			namedIndividuals_.put(render(obj), obj);
		}
		for (OWLDatatype obj : ontology.getDatatypesInSignature()) {
			datatypes_.put(render(obj), obj);
		}
		for (OWLAnnotationProperty obj : ontology
				.getAnnotationPropertiesInSignature()) {
			annotationProperties_.put(render(obj), obj);
		}
	}

	String render(OWLObject obj) {
		StringWriter writer = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer renderer = new ManchesterOWLSyntaxObjectRenderer(
				writer, new SimpleShortFormProvider());
		obj.accept(renderer);
		return writer.toString();
	}

	@Override
	public OWLClass getOWLClass(String name) {
		return classes_.get(name);
	}

	@Override
	public OWLObjectProperty getOWLObjectProperty(String name) {
		return objectProperties_.get(name);
	}

	@Override
	public OWLDataProperty getOWLDataProperty(String name) {
		return dataProperties_.get(name);
	}

	@Override
	public OWLNamedIndividual getOWLIndividual(String name) {
		return namedIndividuals_.get(name);
	}

	@Override
	public OWLDatatype getOWLDatatype(String name) {
		return datatypes_.get(name);
	}

	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
		return annotationProperties_.get(name);
	}

}
