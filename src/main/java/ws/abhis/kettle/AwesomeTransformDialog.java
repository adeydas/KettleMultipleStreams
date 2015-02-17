package ws.abhis.kettle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Created by Abhishek on 2/17/2015.
 */
public class AwesomeTransformDialog extends BaseStepDialog implements StepDialogInterface {

    private AwesomeTransformMeta input;

    //Vars
    private Label wlStream1;
    private Combo wStream1;
    private FormData fdlStream1, fdStream1;

    private Label wlStream2;
    private Combo wStream2;
    private FormData fdlStream2, fdStream2;

    private Button wRefresh;
    private Listener lsRefresh;


    public AwesomeTransformDialog(Shell parent, Object baseStepMeta, TransMeta transMeta, String stepname) {
        super(parent, (StepMetaInterface)baseStepMeta, transMeta, stepname);
        input = (AwesomeTransformMeta) baseStepMeta;
    }

    @Override
    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN
                | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                input.setChanged();
            }
        };
        changed = input.hasChanged();
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);
        shell.setText("Awesome Transform MI");
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.LEFT);
        wlStepname.setText("Multi-input");
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // Stream 1 label
        wlStream1 = new Label(shell, SWT.LEFT);
        wlStream1.setText("Stream #1");
        props.setLook(wlStream1);
        fdlStream1 = new FormData();
        fdlStream1.left = new FormAttachment(0, 0);
        fdlStream1.right = new FormAttachment(middle, -margin);
        fdlStream1.top = new FormAttachment(wStepname, margin);
        wlStream1.setLayoutData(fdlStream1);

        // Stream 1 Combo
        wStream1 = new Combo(shell, SWT.LEFT | SWT.READ_ONLY);
        props.setLook(wStream1);
        wStream1.addModifyListener(lsMod);
        fdStream1 = new FormData();
        fdStream1.left = new FormAttachment(middle, 0);
        fdStream1.right = new FormAttachment(100, 0);
        fdStream1.top = new FormAttachment(wStepname, margin);
        wStream1.setLayoutData(fdStream1);

        // Stream 2 label
        wlStream2 = new Label(shell, SWT.LEFT);
        wlStream2.setText("Stream #2");
        props.setLook(wlStream2);
        fdlStream2 = new FormData();
        fdlStream2.left = new FormAttachment(0, 0);
        fdlStream2.right = new FormAttachment(middle, -margin);
        fdlStream2.top = new FormAttachment(wStream1, margin);
        wlStream2.setLayoutData(fdlStream2);

        // Stream 2 combo
        wStream2 = new Combo(shell, SWT.LEFT | SWT.READ_ONLY);
        props.setLook(wStream2);
        wStream2.addModifyListener(lsMod);
        fdStream2 = new FormData();
        fdStream2.left = new FormAttachment(middle, 0);
        fdStream2.right = new FormAttachment(100, 0);
        fdStream2.top = new FormAttachment(wStream1, margin);
        wStream2.setLayoutData(fdStream2);


        // Buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString("System.Button.Cancel"));
        wRefresh = new Button(shell, SWT.PUSH);
        wRefresh.setText("Get Data");

        BaseStepDialog.positionBottomButtons(shell,
                new Button[] { wOK, wCancel, wRefresh }, margin, wStream2);
        // Listeners
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        lsRefresh = new Listener() {
            @Override
            public void handleEvent(Event event) {
                refresh();
            }
        };
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wRefresh.addListener(SWT.Selection, lsRefresh);
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });
        setSize();
        getData();
        input.setChanged(changed);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return stepname;
    }

    private void refresh() {
        try {
            RowMetaInterface rowMetaInterface = transMeta.getPrevStepFields(stepMeta);
            if ( rowMetaInterface != null ) {
                String[] prevStepFieldNames = rowMetaInterface.getFieldNames();

                wStream1.setItems(prevStepFieldNames);
                wStream2.setItems(prevStepFieldNames);

            }

        } catch (KettleStepException e) {
            MessageBox msgError = new MessageBox(shell);
            msgError.setMessage(e.getMessage());
            msgError.open();
        }
    }

    public void getData() {
        wStepname.selectAll();

        wStream1.setItems(input.getAllInputs1());
        wStream2.setItems(input.getAllInputs2());
        wStream1.select(input.getFirstStream());
        wStream2.select(input.getSecondStream());
    }

    private void cancel() {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    private boolean isEmpty(String s) {
        return s.isEmpty();
    }

    private void ok() {
        stepname = wStepname.getText(); // return value
        if (Const.isEmpty(stepname))
            return;

        input.setAllInputs1(wStream1.getItems());
        input.setAllInputs2(wStream2.getItems());
        input.setFirstStream(wStream1.getSelectionIndex());
        input.setSecondStream(wStream2.getSelectionIndex());

        input.setChanged();
        dispose();
    }
}
