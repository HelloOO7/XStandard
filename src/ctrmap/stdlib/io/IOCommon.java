package ctrmap.stdlib.io;

import ctrmap.stdlib.io.base.impl.ext.data.interpretation.DataInterpreterBE;
import ctrmap.stdlib.io.base.impl.ext.data.interpretation.DataInterpreterLE;
import ctrmap.stdlib.io.base.impl.ext.data.interpretation.IDataInterpreter;

import java.nio.ByteOrder;

public class IOCommon {
    public static final boolean LIBNSIO_DEBUG = false;

    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private static final IDataInterpreter DATA_INTERPRETER_LE = new DataInterpreterLE();
    private static final IDataInterpreter DATA_INTERPRETER_BE = new DataInterpreterBE();

    public static IDataInterpreter getDefaultDataInterpreter(){
        return getInterpreterForByteOrder(DEFAULT_BYTE_ORDER);
    }

    public static IDataInterpreter getInterpreterForByteOrder(ByteOrder order){
        if (order == ByteOrder.BIG_ENDIAN){
            return DATA_INTERPRETER_BE;
        }
        else {
            return DATA_INTERPRETER_LE;
        }
    }

    public static void debugPrint(String msg){
        if (LIBNSIO_DEBUG){
            System.out.println(msg);
        }
    }

    public static void debugPrintf(String format, Object... args){
        if (LIBNSIO_DEBUG){
            System.out.println(String.format(format, args));
        }
    }
}
