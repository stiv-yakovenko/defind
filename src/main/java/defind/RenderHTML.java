package defind;

import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RenderHTML {
    public static String render(OWLObjectPropertyExpression exp) {
        return "<a href='_href_'>_name_</a>"
                .replaceAll("_href_", exp.toString())
                .replaceAll("_name_", ((OWLObjectPropertyImpl) exp).getIRI().getRemainder().get());
    }

    public static String render(OWLClassExpression exp, Map<String, Object> objs) {
        objs.put(exp.toString(),exp);
        StringBuffer res = new StringBuffer();
        if (exp instanceof OWLClassImpl) {
            OWLClassImpl o = ((OWLClassImpl) exp);
            String clsName = o.getIRI().getRemainder().get();
            res.append("<a href='_href_'>_name_</a>"
                    .replaceAll("_href_", o.toString())
                    .replaceAll("_name_", clsName));
        } else if (exp instanceof OWLObjectIntersectionOf) {
            OWLObjectIntersectionOf oio = ((OWLObjectIntersectionOf) exp);
            List<OWLClassExpression> operands = new ArrayList<OWLClassExpression>(oio.getOperands());
            res.append("(");
            for (int i = 0; i < operands.size(); i++) {
                OWLClassExpression owlClassExpression = operands.get(i);
                res.append(render(owlClassExpression,objs));
                if (i < operands.size() - 1) res.append(" and ");
            }
            res.append(")");
        } else if (exp instanceof OWLObjectUnionOf) {
            OWLObjectUnionOf ouo = ((OWLObjectUnionOf) exp);
            List<OWLClassExpression> operands = new ArrayList<OWLClassExpression>(ouo.getOperands());
            if (operands.size() > 1) res.append("(");
            for (int i = 0; i < operands.size(); i++) {
                OWLClassExpression owlClassExpression = operands.get(i);
                String html = render(owlClassExpression,objs);
                res.append(html);
                if (i < operands.size() - 1 && html.length() > 0) {
                    res.append(" or ");
                }
            }
            if (operands.size() > 1) res.append(")");
        } else if (exp instanceof OWLObjectSomeValuesFrom) {
            OWLObjectSomeValuesFrom osvf = ((OWLObjectSomeValuesFrom) exp);
            OWLClassExpression filler = osvf.getFiller();
            OWLObjectPropertyExpression prop = osvf.getProperty();
            res.append("(");
            res.append(render(prop));
            res.append(" some ");
            String html = render(filler,objs);
            res.append(html);
            res.append(")");
        }
        return res.toString();
    }
}
