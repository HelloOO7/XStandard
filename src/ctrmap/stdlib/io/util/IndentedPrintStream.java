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
	public void println(Object obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(String obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(boolean obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(char obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(char[] obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(double obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(float obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(int obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	@Override
	public void println(long obj) {
		super.println(obj);

		setIsNextLineBegin();
	}
	
	private void printImpl(Object obj){
		if (isNextPrintLineBegin){
			super.print(indentor);
			isNextPrintLineBegin = false;
		}
		super.print(obj);
	}
	
	@Override
	public void print(Object obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(String obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(boolean obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(char obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(char[] obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(double obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(float obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(int obj) {
		printImpl(obj);
	}
	
	@Override
	public void print(long obj) {
		printImpl(obj);
	}
}
