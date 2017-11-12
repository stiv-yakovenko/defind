/*
 * #%L
 * Proof-Based Explanations
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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
package org.liveontologies.protege.explanation.proof.service;

import org.liveontologies.puli.AssertedConclusionInference;
import org.liveontologies.puli.DynamicProof;
import org.liveontologies.puli.Inference;
import org.liveontologies.puli.Inferences;
import org.liveontologies.puli.Proof;
import org.liveontologies.puli.ProofNode;
import org.liveontologies.puli.ProofStep;
import org.liveontologies.puli.Proofs;
import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;

/**
 * A skeleton for a plugin that can provide proofs for OWL axioms
 * 
 * @author Yevgeny Kazakov
 *
 */
public abstract class ProofService implements ProtegePluginInstance {

	private OWLEditorKit kit_;
	private String pluginId_;
	private String name_;

	public ProofService setup(OWLEditorKit kit, String pluginId, String name) {
		this.kit_ = kit;
		this.pluginId_ = pluginId;
		this.name_ = name;
		return this;
	}

	public OWLEditorKit getEditorKit() {
		return kit_;
	}

	public String getPluginId() {
		return pluginId_;
	}

	public String getName() {
		return name_;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @param entailment
	 *            the entailed {@link OWLAxiom} for which the proof should be
	 *            found
	 * @return {@code true} if this service can provide a proof for the given
	 *         entailed {@link OWLAxiom}; the subsequent call of
	 *         {@link #getProof(OWLAxiom)} should return such a proof
	 */
	public abstract boolean hasProof(OWLAxiom entailment);

	/**
	 * Returns a proof for the given {@link OWLAxiom} containing inferences over
	 * {@link OWLAxiom}s using which it should be possible to derive the given
	 * {@link OWLAxiom}. In this proof, {@link OWLAxiom}s occurring in the
	 * ontology can be derived using {@link AssertedConclusionInference}. If the
	 * given {@link OWLAxiom} is not entailed, the result may be any proof in
	 * which this {@link OWLAxiom} is not derivable, e.g., the empty proof.
	 * 
	 * @see Proofs#isDerivable(Proof, Object)
	 * @see Inferences#isAsserted(Inference)
	 * @see Proofs#emptyProof()
	 * 
	 * @param entailment
	 *            the {@link OWLAxiom} for which the proof should be generated
	 * @return the {@link DynamicProof} representing the set of inferences using
	 *         which the given {@link OWLAxiom} can be derived
	 * @throws UnsupportedEntailmentTypeException
	 *             if checking entailment of the given given {@link OWLAxiom} is
	 *             not supported
	 */
	public abstract DynamicProof<Inference<? extends OWLAxiom>> getProof(
			OWLAxiom entailment) throws UnsupportedEntailmentTypeException;

	/**
	 * This method provides examples that explain inferences used in the proof
	 * 
	 * @param inference
	 *            an {@link Inference} that should be explained
	 * 
	 * @return an example of the given inference, which can be used for
	 *         explanation purpose. Usually it is an inference instantiated with
	 *         some generic parameters. If {@code null}, no example is provided.
	 */
	abstract public Inference<? extends OWLAxiom> getExample(
			Inference<? extends OWLAxiom> inference);

	/**
	 * Using this method the displayed proof can be additionally post-processed
	 * before being displayed to the user
	 * 
	 * @param node
	 *            the {@link ProofNode} representing the acyclic proof tree
	 *            obtained from the proof returned by
	 *            {@link #getProof(OWLAxiom)}; the proof steps in this proof can
	 *            use only inferences from this proof or the
	 *            {@link AssertedConclusionInference} for inferences from the
	 *            axioms in the ontology. A {@link ProofNode} is acyclic if any
	 *            path induced by the conclusion - premise relation in the proof
	 *            tree represented by this {@link ProofNode} does not contain
	 *            repetitions (i.e., equal {@link ProofNode}s).
	 * 
	 * @return the rewritten acyclic {@link ProofNode} that should be used to
	 *         display the proof
	 * 
	 * @see ProofStep#getInference()
	 */
	public ProofNode<OWLAxiom> postProcess(ProofNode<OWLAxiom> node) {
		return node;
	}

	@Override
	public abstract void dispose();

}
