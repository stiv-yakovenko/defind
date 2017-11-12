package edu.stanford.bmir.protege.examples.defind;

/*-
 * #%L
 * Protege Proof-Based Explanation
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Live Ontologies Project
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


import net.miginfocom.swing.MigLayout;
import org.liveontologies.protege.explanation.proof.ProofServiceManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

public class GridLayoutDemo extends JFrame {

    public GridLayoutDemo(String name) {
        super(name);
    }

    static void updateList(JList jlist, java.util.List<String> list) {
        DefaultListModel model = new DefaultListModel<String>();
        for (String p : list) {
            model.addElement(p);
        }
        jlist.setModel(model);
        jlist.setSelectedIndex(0);
    }

    static void updateList(JList jlist, Set<OWLNamedObject> list) {
        DefaultListModel model = new DefaultListModel<String>();
        for (OWLNamedObject p : list) {
            model.addElement(p.getIRI().getShortForm());
        }
        jlist.setModel(model);
        jlist.setSelectedIndex(0);
    }

    static void removeElem(int idx, Set<OWLNamedObject> delta, JList deltaList) {
        Object dlt = deltaList.getModel().getElementAt(idx);
        for (OWLNamedObject cls : delta) {
            if (cls.getIRI().getShortForm() != dlt) continue;
            delta.remove(cls);
            updateList(deltaList, delta);
            return;
        }
    }

    public static JPanel addComponentsToPane(OWLOntologyManager manager, OWLOntology ont, ProofServiceManager proofServiceManager) {
        String url  = ont.getOntologyID().getDefaultDocumentIRI().get().toString();
        Calc.url=url;
        Set<OWLNamedObject> allClasses = new HashSet<>();
        allClasses.addAll(ont.getClassesInSignature());
        allClasses.addAll(ont.getObjectPropertiesInSignature());
        JPanel mainPanel = new JPanel();
        Set<OWLNamedObject> delta = new HashSet<>();
        OWLClass c[] = new OWLClass[1];
        mainPanel.setLayout(new MigLayout("", "[][grow, left][][grow][]", "[][][grow][][grow][]"));
        mainPanel.add(new JLabel("find:"));
        JTextField filter = new JTextField("", 10);
        Map<Integer, OWLNamedObject> idxToObject = new HashMap<>();
        JList jList = new JList(new String[]{});
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                upd();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                upd();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                upd();
            }
            void upd() {
                String txt = filter.getText();
                java.util.List<String> arr = new ArrayList<>();
                int i = 0;
                idxToObject.clear();
                for (OWLNamedObject elem : allClasses) {
                    if (!elem.getIRI().getShortForm().contains(txt)) continue;
                    arr.add(elem.getIRI().getShortForm());
                    idxToObject.put(i, elem);
                    i++;
                }
                updateList(jList, arr);
            }
        });
        JList deltaList = new JList(new String[]{});
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int idx = list.locationToIndex(evt.getPoint());
                    if (idx < 0) return;
                    OWLNamedObject owlNamedObject = idxToObject.get(idx);
                    delta.add(owlNamedObject);
                    updateList(deltaList, delta);
                }
            }
        });
        deltaList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int idx = list.locationToIndex(evt.getPoint());
                    if (idx < 0) return;
                    removeElem(idx, delta, deltaList);
                }
            }
        });
        java.util.List<String> arr = new ArrayList<>();
        int i = 0;
        for (OWLNamedObject elem : allClasses) {
            arr.add(elem.getIRI().getShortForm());
            idxToObject.put(i, elem);
            i++;
        }
        updateList(jList, arr);
        updateList(deltaList, new HashSet<>());
        mainPanel.add(filter, "wrap");
        JScrollPane jScrollPane = new JScrollPane(jList);
        Dimension d = jList.getPreferredSize();
        jScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        jList.setPreferredSize(d);
        mainPanel.add(jScrollPane, "growy, growx, span 2 4");
        mainPanel.add(new JPanel(), "");
        mainPanel.add(new JLabel("Delta:"), "wrap");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton addButton = new JButton("->");
        addButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = jList.getSelectedIndex();
                if (idx < 0) return;
                OWLNamedObject owlClass = idxToObject.get(idx);
                delta.add(owlClass);
                updateList(deltaList, delta);
            }
        });
        panel.add(addButton);
        JButton removeButton = new JButton("<-");
        removeButton.addActionListener(e -> {
            int idx = deltaList.getSelectedIndex();
            if (idx < 0) return;
            removeElem(idx, delta, deltaList);
        });
        JButton clearButton = new JButton("clear");
        clearButton.addActionListener(e -> {
            delta.clear();
            updateList(deltaList, delta);
        });
        panel.add(removeButton);
        panel.add(clearButton);
        mainPanel.add(panel, "");
        mainPanel.add(new JScrollPane(deltaList), "growx,growy,span2,wrap");
        JButton setCButton = new JButton("->");
        JLabel cLabel = new JLabel("C = ");
        setCButton.addActionListener(e -> {
            int idx = jList.getSelectedIndex();
            if (idx < 0) return;
            OWLNamedObject cls = idxToObject.get(idx);
            if (!(cls instanceof OWLClass)) return;
            c[0] = ((OWLClass) cls);
            cLabel.setText("C = " + cls.getIRI().getShortForm());
        });
        mainPanel.add(setCButton, "");
        mainPanel.add(cLabel, "");
        JButton calc = new JButton("calculate");
        JList<Object> res = new JList<>();
        calc.addActionListener(e -> {
            try {
                if (c[0] == null) {
                    JOptionPane.showMessageDialog(mainPanel, "C is null, select something");
                    return;
                }
                OWLClassExpression sol = Calc.solve(manager, ont, delta, c[0],proofServiceManager);
                List<String> results = new ArrayList<>();
                if (sol instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf ouo = (OWLObjectUnionOf) sol;
                    Set<OWLClassExpression> operands = ouo.getOperands();
                    operands.forEach(owlClassExpression -> {
                        results.add(owlClassExpression.toString().replace(url,""));
                    });
                } else {
                    results.add(sol.toString().replace(url,""));
                }
                updateList(res, results);
            } catch (OWLOntologyCreationException e1) {
                e1.printStackTrace();
            }
        });
        mainPanel.add(calc, "wrap");
        List<String> strs = Arrays.asList(new String[]{});
        updateList(res, strs);
        mainPanel.add(new JLabel(""), "");
        mainPanel.add(new JScrollPane(res), "growx,growy, span 2");
        System.out.println("initializing example tab");
        return mainPanel;
//        pane.add(new JButton("test"));
        //pane.add(mainPanel);
    }

/*    private static void createAndShowGUI() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\steve\\Dropbox\\Projects\\git\\protege_workspace\\protege-master\\omit_cyclic_inferences.owl"));
        GridLayoutDemo frame = new GridLayoutDemo("GridLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addComponentsToPane(frame.getContentPane(), manager, ont, proofServiceManager);
        frame.setSize(700, 600);
        frame.setVisible(true);
    }*/


}
