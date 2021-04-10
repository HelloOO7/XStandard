package ctrmap.stdlib.gui.components;

import ctrmap.stdlib.util.StringEx;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NoSpaceFirstDocument extends PlainDocument {

	public NoSpaceFirstDocument() {
		super();
	}

	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if (str == null) {
			return;
		}
		if (offset == 0){
			int firstChar = StringEx.indexOfFirstNonWhitespace(str);
			if (firstChar != -1){
				super.insertString(offset, str.substring(firstChar), attr);
			}
		}
		else {
			super.insertString(offset, str, attr);
		}
	}
}
