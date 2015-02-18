package ws.abhis.kettle;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * Meta class
 */
public class AwesomeTransformMeta extends BaseStepMeta implements StepMetaInterface {

    private int firstStream;
    private int secondStream;
    private int firstStep;
    private int secondStep;
    private String[] allSteps;
    private String[] allInputs1;
    private String[] allInputs2;
    private ValueMetaInterface outputField;

    /**
     * Set the default values.
     */
    @Override
    public void setDefault() {
        allInputs1 = new String[] { "--None--" };
        allInputs2 = new String[] { "--None--" };
        allSteps = new String[] { "--None--" };
        firstStream = 0;
        secondStream = 0;
        firstStep = 0;
        secondStep = 0;
    }

    /**
     * Create new instance of the step's row processor.
     * @param stepMeta
     * @param stepDataInterface
     * @param copyNr
     * @param transMeta
     * @param trans
     * @return StepInterface
     */
    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return new AwesomeTransform(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    /**
     * Create new instance of the step's data class. Called per row.
     * @return StepDataInterface
     */
    @Override
    public StepDataInterface getStepData() {
        return new AwesomeTransformData();
    }

    /**
     * Create a new GUI dialog.
     * @param shell
     * @param meta
     * @param transMeta
     * @param name
     * @return StepDialogInterface
     */
    public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta,
                                         TransMeta transMeta, String name) {
        return new AwesomeTransformDialog(shell, meta, transMeta, name);
    }



    public int getSecondStream() {
        return secondStream;
    }

    public void setSecondStream(int secondStream) {
        this.secondStream = secondStream;
    }

    public int getFirstStream() {
        return firstStream;
    }

    public void setFirstStream(int firstStream) {
        this.firstStream = firstStream;
    }

    /**
     * Get a well-formed XML to save state on disk.
     * For the XML schema, see step-attributes.xml.
     * @return String
     */
    @Override
    public String getXML() {
        StringBuffer retval = new StringBuffer();

        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STREAM1"), Integer.toString(firstStream)));
        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STREAM2"), Integer.toString(secondStream)));

        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STEP1"), Integer.toString(firstStep)));
        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STEP2"), Integer.toString(secondStep)));


        //Stream 1
        retval.append("    ").append(XMLHandler.openTag(getXmlCode("PREV1")))
                .append(Const.CR);

        for (int i = 0; i < allInputs1.length; i++) {
            retval.append("    ")
                    .append(XMLHandler.openTag(getXmlCode("PVALS1")))
                    .append(Const.CR);
            retval.append("      ").append(
                    XMLHandler.addTagValue(getXmlCode("VALUE1"), allInputs1[i]));
            retval.append("    ")
                    .append(XMLHandler.closeTag(getXmlCode("PVALS1")))
                    .append(Const.CR);
        }

        retval.append("    ").append(XMLHandler.closeTag(getXmlCode("PREV1")))
                .append(Const.CR);

        //Stream 2
        retval.append("    ").append(XMLHandler.openTag(getXmlCode("PREV2")))
                .append(Const.CR);

        for (int i = 0; i < allInputs2.length; i++) {
            retval.append("    ")
                    .append(XMLHandler.openTag(getXmlCode("PVALS2")))
                    .append(Const.CR);
            retval.append("      ").append(
                    XMLHandler.addTagValue(getXmlCode("VALUE2"), allInputs2[i]));
            retval.append("    ")
                    .append(XMLHandler.closeTag(getXmlCode("PVALS2")))
                    .append(Const.CR);
        }

        retval.append("    ").append(XMLHandler.closeTag(getXmlCode("PREV2")))
                .append(Const.CR);

        //Steps

        retval.append("    ").append(XMLHandler.openTag(getXmlCode("PREV3")))
                .append(Const.CR);

        for (int i = 0; i < allSteps.length; i++) {
            retval.append("    ")
                    .append(XMLHandler.openTag(getXmlCode("PVALS3")))
                    .append(Const.CR);
            retval.append("      ").append(
                    XMLHandler.addTagValue(getXmlCode("VALUE3"), allSteps[i]));
            retval.append("    ")
                    .append(XMLHandler.closeTag(getXmlCode("PVALS3")))
                    .append(Const.CR);
        }

        retval.append("    ").append(XMLHandler.closeTag(getXmlCode("PREV3")))
                .append(Const.CR);

        return retval.toString();
    }

    /**
     * Clone object.
     * @return Object
     */
    @Override
    public Object clone() {
        Object retval = super.clone();
        return retval;
    }

    /**
     * Publish fields for step downstream.
     * For this step all previous fields and an "Output" field is published.
     * @param r
     * @param origin
     * @param info
     * @param nextStep
     * @param space
     */
    @Override
    public void getFields(RowMetaInterface r, String origin,
                          RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {


        if (info != null) {
            for (int i=0; i<info.length; i++) {
                if (info[i] != null) {
                    r.mergeRowMeta(info[i]);
                }
            }
        }

        for (int i=0; i<r.size(); i++) {
            r.getValueMeta(i).setOrigin(origin);
        }

        //Add Output field
        try {
            outputField = ValueMetaFactory.createValueMeta("Output", ValueMeta.TYPE_STRING);
            outputField.setOrigin(origin);
            r.addValueMeta(outputField);
        } catch (KettlePluginException e) {
            logError(e.getMessage());
        }

    }

    /**
     * Parse XML from a .ktr file.
     * For the XML schema, see step-attributes.xml.
     * @param stepnode
     * @param databases
     * @param counters
     */
    @Override
    public void loadXML(Node stepnode, List<DatabaseMeta> databases,
                        Map<String, Counter> counters) {
        try {
            readData(stepnode);
        } catch (KettleException e) {
            logError(e.getMessage());
        }
    }

    private void readData(Node stepnode) throws KettleException{

        firstStream = Integer.parseInt(XMLHandler.getTagValue(stepnode, getXmlCode("STREAM1")));
        secondStream = Integer.parseInt(XMLHandler.getTagValue(stepnode, getXmlCode("STREAM2")));

        firstStep = Integer.parseInt(XMLHandler.getTagValue(stepnode, getXmlCode("STEP1")));
        secondStep = Integer.parseInt(XMLHandler.getTagValue(stepnode, getXmlCode("STEP2")));


        Node prev1 = XMLHandler.getSubNode(stepnode, getXmlCode("PREV1"));
        int nrfields = XMLHandler.countNodes(prev1, getXmlCode("PVALS1"));
        String[] strArr = new String[nrfields];
        for (int i = 0; i < nrfields; i++) {
            Node fnode = XMLHandler
                    .getSubNodeByNr(prev1, getXmlCode("PVALS1"), i);
            strArr[i] = (XMLHandler.getTagValue(fnode, getXmlCode("VALUE1")));
        }

        allInputs1 = strArr;

        prev1 = XMLHandler.getSubNode(stepnode, getXmlCode("PREV2"));
        nrfields = XMLHandler.countNodes(prev1, getXmlCode("PVALS2"));
        strArr = new String[nrfields];
        for (int i = 0; i < nrfields; i++) {
            Node fnode = XMLHandler
                    .getSubNodeByNr(prev1, getXmlCode("PVALS2"), i);
            strArr[i] = (XMLHandler.getTagValue(fnode, getXmlCode("VALUE2")));
        }

        allInputs2 = strArr;

        prev1 = XMLHandler.getSubNode(stepnode, getXmlCode("PREV3"));
        nrfields = XMLHandler.countNodes(prev1, getXmlCode("PVALS3"));
        strArr = new String[nrfields];
        for (int i = 0; i < nrfields; i++) {
            Node fnode = XMLHandler
                    .getSubNodeByNr(prev1, getXmlCode("PVALS3"), i);
            strArr[i] = (XMLHandler.getTagValue(fnode, getXmlCode("VALUE3")));
        }

        allSteps = strArr;

    }


    public void resetStepIoMeta() {
        // Don't reset!
    }

    public String[] getAllInputs1() {
        return allInputs1;
    }

    public void setAllInputs1(String[] allInputs1) {
        this.allInputs1 = allInputs1;
    }

    public String[] getAllInputs2() {
        return allInputs2;
    }

    public void setAllInputs2(String[] allInputs2) {
        this.allInputs2 = allInputs2;
    }

    public int getFirstStep() {
        return firstStep;
    }

    public void setFirstStep(int firstStep) {
        this.firstStep = firstStep;
    }

    public int getSecondStep() {
        return secondStep;
    }

    public void setSecondStep(int secondStep) {
        this.secondStep = secondStep;
    }

    public String[] getAllSteps() {
        return allSteps;
    }

    public void setAllSteps(String[] allSteps) {
        this.allSteps = allSteps;
    }

    public ValueMetaInterface getOutputField() {
        return outputField;
    }

    public void setOutputField(ValueMetaInterface outputField) {
        this.outputField = outputField;
    }
}
