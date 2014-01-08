package io.nlopez.androidannotations.viewbyid;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class EntryList extends JPanel {

	protected Project mProject;
	protected Editor mEditor;
	protected ArrayList<Element> mElements = new ArrayList<Element>();
	protected ArrayList<String> mGeneratedIDs = new ArrayList<String>();
	protected ArrayList<Entry> mEntries = new ArrayList<Entry>();
	protected String mPrefix = null;
	protected IConfirmListener mConfirmListener;
	protected ICancelListener mCancelListener;
	protected JCheckBox mPrefixCheck;
	protected JTextField mPrefixValue;
	protected JButton mConfirm;
	protected JButton mCancel;

	public EntryList(Project project, Editor editor, ArrayList<Element> elements, ArrayList<String> ids, IConfirmListener confirmListener, ICancelListener cancelListener) {
		mProject = project;
		mEditor = editor;
		mConfirmListener = confirmListener;
		mCancelListener = cancelListener;
		if (elements != null) {
			mElements.addAll(elements);
		}
		if (ids != null) {
			mGeneratedIDs.addAll(ids);
		}

		setPreferredSize(new Dimension(640, 360));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		addInjections();
		addButtons();
	}

	protected void addInjections() {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPanel.add(new EntryHeader());
		contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JPanel injectionsPanel = new JPanel();
		injectionsPanel.setLayout(new BoxLayout(injectionsPanel, BoxLayout.PAGE_AXIS));
		injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		int cnt = 0;
		for (Element element : mElements) {
			Entry entry = new Entry(this, element, mGeneratedIDs);

			if (cnt > 0) {
				injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			}
			injectionsPanel.add(entry);
			cnt++;

			mEntries.add(entry);
		}
		injectionsPanel.add(Box.createVerticalGlue());
		injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JBScrollPane scrollPane = new JBScrollPane(injectionsPanel);
		contentPanel.add(scrollPane);

		add(contentPanel, BorderLayout.CENTER);
		refresh();
	}

	protected void addButtons() {
		mCancel = new JButton();
		mCancel.setAction(new CancelAction());
		mCancel.setPreferredSize(new Dimension(120, 26));
		mCancel.setText("Cancel");
		mCancel.setVisible(true);

		mConfirm = new JButton();
		mConfirm.setAction(new ConfirmAction());
		mConfirm.setPreferredSize(new Dimension(120, 26));
		mConfirm.setText("Confirm");
		mConfirm.setVisible(true);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(mCancel);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(mConfirm);

		add(buttonPanel, BorderLayout.PAGE_END);
		refresh();
	}

	protected void refresh() {
		revalidate();

		if (mConfirm != null) {
			mConfirm.setVisible(mElements.size() > 0);
		}
	}

	protected boolean checkValidity() {
		boolean valid = true;

		for (Element element : mElements) {
			if (!element.checkValidity()) {
				valid = false;
			}
		}

		return valid;
	}


	public class CheckPrefixListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent event) {
			mPrefixValue.setEnabled(mPrefixCheck.isSelected());

			if (mPrefixCheck.isSelected() && mPrefixValue.getText().length() > 0) {
				mPrefix = mPrefixValue.getText();
			} else {
				mPrefix = null;
			}
		}
	}

	protected class ConfirmAction extends AbstractAction {

		public void actionPerformed(ActionEvent event) {
			boolean valid = checkValidity();

			for (Entry entry : mEntries) {
				entry.syncElement();
			}

			if (valid) {
				if (mConfirmListener != null) {
					mConfirmListener.onConfirm(mProject, mEditor, mElements, mPrefix);
				}
			}
		}
	}

	protected class CancelAction extends AbstractAction {

		public void actionPerformed(ActionEvent event) {
			if (mCancelListener != null) {
				mCancelListener.onCancel();
			}
		}
	}
}