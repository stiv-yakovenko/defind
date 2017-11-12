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

import javax.inject.Provider;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * An {@link OWLExpressionChecker} for parsing of {@link OWLAxiom} in
 * Manchester-flavored syntax rendered by OWL API
 * 
 * @author Yevgeny Kazakov
 */
public class OWLAxiomChecker implements OWLExpressionChecker<OWLAxiom> {

	private OWLModelManager mngr;

	public OWLAxiomChecker(OWLModelManager mngr) {
		this.mngr = mngr;
	}

	public String getRendering(OWLAxiom axiom) {
		return mngr.getRendering(axiom);
	}

	public boolean isParsable(OWLAxiom axiom) {
		try {
			check(getRendering(axiom));
			return true;
		} catch (OWLExpressionParserException e) {
			return false;
		}
	}

	@Override
	public void check(String text) throws OWLExpressionParserException {
		createObject(text);
	}

	@Override
	public OWLAxiom createObject(String text)
			throws OWLExpressionParserException {
		ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserPatched(
				new Provider<OWLOntologyLoaderConfiguration>() {
					@Override
					public OWLOntologyLoaderConfiguration get() {
						return new OWLOntologyLoaderConfiguration();
					}
				}, mngr.getOWLDataFactory());
		parser.setOWLEntityChecker(
				new ProtegeOWLEntityChecker(mngr.getOWLEntityFinder()));
		parser.setStringToParse(text);
		try {
			return parser.parseAxiom();
		} catch (ParserException e) {
			throw ParserUtil.convertException(e);
		}
	}

}
