package defind;

import java.awt.BorderLayout;


import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class DefindComponent extends AbstractOWLViewComponent {
    private static final Logger log = LoggerFactory.getLogger(DefindComponent.class);

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        JPanel panel = MainForm.addComponentsToPane(this);
        add(panel, BorderLayout.CENTER);
        log.info("Example View Component initialized");
    }

	@Override
	protected void disposeOWLView() {
	}
}
