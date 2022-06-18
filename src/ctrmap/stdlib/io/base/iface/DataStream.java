/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ctrmap.stdlib.io.base.iface;

import ctrmap.stdlib.io.InvalidMagicException;
import java.nio.ByteOrder;

public interface DataStream {

	public void order(ByteOrder order);

	public default void orderByBOM(int BOM, int ifBE, int ifLE) {
		if (BOM == ifBE) {
			order(ByteOrder.BIG_ENDIAN);
		} else if (BOM == ifLE) {
			order(ByteOrder.LITTLE_ENDIAN);
		} else {
			throw new InvalidMagicException("Invalid BOM: 0x" + Integer.toHexString(BOM));
		}
	}
}
