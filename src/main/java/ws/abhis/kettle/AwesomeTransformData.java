package ws.abhis.kettle;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Created by Abhishek on 2/17/2015.
 */
public class AwesomeTransformData extends BaseStepData implements StepDataInterface {
    public RowMetaInterface oneMeta, twoMeta;
    public RowMetaInterface outputRowMeta;

    public RowMetaInterface prevRowMeta;

    public RowSet oneRowSet;
    public RowSet twoRowSet;

    public Object[] one, two;

    public int NrFields1, NrFields2;
}
