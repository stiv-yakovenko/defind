package defind;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefindTab extends OWLWorkspaceViewsTab {
    private static final Logger log = LoggerFactory.getLogger(DefindTab.class);

    public DefindTab() {}

    @Override
    public void initialise() {
        super.initialise();
        log.info("DeFind tab initialized");
    }

    @Override
    public void dispose() {
        super.dispose();
        log.info("DeFind tab disposed");
    }
}
