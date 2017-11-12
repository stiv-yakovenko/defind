package org.liveontologies.protege.explanation.proof;

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

import java.util.ArrayList;
import java.util.Collection;

import org.liveontologies.protege.explanation.proof.service.ProofPlugin;
import org.liveontologies.protege.explanation.proof.service.ProofPluginLoader;
import org.liveontologies.protege.explanation.proof.service.ProofService;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;

/**
 * Keeps track of the available {@link ProofService} plugins.
 * 
 * @author Pavel Klinov pavel.klinov@uni-ulm.de
 * 
 * @author Yevgeny Kazakov
 */
public class ProofServiceManager implements Disposable {

	private static final String KEY_ = "org.liveontologies.protege.explanation.proof.services";

	private final OWLEditorKit kit_;

	private final Collection<ProofService> services_;

	private ProofServiceManager(OWLEditorKit kit) throws Exception {
		this.kit_ = kit;
		this.services_ = new ArrayList<ProofService>();
		ProofPluginLoader loader = new ProofPluginLoader(kit_);
		for (ProofPlugin plugin : loader.getPlugins()) {
			ProofService service = plugin.newInstance();
			service.initialise();
			services_.add(service);
		}
	}

	public static synchronized ProofServiceManager get(OWLEditorKit editorKit)
			throws Exception {
		// reuse one instance
		ProofServiceManager m = editorKit.getModelManager().get(KEY_);
		if (m == null) {
			m = new ProofServiceManager(editorKit);
			editorKit.put(KEY_, m);
		}
		return m;
	}

	@Override
	public void dispose() {
		for (ProofService proofService : services_) {
			proofService.dispose();
		}
	}

	public OWLEditorKit getOWLEditorKit() {
		return kit_;
	}

	public Collection<ProofService> getProofServices() {
		return services_;
	}

}
