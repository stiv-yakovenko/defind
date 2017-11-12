package org.liveontologies.protege.explanation.proof.preferences;

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

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

public class ProofBasedExplPrefs {

	private static final String PREFS_KEY_ = "PROOF_BASED_EXPLANATION_PREFS",
			RECURSIVE_EXPANSION_LIMIT_KEY_ = "RECURSIVE_EXPANSION_LIMIT",
			DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_KEY = "DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT",
			REMOVE_UNNECESSARY_INFERENCES_KEY = "REMOVE_UNNECESSARY_INFERENCES";

	public final static String RECURSIVE_EXPANSION_LIMIT_DESCRIPTION = "The maximal number of inferences expanded upon long press or alt + click",
			DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_DESCRIPTION = "The maximal number of inferences displayed at once for each conclusion",
			REMOVE_UNNECESSARY_INFERENCES_DESCRIPTION = "If checked, remove inferences unless it prevents"
					+ " the entailment to be derived from any subset of axioms in the ontology";

	private final static int DEFAULT_RECURSIVE_EXPANSION_LIMIT_ = 300; // inferences

	private final static int DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_ = 5;

	private final static boolean DEFAULT_REMOVE_UNNECESSARY_INFERENCES_ = true;

	/**
	 * {@value #RECURSIVE_EXPANSION_LIMIT_DESCRIPTION}
	 */
	public int recursiveExpansionLimit = DEFAULT_RECURSIVE_EXPANSION_LIMIT_; // inferences

	/**
	 * {@value #DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_DESCRIPTION}
	 */
	public int displayedInferencesPerConclusionLimit = DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_;

	/**
	 * {@value #REMOVE_UNNECESSARY_INFERENCES_DESCRIPTION}
	 */
	public boolean removeUnnecessaryInferences = DEFAULT_REMOVE_UNNECESSARY_INFERENCES_;

	private ProofBasedExplPrefs() {

	}

	private static Preferences getPrefs() {
		PreferencesManager prefMan = PreferencesManager.getInstance();
		return prefMan.getPreferencesForSet(PREFS_KEY_,
				ProofBasedExplPrefs.class);
	}

	/**
	 * @return the preferences initialized with default values
	 */
	public static ProofBasedExplPrefs create() {
		return new ProofBasedExplPrefs();
	}

	public ProofBasedExplPrefs load() {
		Preferences prefs = getPrefs();
		recursiveExpansionLimit = prefs.getInt(RECURSIVE_EXPANSION_LIMIT_KEY_,
				DEFAULT_RECURSIVE_EXPANSION_LIMIT_);
		displayedInferencesPerConclusionLimit = prefs.getInt(
				DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_KEY,
				DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_);
		removeUnnecessaryInferences = prefs.getBoolean(
				REMOVE_UNNECESSARY_INFERENCES_KEY,
				DEFAULT_REMOVE_UNNECESSARY_INFERENCES_);
		return this;
	}

	public ProofBasedExplPrefs save() {
		Preferences prefs = getPrefs();
		prefs.putInt(RECURSIVE_EXPANSION_LIMIT_KEY_, recursiveExpansionLimit);
		prefs.putInt(DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_KEY,
				displayedInferencesPerConclusionLimit);
		prefs.putBoolean(REMOVE_UNNECESSARY_INFERENCES_KEY,
				removeUnnecessaryInferences);
		return this;
	}

	public ProofBasedExplPrefs reset() {
		recursiveExpansionLimit = DEFAULT_RECURSIVE_EXPANSION_LIMIT_;
		displayedInferencesPerConclusionLimit = DEFAULT_DISPLAYED_INFERENCES_PER_CONCLUSION_LIMIT_;
		removeUnnecessaryInferences = DEFAULT_REMOVE_UNNECESSARY_INFERENCES_;
		return this;
	}

}
