package xstandard.util;

/**
 * A boolean flag that gets reset to a default value when read.
 */
public class TripFlag {
	private final boolean defaultValue;
	private boolean v;
	
	/**
	 * Creates a TripFlag from a value set.
	 * @param defaultValue The value the flag should be reset to when read.
	 * @param currentValue The value of the flag upon initialization.
	 */
	public TripFlag(boolean defaultValue, boolean currentValue){
		this.defaultValue = defaultValue;
		v = currentValue;
	}
	
	public TripFlag(){
		this(false, false);
	}
	
	/**
	 * Gets the flag's value and trips it.
	 * @return Value of the flag before being tripped.
	 */
	public boolean get(){
		boolean v2 = v;
		v = defaultValue;
		return v2;
	}
	
	/**
	 * Gets the flag's value without tripping it.
	 * @return The current value of the flag.
	 */
	public boolean peek(){
		return v;
	}
	
	/**
	 * Forces the flag into tripped state.
	 */
	public void raise(){
		v = !defaultValue;
	}
}
