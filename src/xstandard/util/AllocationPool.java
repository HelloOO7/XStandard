package xstandard.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AllocationPool<T> {

	private final Supplier<T> ctor;
	private final Consumer<T> initializer;
	private final List<T> freeValues = new ArrayList<T>();
	private final Set<T> freeValueLookup = new HashSet<>();
	
	private Class<? extends T> type;
	private int allocationCount;

	public AllocationPool(Class<? extends T> type, Supplier<T> ctor, Consumer<T> initializer) {
		this.type = type;
		this.ctor = ctor;
		this.initializer = initializer;
	}

	public T alloc() {
		T v;
		if (freeValues.isEmpty()) {
			v = ctor.get(); //freeing will make the newly allocated memory reusable
		} else {
			v = freeValues.remove(freeValues.size() - 1); //should be fast
			freeValueLookup.remove(v);
			initializer.accept(v);
		}
		allocationCount++;
		/*if (allocationCount % 1000 == 0) {
			System.out.println("Allocation count " + allocationCount + " for " + type);
		}*/
		return v;
	}

	public void free(T p) {
		if (p != null) {
			if (p.getClass() != type) {
				throw new RuntimeException("Called free for invalid type: " + p.getClass() + ", expected " + type);
			}
			if (!freeValueLookup.contains(p)) {
				if (allocationCount == 0) {
					throw new RuntimeException("All values already freed?? - " + p.getClass());
				}
				freeValues.add(p);
				freeValueLookup.add(p);
				allocationCount--;
			}
		}
	}
}
