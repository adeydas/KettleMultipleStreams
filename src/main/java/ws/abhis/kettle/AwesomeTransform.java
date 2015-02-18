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
 * Primary business logic here.
 * Each row is processed using this logic.
 */
public class AwesomeTransform extends BaseStep implements StepInterface {

    private AwesomeTransformData data;
    private AwesomeTransformMeta meta;

    /**
     * Initialize.
     * @param smi
     * @param sdi
     * @return boolean
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (AwesomeTransformMeta) smi;
        data = (AwesomeTransformData) sdi;
        return super.init(smi, sdi);
    }

    /**
     * Dtor
     * @param smi
     * @param sdi
     */
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (AwesomeTransformMeta) smi;
        data = (AwesomeTransformData) sdi;
        super.dispose(smi, sdi);
    }

    /**
     * Process each row.
     * @param smi
     * @param sdi
     * @return boolean
     * @throws KettleException
     */
    public synchronized boolean processRow(StepMetaInterface smi,StepDataInterface sdi) throws KettleException {
        meta = (AwesomeTransformMeta) smi;
        data = (AwesomeTransformData) sdi;

        //Get the first rowset. Fetches from the selected step in GUI.
        data.oneRowSet = findInputRowSet(meta.getAllSteps()[meta.getFirstStep()]);
        if (data.oneRowSet == null)
            throw new KettleException("Could not find first rowset");

        //Get the second rowset. Fetches from the selected step in GUI.
        data.twoRowSet = findInputRowSet(meta.getAllSteps()[meta.getSecondStep()]);
        if (data.twoRowSet == null)
            throw new KettleException("Could not find second rowset");

        //Get data from the fetched rowsets.
        data.one = getRowFrom(data.oneRowSet);
        data.two = getRowFrom(data.twoRowSet);

        //We stop processing if either one is null
        if (data.one == null || data.two == null) {
            setOutputDone();
            return false;
        }

        //Conditional logic to fetch meta information, if it is the first row.
        if (first) {
            first = false;

            //Get meta information for first set.
            if (data.one != null) {
                data.oneMeta = data.oneRowSet.getRowMeta();
            } else {
                data.one = null;
                data.oneMeta = getTransMeta().getStepFields(meta.getAllSteps()[meta.getFirstStep()]);
            }

            //Get meta information for second set.
            if (data.two != null) {
                data.twoMeta = data.twoRowSet.getRowMeta();
            } else {
                data.two = null;
                data.twoMeta = getTransMeta().getStepFields(meta.getAllSteps()[meta.getSecondStep()]);
            }

            /**
             * Merge the two streams.
             * This step may vary based on the business logic. We keep it here so we can output all the fields
             * at one go.
             * The fields may be outputted per rowset as well but this implementation saves some time in that
             * case.
             */
            data.outputRowMeta = new RowMeta();
            data.outputRowMeta.mergeRowMeta(data.oneMeta.clone());
            data.outputRowMeta.mergeRowMeta(data.twoMeta.clone());
            /**
             * Add extra meta information for our output field.
             * Compared to single stream, a clone would not work here. We need to add this bit of meta information
             * explicitly.
             */
            data.outputRowMeta.addValueMeta(meta.getOutputField());


            data.NrFields1 = data.oneMeta.size();
            data.NrFields2 = data.twoMeta.size();
        }

        //Get the selected fields as String
        String output1 = data.oneMeta.getString(data.one, meta.getFirstStream());
        String output2 = data.twoMeta.getString(data.two, meta.getSecondStream());

        //Our nonsense business logic to concatenate two String fields.
        String totalOutput = output1 + output2;

        //Initialize the output array.
        Object[] outputRow = RowDataUtil.allocateRowData(data.NrFields1+data.NrFields2+1); //+1 for result field

        //Insert fields from previous steps.
        int i=0;
        for (i = 0; i < data.NrFields1; i++) {
            outputRow[i] = data.one[i];
        }
        for (int j=0; j<data.NrFields2; j++) {
            outputRow[i++] = data.two[j];
        }

        //Add our result.
        outputRow[i] = totalOutput;

        //Send row downstream.
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
