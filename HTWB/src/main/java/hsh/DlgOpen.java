package hsh;

import java.io.File;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import static javafx.application.ConditionalFeature.SWT;


public class DlgOpen implements SelectionListener {




	private HTWB parent;


	public DlgOpen(HTWB htwb) {
		super();
		parent = htwb;
	}


	@Override
	public void widgetSelected(SelectionEvent event) {
		FileDialog fd = new FileDialog(parent.shlHTWB);
		fd.setText("Open");
		fd.setFilterPath("C:/");
		String[] filterExt = { "*.rdf", "*.rdfs", "*.ttl", "*.xml", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if(selected != null) {
			File test = new File(selected);
			if(!test.exists()) {
				MessageBox messageBox = new MessageBox(parent.shlHTWB);
				messageBox.setText("File not found");
				messageBox.setMessage("File could not be found");
				messageBox.open();
			} else if (parent.isModified()) {
			
			} else {	
				parent.setThesaurus(selected);
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
	}
}

