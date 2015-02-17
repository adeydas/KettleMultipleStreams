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
 * Created by Abhishek on 2/17/2015.
 */
public class AwesomeTransformMeta extends BaseStepMeta implements StepMetaInterface {

    private int firstStream;
    private int secondStream;
    private String[] allInputs1;
    private String[] allInputs2;

    @Override
    public void setDefault() {
        allInputs1 = new String[] { "--None--" };
        allInputs2 = new String[] { "--None--" };
        firstStream = 0;
        secondStream = 0;
    }

    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return new AwesomeTransform(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    @Override
    public StepDataInterface getStepData() {
        return new AwesomeTransformData();
    }

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

    @Override
    public String getXML() {
        StringBuffer retval = new StringBuffer();

        List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();

        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STREAM1"), Integer.toString(firstStream)));
        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STREAM2"), Integer.toString(secondStream)));

        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STEP1"), infoStreams.get(0).getStepname()));
        retval.append("    ").append(XMLHandler.addTagValue(getXmlCode("STEP2"), infoStreams.get(1).getStepname()));


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

        for (int i = 0; i < allInputs1.length; i++) {
            retval.append("    ")
                    .append(XMLHandler.openTag(getXmlCode("PVALS2")))
                    .append(Const.CR);
            retval.append("      ").append(
                    XMLHandler.addTagValue(getXmlCode("VALUE2"), allInputs1[i]));
            retval.append("    ")
                    .append(XMLHandler.closeTag(getXmlCode("PVALS2")))
                    .append(Const.CR);
        }

        retval.append("    ").append(XMLHandler.closeTag(getXmlCode("PREV2")))
                .append(Const.CR);

        return retval.toString();
    }

    @Override
    public Object clone() {
        Object retval = super.clone();
        return retval;
    }

    @Override
    public void getFields(RowMetaInterface r, String origin,
                          RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {
        ValueMetaInterface v;

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



        //Output field
        try {
            v = ValueMetaFactory.createValueMeta("Output", ValueMeta.TYPE_STRING);
            v.setOrigin(origin);
            r.addValueMeta(v);
        } catch (KettlePluginException e) {
            logError(e.getMessage());
        }

    }

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

        List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
        infoStreams.get(0).setSubject(XMLHandler.getTagValue(stepnode, "STEP1"));
        infoStreams.get(1).setSubject(XMLHandler.getTagValue(stepnode, "STEP2"));

    }

    public StepIOMetaInterface getStepIOMeta() {
        if ( ioMeta == null ) {

            ioMeta = new StepIOMeta( true, true, false, false, false, false );

            ioMeta.addStream( new Stream( StreamInterface.StreamType.INFO, null, "First Stream" , StreamIcon.INFO, null ) );
            ioMeta.addStream( new Stream( StreamInterface.StreamType.INFO, null, "Second Stream" , StreamIcon.INFO, null ) );
        }

        return ioMeta;
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
}
