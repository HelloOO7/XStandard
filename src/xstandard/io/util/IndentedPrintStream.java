package xstandard.io.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A PrintStream wrapper that allows setting per-line indentation.
 */
public class IndentedPrintStream extends PrintStream {

	private int indentLevel = 0;
	private int indentStep = 4;
	private boolean indentIsTabs = true;

	private String indentor = "";
	
	private boolean isNextPrintLineBegin = true;

	/**
	 * Creates an IndentedPrintStream for an output stream.
	 * @param out An OutputStream.
	 */
	public IndentedPrintStream(OutputStream out) {
		super(out);
	}
	
	public IndentedPrintStream(File f) throws FileNotFoundException {
		super(f);
	}

	/**
	 * Directly set the level of indentation.
	 * @param il Level of indentation.
	 */
	public void setIndentLevel(int il) {
		indentLevel = il;
		updateIndentor();
	}

	/**
	 * Increases the level of indentation by one.
	 */
	public void incrementIndentLevel() {
		setIndentLevel(indentLevel + 1);
	}

	/**
	 * If possible, decreases the level of indentation by one.
	 */
	public void decrementIndentLevel() {
		if (indentLevel != 0) {
			setIndentLevel(indentLevel - 1);
		}
	}

	/**
	 * Sets how many spaces represent one indentation level.
	 * If the stream uses tabs for indentation, the value is ignored.
	 * @param is Number of spaces per indentor.
	 */
	public void setIndentStep(int is) {
		indentStep = is;
		updateIndentor();
	}

	/**
	 * Set if the stream should use tabs for indentation.
	 * @param b True to use tabs, false to use spaces.
	 */
	public void setIndentIsTabs(boolean b) {
		indentIsTabs = b;
		updateIndentor();
	}

	private void updateIndentor() {
		StringBuilder sb = new StringBuilder();

		StringBuilder baseSB = new StringBuilder();
		if (indentIsTabs) {
			baseSB.append("\t");
		} else {
			for (int i = 0; i < indentStep; i++) {
				baseSB.append(" ");
			}
		}
		String base = baseSB.toString();

		for (int i = 0; i < indentLevel; i++) {
			sb.append(base);
		}

		indentor = sb.toString();
	}
	
	private void setIsNextLineBegin(){
		isNextPrintLineBegin = true;
	}

	@Override
	public void println(){
		super.println();
		setIsNextLineBegin();
	}
	
	@Override
	public void println(Object obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(String str) {
		super.println(str);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(boolean b) {
		super.println(b);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(char c) {
		super.println(c);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(char[] chars) {
		super.println(chars);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(double d) {
		super.println(d);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(float f) {
		super.println(f);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(int i) {
		super.println(i);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(long l) {
		super.println(l);

		setIsNextLineBegin();
	}
	
	private String createIndentedString(String str){
		if (isNextPrintLineBegin){
			super.print(indentor);
			isNextPrintLineBegin = false;
		}
		if (str.contains("\n")) {
			if (str.endsWith("\n")) {
				setIsNextLineBegin();
				return str.substring(0, str.length() - 1).replace("\n", "\n" + indentor) + "\n";
			}
			else {
				return str.replace("\n", "\n" + indentor);
			}
		}
		return str;
	}
	
	@Override
	public void print(Object obj) {
		print(String.valueOf(obj));
	}
	
	@Override
	public void print(String str) {
		super.print(createIndentedString(str));
	}
	
	@Override
	public void print(boolean b) {
		print(Boolean.toString(b));
	}
	
	@Override
	public void print(char c) {
		print(Character.toString(c));
	}
	
	@Override
	public void print(char[] chars) {
		print(new String(chars));
	}
	
	@Override
	public void print(double d) {
		print(Double.toString(d));
	}
	
	@Override
	public void print(float f) {
		print(Float.toString(f));
	}
	
	@Override
	public void print(int i) {
		print(Integer.toString(i));
	}
	
	@Override
	public void print(long l) {
		print(Long.toString(l));
	}
}
