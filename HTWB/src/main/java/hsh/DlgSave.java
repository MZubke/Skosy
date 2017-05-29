package hsh;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;

public class DlgSave implements SelectionListener {

	private HTWB parent;
	
	public DlgSave(HTWB htwb) {
		super();
		parent = htwb;
	}

	
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		FileDialog fd = new FileDialog(parent.shlHTWB, SWT.SAVE);
        fd.setText("Save");
        fd.setFilterPath("C:/");
        String[] filterExt = { "*.rdf", "*.rdfs", ".ttl", ".xml", "*.*" };
        fd.setFilterExtensions(filterExt);
        String selected = fd.open();
        System.out.println(selected);
        parent.setModified(false);

	}

}
