package org.liveontologies.protege.explanation.proof.service;

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

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractPluginLoader;
import org.protege.editor.owl.OWLEditorKit;

public class ProofPluginLoader extends AbstractPluginLoader<ProofPlugin> {

	private final OWLEditorKit editorKit_;

	public ProofPluginLoader(OWLEditorKit editorKit) {
		super(ProofPlugin.KEY, ProofPlugin.ID);
		this.editorKit_ = editorKit;
	}

	@Override
	protected ProofPlugin createInstance(IExtension extension) {
		return new ProofPlugin(editorKit_, extension);
	}

}
