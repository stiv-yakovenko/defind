package edu.stanford.bmir.protege.examples.view;

/*-
 * #%L
 * DeFind
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Some Organisation
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


import defind.GridLayoutDemo;
import org.liveontologies.protege.explanation.proof.ProofServiceManager;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class ExampleViewComponent extends AbstractOWLViewComponent {
    private static final Logger log = LoggerFactory.getLogger(ExampleViewComponent.class);

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        JPanel panel = GridLayoutDemo.addComponentsToPane(this);
        add(panel, BorderLayout.CENTER);
        log.info("Example View Component initialized");
    }

	@Override
	protected void disposeOWLView() {
	}
}
