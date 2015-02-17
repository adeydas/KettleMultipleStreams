package ws.abhis.kettle;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.List;

/**
 * Created by Abhishek on 2/17/2015.
 */
public class AwesomeTransform extends BaseStep implements StepInterface {

    private AwesomeTransformData data;
    private AwesomeTransformMeta meta;

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (AwesomeTransformMeta) smi;
        data = (AwesomeTransformData) sdi;
        // All initialization code goes here
        return super.init(smi, sdi);
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (AwesomeTransformMeta) smi;
        data = (AwesomeTransformData) sdi;
        super.dispose(smi, sdi);
    }

    public synchronized boolean processRow(StepMetaInterface smi,StepDataInterface sdi) throws KettleException {
        meta = (AwesomeTransformMeta) smi;
        data = (AwesomeTransformData) sdi;

        List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
        String stepname1 = infoStreams.get(0).getStepname();
        data.oneRowSet = findInputRowSet(stepname1);
        if (data.oneRowSet == null)
            throw new KettleException("Could not find first rowset");

        String stepname2 = infoStreams.get(1).getStepname();
        data.twoRowSet = findInputRowSet(stepname2);
        if (data.twoRowSet == null)
            throw new KettleException("Could not find second rowset");

        data.one = getRowFrom(data.oneRowSet);
        data.two = getRowFrom(data.twoRowSet);

        //We stop processing if either one is null
        if (data.one == null || data.two == null) {
            setOutputDone();
            return false;
        }

        if (first) {
            first = false;



//            data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
//            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
              data.prevRowMeta = getInputRowMeta().clone();
//            data.NrPrevFields = data.prevRowMeta.size();


            if (data.one != null) {
                data.oneMeta = data.oneRowSet.getRowMeta();
            } else {
                data.one = null;
                data.oneMeta = getTransMeta().getStepFields(infoStreams.get(0).getStepname());
            }


            if (data.two != null) {
                data.twoMeta = data.twoRowSet.getRowMeta();
            } else {
                data.two = null;
                data.twoMeta = getTransMeta().getStepFields(infoStreams.get(1).getStepname());
            }

            /**
             * Merge the two streams
             */
            data.outputRowMeta = new RowMeta();
            data.outputRowMeta.mergeRowMeta(data.oneMeta.clone());
            data.outputRowMeta.mergeRowMeta(data.twoMeta.clone());

            data.NrFields1 = data.oneMeta.size();
            data.NrFields2 = data.twoMeta.size();
        }


        String output1 = data.oneMeta.getString(data.one, meta.getFirstStream());
        String output2 = data.twoMeta.getString(data.two, meta.getSecondStream());

        String totalOutput = output1 + output2;

        Object[] outputRow = RowDataUtil.allocateRowData(data.NrFields1+data.NrFields2);

        int i=0;
        for (i = 0; i < data.NrFields1; i++) {
            outputRow[i] = data.one[i];
        }
        for (int j=0; j<data.NrFields2; j++) {
            outputRow[i++] = data.two[j];
        }

        outputRow[i] = outputRow;
        putRow(data.outputRowMeta, outputRow);


        return true;
    }


    public void run() {
        try {
            while (processRow(meta, data) && !isStopped());
        } catch (Exception e) {
            logError(Const.getStackTracker(e));
            setErrors(1);
            stopAll();
        } finally {
            dispose(meta, data);
            markStop();
        }
    }

    /**
     * This is the base step that forms that basis for all steps. You can derive from this class to implement your own
     * steps.
     *
     * @param stepMeta          The StepMeta object to run.
     * @param stepDataInterface the data object to store temporary data, database connections, caches, result sets,
     *                          hashtables etc.
     * @param copyNr            The copynumber for this step.
     * @param transMeta         The TransInfo of which the step stepMeta is part of.
     * @param trans             The (running) transformation to obtain information shared among the steps.
     */
    public AwesomeTransform(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
}
