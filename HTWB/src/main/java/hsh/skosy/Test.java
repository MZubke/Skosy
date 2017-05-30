package hsh.skosy;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;

public class Test {

	protected Shell shell;
	private Text text;
	private Table table;
	private Button btnNewButton;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Test window = new Test();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 300);
		shell.setText("SWT Application");
		shell.setLayout(new FormLayout());
		
		Composite composite = new Composite(shell, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(0, 262);
		fd_composite.right = new FormAttachment(0, 434);
		fd_composite.top = new FormAttachment(0);
		fd_composite.left = new FormAttachment(0);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new FormLayout());
		
		text = new Text(composite, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.right = new FormAttachment(0, 306);
		fd_text.top = new FormAttachment(0, 10);
		fd_text.left = new FormAttachment(0, 10);
		text.setLayoutData(fd_text);
		
		Button btnRadioButton = new Button(composite, SWT.RADIO);
		btnRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		FormData fd_btnRadioButton = new FormData();
		fd_btnRadioButton.top = new FormAttachment(text, 6);
		fd_btnRadioButton.left = new FormAttachment(0, 10);
		btnRadioButton.setLayoutData(fd_btnRadioButton);
		btnRadioButton.setText("Fuzzy Search");
		
		Button btnExactSearch = new Button(composite, SWT.RADIO);
		FormData fd_btnExactSearch = new FormData();
		fd_btnExactSearch.left = new FormAttachment(0, 10);
		fd_btnExactSearch.top = new FormAttachment(btnRadioButton, 6);
		btnExactSearch.setLayoutData(fd_btnExactSearch);
		btnExactSearch.setText("Exact Search");
		
		table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(btnExactSearch, 6);
		fd_table.bottom = new FormAttachment(100, -10);
		fd_table.left = new FormAttachment(0);
		fd_table.right = new FormAttachment(0, 434);
		table.setLayoutData(fd_table);
		
		btnNewButton = new Button(composite, SWT.NONE);
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.bottom = new FormAttachment(text, 0, SWT.BOTTOM);
		fd_btnNewButton.left = new FormAttachment(text, 13);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("New Button");

	}
}
