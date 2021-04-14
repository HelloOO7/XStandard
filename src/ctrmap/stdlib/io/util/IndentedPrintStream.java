/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.stdlib.io.util;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 */
public class IndentedPrintStream extends PrintStream {

	private int indentLevel = 0;
	private int indentStep = 4;
	private boolean indentIsTabs = true;

	private String indentor = "";
	
	private boolean isNextPrintLineBegin = true;

	public IndentedPrintStream(OutputStream out) {
		super(out);
	}

	public void setIndentLevel(int il) {
		indentLevel = il;
		updateIndentor();
	}

	public void incrementIndentLevel() {
		setIndentLevel(indentLevel + 1);
	}

	public void decrementIndentLevel() {
		if (indentLevel != 0) {
			setIndentLevel(indentLevel - 1);
		}
	}

	public void setIndentStep(int is) {
		indentStep = 4;
		updateIndentor();
	}

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
	
	private void printIndentor(){
		if (isNextPrintLineBegin){
			super.print(indentor);
			isNextPrintLineBegin = false;
		}
	}
	
	@Override
	public void print(Object obj) {
		printIndentor();
		super.print(obj);
	}
	
	@Override
	public void print(String str) {
		printIndentor();
		super.print(str);
	}
	
	@Override
	public void print(boolean b) {
		printIndentor();
		super.print(b);
	}
	
	@Override
	public void print(char c) {
		printIndentor();
		super.print(c);
	}
	
	@Override
	public void print(char[] chars) {
		printIndentor();
		super.print(chars);
	}
	
	@Override
	public void print(double d) {
		printIndentor();
		super.print(d);
	}
	
	@Override
	public void print(float f) {
		printIndentor();
		super.print(f);
	}
	
	@Override
	public void print(int i) {
		printIndentor();
		super.print(i);
	}
	
	@Override
	public void print(long l) {
		printIndentor();
		super.print(l);
	}
}
