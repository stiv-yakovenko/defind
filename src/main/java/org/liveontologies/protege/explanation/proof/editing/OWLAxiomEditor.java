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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.editor.AbstractOWLObjectEditor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;

/**
 * Editor for editing all kinds of axioms which may appear in the proofs.
 * 
 * @author Pavel Klinov
 * 
 * @author Yevgeny Kazakov
 */
public class OWLAxiomEditor extends AbstractOWLObjectEditor<OWLAxiom>
		implements VerifiedInputEditor {

	private final OWLAxiomChecker axiomChecker_;

	private final ExpressionEditor<OWLAxiom> editor_;

	private final JComponent editingComponent_;

	public OWLAxiomEditor(OWLEditorKit editorKit,
			OWLAxiomChecker axiomChecker) {
		this.axiomChecker_ = axiomChecker;
		this.editor_ = new ExpressionEditor<OWLAxiom>(editorKit, axiomChecker);

		editingComponent_ = new JPanel(new BorderLayout());
		editingComponent_.add(editor_);
		editingComponent_.setPreferredSize(new Dimension(400, 200));
	}

	@Override
	public boolean setEditedObject(OWLAxiom axiom) {
		if (axiom == null) {
			return true;
		}
		editor_.setText(getRendering(axiom));
		return true;
	}

	@Override
	public String getEditorTypeName() {
		return "OWL Axiom Editor";
	}

	@Override
	public boolean canEdit(Object object) {
		return object instanceof OWLAxiom && (isParsable((OWLAxiom) object));
	}

	String getRendering(OWLAxiom axiom) {
		return axiomChecker_.getRendering(axiom);
	}

	boolean isParsable(OWLAxiom axiom) {
		try {
			axiomChecker_.check(getRendering(axiom));
			return true;
		} catch (OWLExpressionParserException e) {
			return false;
		}
	}

	/**
	 * Gets a component that will be used to edit the specified object.
	 * 
	 * @return The component that will be used to edit the object
	 */
	@Override
	public JComponent getEditorComponent() {
		return editingComponent_;
	}

	/**
	 * Gets the object that has been edited.
	 * 
	 * @return The edited object
	 */
	@Override
	public OWLAxiom getEditedObject() {
		try {
			if (editor_.isWellFormed()) {
				return editor_.createObject();
			} else {
				return null;
			}
		} catch (OWLException e) {
			return null;
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void addStatusChangedListener(
			InputVerificationStatusChangedListener listener) {
		editor_.addStatusChangedListener(listener);
	}

	@Override
	public void removeStatusChangedListener(
			InputVerificationStatusChangedListener listener) {
		editor_.removeStatusChangedListener(listener);
	}

}
