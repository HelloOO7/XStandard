package ctrmap.stdlib.io;

import ctrmap.stdlib.io.base.impl.ext.data.interpretation.DataInterpreterBE;
import ctrmap.stdlib.io.base.impl.ext.data.interpretation.DataInterpreterLE;
import ctrmap.stdlib.io.base.impl.ext.data.interpretation.IDataInterpreter;

import java.nio.ByteOrder;

/**
 * New Super IO Library.
 * Common values and methods.
 */
public class IOCommon {
	/**
	 * Enables debug prints for all NSIO components.
	 */
    public static final boolean LIBNSIO_DEBUG = false;

	/**
	 * The default byte order of all NSIO data streams.
	 */
    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private static final IDataInterpreter DATA_INTERPRETER_LE = new DataInterpreterLE();
    private static final IDataInterpreter DATA_INTERPRETER_BE = new DataInterpreterBE();

	/**
	 * Gets the DataInterpreter corresponding to NSIO's default byte order.
	 * @return 
	 */
    public static IDataInterpreter getDefaultDataInterpreter(){
        return getInterpreterForByteOrder(DEFAULT_BYTE_ORDER);
    }

	/**
	 * Gets the DataInterpreter corresponding to a given ByteOrder.
	 * @param order A byte order value.
	 * @return DataInterpreter for 'order'.
	 */
    public static IDataInterpreter getInterpreterForByteOrder(ByteOrder order){
        if (order == ByteOrder.BIG_ENDIAN){
            return DATA_INTERPRETER_BE;
        }
        else {
            return DATA_INTERPRETER_LE;
        }
    }

	/**
	 * Prints a message only if LIBNSIIO_DEBUG is enabled.
	 * @param msg Message to print.
	 */
    public static void debugPrint(String msg){
        if (LIBNSIO_DEBUG){
            System.out.println(msg);
        }
    }

	/**
	 * Prints a formatted message only if LIBNSIIO_DEBUG is enabled. 
	 * @param format String format.
	 * @param args String format arguments.
	 */
    public static void debugPrintf(String format, Object... args){
        if (LIBNSIO_DEBUG){
            System.out.println(String.format(format, args));
        }
    }
}
